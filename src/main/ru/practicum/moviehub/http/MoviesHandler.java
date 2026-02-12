package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler {

    private final MoviesStore store;

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        Endpoint endpoint = getEndpoint(ex.getRequestMethod());

        switch (endpoint) {
            case GET -> handleGetMovies(ex);
            case POST -> handlePostMovie(ex);
            default -> handleUnknown(ex, ex.getRequestMethod());
        }
    }

    private void handlePostMovie(HttpExchange ex) throws IOException {
        String contentType = ex.getRequestHeaders().getFirst("Content-Type");
        if (!"application/json; charset=UTF-8".equals(contentType)) {
            sendErrorToJson(ex, 415, new ErrorResponse("Unsupported Media Type"));
            return;
        }

        StringBuilder requestBodyBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8))){
            String line;

            while ((line = reader.readLine()) != null) {
                requestBodyBuilder.append(line);
            }
        }

        String requestBody = requestBodyBuilder.toString();
        Movie movie = new Gson().fromJson(requestBody, Movie.class);

        if (isAlreadyAddedMovie(ex, movie)) {
            return;
        }

        List<String> validatorErrors = validateMovie(movie);

        if (validatorErrors.isEmpty()) {
            store.add(movie);
            String json = new Gson().toJson(movie);
            sendJson(ex, 201, json);
        } else {
            ErrorResponse errorResponse = new ErrorResponse("Ошибка валидации");
            errorResponse.setDetails(validatorErrors);
            sendErrorToJson(ex, 422, errorResponse);
        }
    }

    private List<String> validateMovie(Movie movie) {
        List<String> errors = new ArrayList<>();

        if (movie.getTitle() == null || movie.getTitle().isBlank()) {
            errors.add("название не должно быть пустым");
        }
        if (!(movie.getYear() >= 1888 && movie.getYear() <= LocalDate.now().getYear() + 1)) {
            errors.add("год должен быть между 1888 и " + (LocalDate.now().getYear() + 1));
        }

        return errors;
    }

    private void handleGetMovies(HttpExchange ex) throws IOException {
        String query = ex.getRequestURI().getQuery();
        if (query != null) {
            getMoviesByYear(ex, query);
            return;
        }

        List<Movie> movies = store.getAll();
        String json;

        if (movies.isEmpty()) {
            json = "[]";
        } else {
            json = new Gson().toJson(movies);
        }
        sendJson(ex, 200, json);
    }

    private boolean isAlreadyAddedMovie(HttpExchange ex, Movie movie) throws IOException {
        for (Movie movieAdded : store.getAll()) {
            if (movie.equals(movieAdded)) {
                sendErrorToJson(ex, 409, new ErrorResponse("Conflict",
                        "Фильм " + movieAdded.getTitle() + ", " + movieAdded.getYear() +
                                " уже существует, его id: " + movieAdded.getId()));
                return true;
            }
        }
        return false;
    }

    private String getParameter(String query, String paramName) {
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && paramName.equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }

    private boolean isValidYear(int year) {
        return year >= 1888 && year <= LocalDate.now().getYear() + 1;
    }

    private void getMoviesByYear(HttpExchange ex, String query) throws IOException {
            String yearParam = getParameter(query, "year");
            if (yearParam != null) {
                int year = Integer.parseInt(yearParam);
                if (isValidYear(year)) {
                    List<Movie> movies = store.filterByYear(year);
                    String json = new Gson().toJson(movies);
                    sendJson(ex, 200, json);
                } else {
                    sendErrorToJson(ex, 400, new ErrorResponse("Год должен быть между 1888 и текущим годом"));
                }
            } else {
                sendErrorToJson(ex, 400, new ErrorResponse("Некорректный параметр запроса — 'year'"));
            }
    }
}
