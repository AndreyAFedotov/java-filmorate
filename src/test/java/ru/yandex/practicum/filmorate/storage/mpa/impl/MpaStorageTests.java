package ru.yandex.practicum.filmorate.storage.mpa.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("Тесты MpaStorage")
public class MpaStorageTests {
    private final MpaStorage mpaStorage;

    @Test
    @DisplayName("Получение списка MPA")
    void getMpasTest() {
        List<Mpa> mpas = mpaStorage.getMpas();
        assertEquals(5, mpas.size(), "Не верное количество MPA");
    }

    @Test
    @DisplayName("Получение конкретного MPA")
    void getMpaTest() {
        Mpa mpa = mpaStorage.getMpa(2);
        assertEquals("PG", mpa.getName(), "Не верный MPA");
    }

    @Test
    @DisplayName("Не существующий MPA")
    void getWrongMpaTest() {
        Mpa mpa = mpaStorage.getMpa(10);
        assertNull(mpa, "Не верная реакция на MPA");
    }
}
