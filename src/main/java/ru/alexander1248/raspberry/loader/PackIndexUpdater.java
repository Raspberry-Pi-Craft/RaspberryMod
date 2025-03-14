package ru.alexander1248.raspberry.loader;

import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.alexander1248.raspberry.Raspberry;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class PackIndexUpdater {
    private static final String TEMP_PATH = "raspberry_temp";
    private static final Path GAME_FOLDER = FabricLoader.getInstance().getGameDir();
    private static final EnvType ENV = FabricLoader.getInstance().getEnvironmentType();

    private final PackFile[] files;


    private final List<String> oldFiles = new LinkedList<>();
    private final List<PackFile> updateQuery = new LinkedList<>();


    public PackIndexUpdater(String uri) throws IOException, InterruptedException {
        var response = HttpDataLoader.loadString(uri);
        if (response.statusCode() == 302) {
            uri = response.headers().firstValue(HttpHeaders.LOCATION).orElse(uri);
            Raspberry.LOGGER.info("Redirected to: {}", uri);
            response = HttpDataLoader.loadString(uri);
        }
        if (response.statusCode() != 200) {
            for (int i = 1; i <= Raspberry.CONFIG.connectionRetry(); i++) {
                Raspberry.LOGGER.warn("Failed to load pack index! Attempt {}!", i);
                response = HttpDataLoader.loadString(uri);
                if (response.statusCode() == 302) {
                    uri = response.headers().firstValue(HttpHeaders.LOCATION).orElse(uri);
                    Raspberry.LOGGER.info("Redirected to: {}", uri);
                    response = HttpDataLoader.loadString(uri);
                }
                if (response.statusCode() == 200) break;
            }
            if (response.statusCode() != 200) {
                Raspberry.LOGGER.error("Pack index loading failed! URL: {}", uri);
                files = new PackFile[0];
                return;
            }
        }
        Gson gson = new GsonBuilder().create();
        files = gson.fromJson(response.body(), PackFile[].class);
    }

    public void tryUpdateFiles() throws IOException, InterruptedException {
        if (!checkFiles()) {
            deleteDirectory(GAME_FOLDER.resolve(TEMP_PATH));
            return;
        }
        startFileUpdate();
    }
    private void startFileUpdate() throws IOException, InterruptedException {
        Path temp = GAME_FOLDER.resolve(TEMP_PATH);

        // Save state
        Path files = temp.resolve("new");
        Files.createDirectories(files);
        for (PackFile packFile : updateQuery) {
            Path filepath = files.resolve(packFile.path);
            Files.createDirectories(filepath.getParent());
            var uri = packFile.downloadUri;
            var response = HttpDataLoader.loadFile(uri, filepath);
            if (response.statusCode() == 302) {
                uri = response.headers().firstValue(HttpHeaders.LOCATION).orElse(uri);
                Raspberry.LOGGER.info("Redirected to: {}", uri);
                response = HttpDataLoader.loadFile(uri, filepath);
            }
            if (response.statusCode() != 200) {
                for (int i = 1; i <= Raspberry.CONFIG.connectionRetry(); i++) {
                    Raspberry.LOGGER.warn("Failed to download file! Attempt {}!", i);
                    response = HttpDataLoader.loadFile(uri, filepath);
                    if (response.statusCode() == 302) {
                        uri = response.headers().firstValue(HttpHeaders.LOCATION).orElse(uri);
                        Raspberry.LOGGER.info("Redirected to: {}", uri);
                        response = HttpDataLoader.loadFile(uri, filepath);
                    }
                    if (response.statusCode() == 200) break;
                }
                if (response.statusCode() != 200) {
                    Raspberry.LOGGER.error("Download file failed! URL: {}", uri);
                    break;
                }
            }
            Raspberry.LOGGER.info("Asset {} loaded!", packFile.path);
        }
        Raspberry.LOGGER.info("Asset loading complete!");

        PrintWriter oldWriter = new PrintWriter(temp.resolve("old.txt").toFile());
        oldFiles.forEach(oldWriter::println);
        oldWriter.close();


        PrintWriter commandWriter = new PrintWriter(temp.resolve("start.txt").toFile());
        if (Raspberry.CONFIG.autoReload()) {
            Optional<ProcessHandle> parentProcess = ProcessHandle.current().parent();
            if (parentProcess.isPresent()) {
                ProcessHandle.Info info = parentProcess.get().info();
                if (info.command().isPresent()) {
                    commandWriter.write("\"");
                    commandWriter.write(info.command().get());
                    commandWriter.write("\" ");
                    if (info.arguments().isPresent())
                        commandWriter.write(String.join(" ", info.arguments().get()));
                    else
                        commandWriter.write(String.join(" ", ManagementFactory.getRuntimeMXBean().getInputArguments()));
                } else
                    Raspberry.LOGGER.warn("Auto reload command building error!");
            } else
                Raspberry.LOGGER.warn("Auto reload command building error!");
        }
        commandWriter.close();


        // Run updater
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            InputStream resource = loadScript("raspberry.bat");
            if (resource == null) return;
            Path scriptPath = temp.resolve("raspberry.bat");
            Files.copy(resource, scriptPath, StandardCopyOption.REPLACE_EXISTING);

            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "raspberry.bat")
                    .directory(temp.toFile()).inheritIO();
            var p = builder.start();
            p.waitFor();
        } else if (os.contains("linux") || os.contains("mac")) {
            InputStream resource = loadScript("raspberry.sh");
            if (resource == null) return;
            Path scriptPath = temp.resolve("raspberry.sh");
            Files.copy(resource, scriptPath, StandardCopyOption.REPLACE_EXISTING);

            ProcessBuilder builder = new ProcessBuilder("nohup", "sh", "raspberry.sh", "&")
                    .directory(temp.toFile()).inheritIO();
            var p = builder.start();
            p.waitFor();
        }
        System.exit(0);
    }

    private @Nullable InputStream loadScript(String name) throws IOException {
        InputStream resource = getClass().getClassLoader().getResourceAsStream(name);
        if (resource == null) {
            deleteDirectory(GAME_FOLDER.resolve(TEMP_PATH));
            Raspberry.LOGGER.error("Auto updater not found!");
            return null;
        }
        return resource;
    }

    public static void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public @NotNull FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public @NotNull FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private boolean checkFiles() throws IOException {
        boolean updated = false;
        for (PackFile file : files)
            if (checkFile(file))
                updated = true;
        return updated;
    }

    private boolean checkFile(PackFile file) throws IOException {
        // File for another environment check
        if (!file.environments.contains(ENV.name().toLowerCase())) return false;

        // Check paths and alters
        Path path = GAME_FOLDER.resolve(file.path);
        if (Files.exists(path)) {
            if (checkHash(path, file.hashes)) return false;
            else {
                oldFiles.add(file.path);
                update(file);
                return true;
            }
        }
        for (String alternativePath : file.alternativePaths) {
            path =  GAME_FOLDER.resolve(alternativePath);
            if (Files.exists(path)) {
                if (checkHash(path, file.hashes)) return false;
                else {
                    oldFiles.add(alternativePath);
                    update(file);
                    return true;
                }
            }
        }

        // File not found
        update(file);
        return true;
    }

    private static boolean checkHash(Path path, Map<String, String> hashes) throws IOException {
        File file = path.toFile();
        for (Map.Entry<String, String> entry : hashes.entrySet()) {
            try {
                if (!hashFile(file, entry.getKey().toUpperCase()).equals(entry.getValue()))
                    return false;
            } catch (NoSuchAlgorithmException e) {
                Raspberry.LOGGER.warn("Hash {} check failed!", entry.getKey(), e);
            }
        }
        return true;
    }

    private static String hashFile(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] buffer = new byte[8192];
        int bytesRead;

        try (FileInputStream stream = new FileInputStream(file)) {
            while ((bytesRead = stream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        return bytesToHex(digest.digest());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) hexString.append(String.format("%02x", b));
        return hexString.toString();
    }

    private void update(PackFile file) {
        Raspberry.LOGGER.info("File added to update query: {}", file.path);
        updateQuery.add(file);
    }
}
