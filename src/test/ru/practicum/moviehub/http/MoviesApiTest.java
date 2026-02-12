package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {

    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore moviesStore = new MoviesStore();

    @BeforeAll
    static void beforeAll() {

        server = new MoviesServer(moviesStore, 8080);
        server.start();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @AfterEach
    void afterEach() {
        moviesStore.clear();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    private HttpResponse<String> sendHttpRequest(URI uri, String method, String body, String contentType) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", contentType);

        if ("POST".equals(method)) {
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
        } else if ("GET".equals(method)) {
            requestBuilder.GET();
        } else if ("DELETE".equals(method)) {
            requestBuilder.DELETE();
        }

        HttpRequest request = requestBuilder.build();

        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void postMovie_whenSuccess() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Название фильма\", \"year\": 2026}"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body();
        Movie addedMovie = new Gson().fromJson(body, Movie.class);
        assertEquals("Название фильма", addedMovie.getTitle());
        assertEquals(2026, addedMovie.getYear());
    }

    @Test
    void postMovie_whenErrorValidationByYear() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Название фильма\", \"year\": 2047}"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies со значением года 2047 должен вернуть 422");
    }

    @Test
    void postMovie_whenErrorValidationByName() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"     \", \"year\": 2026}"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(),
                "POST /movies с пустой строкой вместо названия должен вернуть 422");
    }

    @Test
    void postMovie_whenErrorContentTypeByHeader() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Название фильма\", \"year\": 2026}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(415, resp.statusCode(),
                "POST /movies в header не указан формат charset=UTF-8, должен вернуть 415");
    }

    @Test
    void getMovieById_whenSuccess() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Название фильма\", \"year\": 2026}"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(201, resp.statusCode());

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1"))
                .GET()
                .build();

        HttpResponse<String> resp2 =
                client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, resp2.statusCode());
    }

    @Test
    void getMovieById_whenErrorMovieNotFound() throws Exception {
        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1"))
                .GET()
                .build();

        HttpResponse<String> resp2 =
                client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(404, resp2.statusCode());
    }

    @Test
    void getMovieById_whenErrorIdNotNumber() throws Exception {
        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/edfaaw"))
                .GET()
                .build();

        HttpResponse<String> resp2 =
                client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(400, resp2.statusCode());
    }

    @Test
    void deleteMovieById_whenSuccessDelete() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Название фильма\", \"year\": 2026}"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(201, resp.statusCode());

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1"))
                .DELETE()
                .build();

        HttpResponse<String> resp2 =
                client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(204, resp2.statusCode());
    }

    @Test
    void deleteMovieById_whenErrorMovieNotFound() throws Exception {
        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1"))
                .DELETE()
                .build();

        HttpResponse<String> resp2 =
                client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(404, resp2.statusCode());
    }

    @Test
    void getMoviesByYear_whenSuccessYear() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Название фильма\", \"year\": 2026}"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(201, resp.statusCode());

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2026"))
                .GET()
                .build();

        HttpResponse<String> resp2 =
                client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, resp2.statusCode());
    }

    @Test
    void getMoviesByYear_whenError() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Название фильма\", \"year\": 2026}"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(201, resp.statusCode());

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?yearse=2026"))
                .GET()
                .build();

        HttpResponse<String> resp2 =
                client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(400, resp2.statusCode());
    }
}