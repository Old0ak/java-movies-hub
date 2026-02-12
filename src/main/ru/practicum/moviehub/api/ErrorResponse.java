package ru.practicum.moviehub.api;

public class ErrorResponse {

    private String error;
    private String details;

    public ErrorResponse(String error, String details) {
        this.error = error;
        this.details = details;
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

    public String getDetails() {
        return details;
    }

    public void addDetails(String details) {
        if (this.details == null) {
            this.details = details;
        } else {
            this.details = this.details + ", " + details;
        }
    }
}