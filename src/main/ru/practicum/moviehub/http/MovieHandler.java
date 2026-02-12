package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

public class MovieHandler extends BaseHttpHandler {

    private final MoviesStore store;

    public MovieHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        Endpoint endpoint = getEndpoint(ex.getRequestMethod());

        switch (endpoint) {
            case GET -> handleGetMovieById(ex);
            case DELETE -> handleDeleteMovieById(ex);
            default -> handleUnknown(ex, ex.getRequestMethod());
        }
    }

    private void handleDeleteMovieById(HttpExchange ex) throws IOException {
        try {
            int id = receivingIdByRequest(ex);
            Movie movie = store.getById(id);

            if (movie == null) {
                sendErrorToJson(ex, 404, new ErrorResponse("Фильм не найден"));
            } else {
                store.delete(id);
                sendNoContent(ex);
            }
        } catch (NumberFormatException e) {
            sendErrorToJson(ex, 400, new ErrorResponse("Некорректный ID"));
        }
    }

    private void handleGetMovieById(HttpExchange ex) throws IOException {
        try {
            int id = receivingIdByRequest(ex);
            Movie movie = store.getById(id);

            if (movie == null) {
                sendErrorToJson(ex, 404, new ErrorResponse("Фильм не найден"));
            } else {
                String json = new Gson().toJson(movie);
                sendJson(ex, 200, json);
            }
        } catch (NumberFormatException e) {
            sendErrorToJson(ex, 400, new ErrorResponse("Некорректный ID"));
        }
    }

    private int receivingIdByRequest(HttpExchange ex) {
        String path = ex.getRequestURI().getPath();
        String idStr = path.substring(path.lastIndexOf('/') + 1);
        return Integer.parseInt(idStr);
    }
}
