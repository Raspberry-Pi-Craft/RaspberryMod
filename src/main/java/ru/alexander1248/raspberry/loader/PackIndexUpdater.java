package ru.alexander1248.raspberry.loader;

import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.ProgressListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.alexander1248.raspberry.Raspberry;
import ru.alexander1248.raspberry.loader.data.PackData;
import ru.alexander1248.raspberry.loader.data.PackFile;
import ru.alexander1248.raspberry.loggers.AbstractMessenger;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static ru.alexander1248.raspberry.Raspberry.CONFIG;

public class PackIndexUpdater {
    private static final String TEMP_PATH = "raspberry_temp";
    private static final Path GAME_FOLDER = FabricLoader.getInstance().getGameDir();
    private static final EnvType ENV = FabricLoader.getInstance().getEnvironmentType();

    private static PackData data;
    private static final List<String> oldFiles = new LinkedList<>();
    private static final List<PackFile> updateQuery = new LinkedList<>();

    private static boolean needUpdate = false;

    public static boolean isNeedUpdate() {
        return needUpdate;
    }
    private PackIndexUpdater() {}

    public static void init(AbstractMessenger messenger) throws IOException, InterruptedException {
        var uri = CONFIG.modListUri();
        var response = HttpDataLoader.loadString(uri);
        if (response.statusCode() == 302) {
            uri = response.headers().firstValue(HttpHeaders.LOCATION).orElse(uri);
            messenger.debug("Redirected to: {}", uri);
            response = HttpDataLoader.loadString(uri);
        }
        if (response.statusCode() != 200) {
            for (int i = 1; i <= CONFIG.connectionRetry(); i++) {
                messenger.warn("Failed to load pack index! Attempt {}!", i);
                response = HttpDataLoader.loadString(uri);
                if (response.statusCode() == 302) {
                    uri = response.headers().firstValue(HttpHeaders.LOCATION).orElse(uri);
                    messenger.debug("Redirected to: {}", uri);
                    response = HttpDataLoader.loadString(uri);
                }
                if (response.statusCode() == 200) break;
            }
            if (response.statusCode() != 200) {
                messenger.error("Pack index loading failed! URL: {}", uri);
                data = new PackData();
                return;
            }
        }
        Gson gson = new GsonBuilder().create();
        data = gson.fromJson(response.body(), PackData.class);
    }

    public static void tryUpdateFiles(AbstractMessenger messenger, ProgressListener listener) throws IOException, InterruptedException {
        if (!needUpdate) return;
        Path temp = GAME_FOLDER.resolve(TEMP_PATH);


        if (listener != null)
            listener.setTitleAndTask(Text.translatable("raspberry.asset_loading"));
        // Save state
        Path files = temp.resolve("new");
        Files.createDirectories(files);
        for (int j = 0; j < updateQuery.size(); j++) {
            PackFile packFile = updateQuery.get(j);
            if (listener != null) {
                listener.setTask(Text.literal(packFile.path));
                listener.progressStagePercentage(100 * j / updateQuery.size());
            }
            Path filepath = files.resolve(packFile.path);
            Files.createDirectories(filepath.getParent());
            var uri = packFile.downloadUri;
            var response = HttpDataLoader.loadFile(uri, filepath);
            if (response.statusCode() == 302) {
                uri = response.headers().firstValue(HttpHeaders.LOCATION).orElse(uri);
                messenger.debug("Redirected to: {}", uri);
                response = HttpDataLoader.loadFile(uri, filepath);
            }
            if (response.statusCode() != 200) {
                for (int i = 1; i <= CONFIG.connectionRetry(); i++) {
                    messenger.warn("Failed to download file! Attempt {}!", i);
                    response = HttpDataLoader.loadFile(uri, filepath);
                    if (response.statusCode() == 302) {
                        uri = response.headers().firstValue(HttpHeaders.LOCATION).orElse(uri);
                        messenger.debug("Redirected to: {}", uri);
                        response = HttpDataLoader.loadFile(uri, filepath);
                    }
                    if (response.statusCode() == 200) break;
                }
                if (response.statusCode() != 200) {
                    messenger.error("Download file failed! URL: {}", uri);
                    break;
                }
            }
            messenger.info("Asset {} loaded!", packFile.path);
        }
        messenger.info("Asset loading complete!");

        if (listener != null)
            listener.setTitleAndTask(Text.translatable("raspberry.reload_prepare"));

        if (listener != null) {
            listener.setTask(Text.translatable("raspberry.reload_prepare.old_file_saving"));
            listener.progressStagePercentage(25);
        }
        PrintWriter oldWriter = new PrintWriter(temp.resolve("old.txt").toFile());
        oldFiles.forEach(oldWriter::println);
        oldWriter.close();


        PrintWriter commandWriter = new PrintWriter(temp.resolve("start.txt").toFile());
        if (CONFIG.autoReload()) {
            if (listener != null) {
                listener.setTask(Text.translatable("raspberry.reload_prepare.command_baking"));
                listener.progressStagePercentage(50);
            }
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
                    messenger.warn("Auto reload command building error!");
            } else
                messenger.warn("Auto reload command building error!");
        }
        commandWriter.close();


