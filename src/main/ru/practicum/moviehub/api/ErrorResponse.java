package ru.practicum.moviehub.api;

import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {

    private String error;
    private List<String> details;

    public ErrorResponse(String error, String detail) {
        this.error = error;
        this.details = new ArrayList<>();
        details.add(detail);
    }

    public ErrorResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<String> getDetails() {
        return details;
    }

    public void addDetails(String detail) {
        details.add(detail);
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }
}