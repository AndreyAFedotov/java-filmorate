package ru.yandex.practicum.filmorate.storage.genre.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("Тесты GenreStorage")
class GenreStorageTests {
    private final GenreStorage genreStorage;

    @Test
    @DisplayName("Получение списка жанров")
    void getGenresTest() {
        List<Genre> genres = genreStorage.getGenres();
        assertEquals(6, genres.size(), "Не верное количество жанров");
    }

    @Test
    @DisplayName("Получение конкретного жанра")
    void getGenreTest() {
        Genre genre = genreStorage.getGenre(3);
        assertEquals("Мультфильм", genre.getName(), "Не верный жанр");
    }

    @Test
    @DisplayName("Не существующий жанр")
    void getWrongGenreTest() {
        Genre genre = genreStorage.getGenre(10);
        assertNull(genre, "Не верная реакция на жанр");
    }
}