        // Run updater
        if (listener != null) {
            listener.setTask(Text.translatable("raspberry.reload_prepare.script_extraction"));
            listener.progressStagePercentage(75);
        }
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            InputStream resource = loadScript("raspberry.bat", messenger);
            if (resource == null) return;
            Path scriptPath = temp.resolve("raspberry.bat");
            Files.copy(resource, scriptPath, StandardCopyOption.REPLACE_EXISTING);

            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "raspberry.bat")
                    .directory(temp.toFile()).inheritIO();
            var p = builder.start();
            p.waitFor();
        } else if (os.contains("linux") || os.contains("mac")) {
            InputStream resource = loadScript("raspberry.sh", messenger);
            if (resource == null) return;
            Path scriptPath = temp.resolve("raspberry.sh");
            Files.copy(resource, scriptPath, StandardCopyOption.REPLACE_EXISTING);

            ProcessBuilder builder = new ProcessBuilder("nohup", "sh", "raspberry.sh", "&")
                    .directory(temp.toFile()).inheritIO();
            var p = builder.start();
            p.waitFor();
        }
        if (listener != null) {
            listener.setTask(Text.translatable("raspberry.reload_prepare.reloading"));
            listener.progressStagePercentage(100);
        }
        if (listener != null)
            listener.setDone();
        System.exit(0);
    }

    private static @Nullable InputStream loadScript(String name, AbstractMessenger messenger) throws IOException {
        InputStream resource = PackIndexUpdater.class.getClassLoader().getResourceAsStream(name);
        if (resource == null) {
            deleteDirectory(GAME_FOLDER.resolve(TEMP_PATH));
            messenger.error("Auto updater not found!");
            return null;
        }
        return resource;
    }

    private static void deleteDirectory(Path path) throws IOException {
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

    public static void checkFiles(AbstractMessenger messenger) throws IOException {
        deleteDirectory(GAME_FOLDER.resolve(TEMP_PATH));
        updateQuery.clear();
        oldFiles.clear();
        needUpdate = false;
        for (PackFile file : data.files)
            if (checkFile(file, messenger))
                needUpdate = true;
    }

    private static boolean checkFile(PackFile file, AbstractMessenger messenger) throws IOException {
        // File for another environment check
        if (!file.environments.contains(ENV.name().toLowerCase())) return false;

        // Check paths and alters
        Path path = GAME_FOLDER.resolve(file.path);
        if (Files.exists(path)) {
            if (checkHash(path, file.hashes, messenger)) return false;
            else {
                oldFiles.add(file.path);
                update(file, messenger);
                return true;
            }
        }
        for (String alternativePath : file.alternativePaths) {
            path =  GAME_FOLDER.resolve(alternativePath);
            if (Files.exists(path)) {
                if (checkHash(path, file.hashes, messenger)) return false;
                else {
                    oldFiles.add(alternativePath);
                    update(file, messenger);
                    return true;
                }
            }
        }

        // File not found
        update(file, messenger);
        return true;
    }

    private static boolean checkHash(Path path, Map<String, String> hashes, AbstractMessenger messenger) throws IOException {
        File file = path.toFile();
        for (Map.Entry<String, String> entry : hashes.entrySet()) {
            try {
                if (!hashFile(file, entry.getKey().toUpperCase()).equals(entry.getValue()))
                    return false;
            } catch (NoSuchAlgorithmException e) {
                messenger.warn("Hash {} check failed!", entry.getKey(), e);
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

    private static void update(PackFile file, AbstractMessenger messenger) {
        messenger.info("File added to update query: {}", file.path);
        updateQuery.add(file);
    }

    public static boolean isNeedUpdateImmediately() {
        return data.isNeedUpdateImmediately;
    }
}
