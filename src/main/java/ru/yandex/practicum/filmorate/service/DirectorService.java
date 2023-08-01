package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
public class DirectorService {
    private final DirectorStorage directorStorage;

    @Autowired
    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public List<Director> getDirectors() {
        return directorStorage.getDirectors();
    }

    public Director getDirector(long id) {
        checkDirectorIsExist(id);
        return directorStorage.getDirector(id);
    }

    public Director createDirector(Director director) {
        return directorStorage.createDirector(director);
    }

    public Director updateDirector(Director director) {
        checkDirectorIsExist(director.getId());
        return directorStorage.updateDirector(director);
    }

    public Director deleteDirector(long id) {
        checkDirectorIsExist(id);
        return directorStorage.deleteDirector(id);
    }

    public void checkDirectorIsExist(long id) {
        if (!directorStorage.isExists(id)) {
            throw new NotFoundException("Такого режиссёра нет в базе: " + id);
        }
    }

}
