package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Service
public class GenreService {
    private final GenreStorage genreStorage;

    @Autowired
    public GenreService(@Qualifier("DBGenreStorage") GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public List<Genre> getGenres() {
        List<Genre> result = genreStorage.getGenres();
        if (result.isEmpty()) {
            throw new NotFoundException("Жанры отсутствуют");
        }
        return result;
    }

    public Genre getGenre(long id) {
        Genre genre = genreStorage.getGenre(id);
        if (genre == null) {
            throw new NotFoundException("Неизвестный жанр: " + id);
        }
        return genre;
    }
}
