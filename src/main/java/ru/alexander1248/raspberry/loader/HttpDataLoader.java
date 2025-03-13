package ru.alexander1248.raspberry.loader;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class HttpDataLoader {
    private static final HttpClient client = HttpClient.newBuilder().build();

    public static String loadString(String uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) return null;
        return response.body();
    }
    public static CompletableFuture<String> loadStringAsync(String uri) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(
                response -> response.statusCode() == 200 ? response.body() : null
        );
    }

    public static boolean loadFile(String uri, Path file) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofFile(file));
        return response.statusCode() == 200;
    }

    public static CompletableFuture<Boolean> loadFileAsync(String uri, Path file) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofFile(file)).thenApply(
                response -> response.statusCode() == 200
        );
    }
}
