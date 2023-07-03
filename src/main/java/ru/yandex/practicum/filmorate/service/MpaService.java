package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    @Autowired
    public MpaService(@Qualifier("DBMpaStorage") MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public List<Mpa> getMpas() {
        List<Mpa> mpas = mpaStorage.getMpas();
        if (mpas.isEmpty()) {
            throw new NotFoundException("MPA отсутствуют");
        }
        return mpas;
    }

    public Mpa getMpa(long id) {
        Mpa mpa = mpaStorage.getMpa(id);
        if (mpa == null) {
            throw new NotFoundException("Неизвестный MPA: " + id);
        }
        return mpa;
    }
}
