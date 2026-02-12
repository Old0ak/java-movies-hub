package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoviesStore implements MoviesManager {

    private Map<Integer, Movie> movies = new HashMap<>();

    private int generatedId = 0;

    protected int generateId() {
        return ++generatedId;
    }

    @Override
    public void add(Movie movie) {
        int id = generateId();
        movie.setId(id);
        movies.put(id, movie);
    }

    @Override
    public Movie getById(int id) {
        return movies.get(id);
    }

    @Override
    public List<Movie> getAll() {
        return new ArrayList<>(movies.values());
    }

    @Override
    public void delete(int id) {
        movies.remove(id);
    }

    @Override
    public List<Movie> filterByYear(int year) {
        List<Movie> filteredMovie = new ArrayList<>();

        for (Movie movie : movies.values()) {
            if (movie.getYear() == year) {
                filteredMovie.add(movie);
            }
        }
        return filteredMovie;
    }

    @Override
    public void clear() {
        movies.clear();
        generatedId = 0;
    }
}