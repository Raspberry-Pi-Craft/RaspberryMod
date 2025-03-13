package ru.alexander1248.raspberry.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import ru.alexander1248.raspberry.Raspberry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PackIndexUpdater {
    private static String TEMP_PATH = "raspberry_temp";
    private static final Path GAME_FOLDER = FabricLoader.getInstance().getGameDir();
    private static final EnvType ENV = FabricLoader.getInstance().getEnvironmentType();

    private final PackFile[] files;


    private final List<String> oldFiles = new LinkedList<>();
    private final List<PackFile> updateQuery = new LinkedList<>();


    public PackIndexUpdater(String uri) throws IOException, InterruptedException {
        var json = HttpDataLoader.loadString(uri);
        Gson gson = new GsonBuilder().create();
        files = gson.fromJson(json, PackFile[].class);
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
        Files.createDirectory(temp);
        // Save state
        Path files = temp.resolve("new");
        Files.createDirectory(files);
        for (PackFile packFile : updateQuery)
                HttpDataLoader.loadFile(packFile.downloadUri, files.resolve(packFile.path));

        PrintWriter oldWriter = new PrintWriter(temp.resolve("old.txt").toFile());
        oldFiles.forEach(oldWriter::println);
        oldWriter.close();

        PrintWriter commandWriter = new PrintWriter(temp.resolve("start.txt").toFile());
        Optional<ProcessHandle> parentProcess = ProcessHandle.current().parent();
        if (parentProcess.isPresent()) {
            ProcessHandle.Info info = parentProcess.get().info();
            if (info.command().isPresent()) {
                commandWriter.write(info.command().get());
                commandWriter.write(" ");
                if (info.arguments().isPresent())
                    commandWriter.write(String.join(" ", info.arguments().get()));
                else
                    commandWriter.write(String.join(" ", ManagementFactory.getRuntimeMXBean().getInputArguments()));
            }
            else
                Raspberry.LOGGER.warn("Auto reload command building error!");
        }
        else
            Raspberry.LOGGER.warn("Auto reload command building error!");
        commandWriter.close();


        // Run updater
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", temp.resolve("raspberry.bat").toString());
            builder.directory(temp.toFile());
            builder.start();

        } else if (os.contains("linux") || os.contains("mac")) {
            ProcessBuilder builder = new ProcessBuilder("sh", temp.resolve("raspberry.sh").toString());
            builder.directory(temp.toFile());
            builder.start();
        }
        System.exit(0);
    }
    public static void deleteDirectory(Path path) throws IOException {
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
        if (files == null) return false;
        boolean updated = false;
        for (PackFile file : files)
            if (checkFile(file))
                updated = true;
        return updated;
    }

    private boolean checkFile(PackFile file) throws IOException {
        // File for another environment check
        if (!file.environments.get(ENV.name().toLowerCase())) return false;

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
                if (!hashFile(file, entry.getKey()).equals(entry.getValue()))
                    return false;
            } catch (NoSuchAlgorithmException e) {}
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
