package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

    protected static final String CT_JSON = "application/json; charset=UTF-8";

    protected void sendJson(HttpExchange ex, int status, String json) throws IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendErrorToJson(HttpExchange ex, int status, ErrorResponse errorResponse) throws IOException {
        String json = new Gson().toJson(errorResponse);
        sendJson(ex, status, json);
    }

    protected Endpoint getEndpoint(String requestMethod) {
        if (requestMethod.equalsIgnoreCase("GET")) {
            return Endpoint.GET;
        } else if (requestMethod.equalsIgnoreCase("POST")) {
            return Endpoint.POST;
        } else if (requestMethod.equalsIgnoreCase("DELETE")) {
            return Endpoint.DELETE;
        }
        return Endpoint.UNKNOWN;
    }

    protected void handleUnknown(HttpExchange ex, String requestMethod) throws IOException {
        sendErrorToJson(ex, 404, new ErrorResponse("Ошибка эндпойнта",
                "эндпойнт " + requestMethod + " не существует"));
    }

    protected void sendNoContent(HttpExchange ex) throws java.io.IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(204, -1);
    }
}