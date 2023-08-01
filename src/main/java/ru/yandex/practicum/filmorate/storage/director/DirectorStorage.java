package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {

    List<Director> getDirectors();

    Director getDirector(long id);

    Director createDirector(Director director);

    Director updateDirector(Director director);

    Director deleteDirector(long id);

    boolean isExists(long id);
}
