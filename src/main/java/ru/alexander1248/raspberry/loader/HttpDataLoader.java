package ru.alexander1248.raspberry.loader;

import ru.alexander1248.raspberry.Raspberry;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class HttpDataLoader {
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.of(
                    Raspberry.CONFIG.connectionTimeout(),
                    Raspberry.CONFIG.connectionTimeoutUnit().toChronoUnit()
            ))
            .build();

    public static HttpResponse<String> loadString(String uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
    public static CompletableFuture<HttpResponse<String>> loadStringAsync(String uri) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<Path> loadFile(String uri, Path file) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofFile(file));
    }

    public static CompletableFuture<HttpResponse<Path>> loadFileAsync(String uri, Path file) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofFile(file));
    }
}
