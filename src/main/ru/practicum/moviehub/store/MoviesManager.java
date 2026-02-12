package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.List;

public interface MoviesManager {

    void add(Movie movie);

    Movie getById(int id);

    List<Movie> getAll();

    void delete(int id);

    List<Movie> filterByYear(int year);

    void clear();
}
