DELETE
FROM FRIENDSHIPS;
DELETE
FROM FILMS_LIKES;
DELETE
FROM FILMS_GENRES;
DELETE
FROM FILMS_DIRECTORS;
DELETE
FROM DIRECTORS;
DELETE
FROM FILMS;
DELETE
FROM USERS;
DELETE
FROM REVIEWS;
DELETE
FROM USEFULS;
DELETE
FROM EVENTS;

ALTER TABLE FILMS
    ALTER COLUMN film_id RESTART WITH 1;
ALTER TABLE USERS
    ALTER COLUMN user_id RESTART WITH 1;
ALTER TABLE DIRECTORS
    ALTER COLUMN director_id RESTART WITH 1;
ALTER TABLE REVIEWS
    ALTER COLUMN review_id RESTART WITH 1;
ALTER TABLE EVENTS
    ALTER COLUMN event_id RESTART WITH 1;

MERGE INTO GENRES (genre_id, name)
KEY(genre_id)
VALUES (1, 'Комедия'),
       (2, 'Драма'),
       (3, 'Мультфильм'),
       (4, 'Триллер'),
       (5, 'Документальный'),
       (6, 'Боевик');
MERGE INTO MPAS (mpa_id, name, description)
KEY(mpa_id)
VALUES (1, 'G','у фильма нет возрастных ограничений'),
       (2, 'PG','детям рекомендуется смотреть фильм с родителями'),
       (3, 'PG-13','детям до 13 лет просмотр не желателен'),
       (4, 'R','лицам до 17 лет просматривать фильм можно только в присутствии взрослого'),
       (5, 'NC-17','лицам до 18 лет просмотр запрещён');
