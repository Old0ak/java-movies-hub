package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

public class MovieHandler extends BaseHttpHandler {

    public MovieHandler(MoviesStore store) {
        super(store);
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

    private void handleCommonMovieById(HttpExchange ex, boolean isDelete) throws IOException {
        String path = ex.getRequestURI().getPath();
        String idStr = path.substring(path.lastIndexOf('/') + 1);

        try {
            int id = Integer.parseInt(idStr);
            Movie movie = store.getById(id);

            if (movie == null) {
                sendErrorToJson(ex, 404, new ErrorResponse("Фильм не найден"));
            } else {
                if (isDelete) {
                    store.delete(id);
                    sendNoContent(ex);
                } else {
                    String json = new Gson().toJson(movie);
                    sendJson(ex, 200, json);
                }
            }
        } catch (NumberFormatException e) {
            sendErrorToJson(ex, 400, new ErrorResponse("Некорректный ID"));
        }
    }

    private void handleDeleteMovieById(HttpExchange ex) throws IOException {
        handleCommonMovieById(ex, true);
    }

    private void handleGetMovieById(HttpExchange ex) throws IOException {
        handleCommonMovieById(ex, false);
    }
}
