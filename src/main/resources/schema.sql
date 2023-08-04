CREATE TABLE IF NOT EXISTS USERS
(
    user_id int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email varchar(150) NOT NULL,
    login varchar(100) NOT NULL,
    name varchar(150),
    birthday timestamp NOT NULL
);

CREATE TABLE IF NOT EXISTS MPAS
(
    mpa_id int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name varchar(20) NOT NULL,
    description varchar(200)
);

CREATE TABLE IF NOT EXISTS FILMS
(
    film_id int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    mpa_id int NOT NULL,
    name varchar(150) NOT NULL,
    description varchar(200),
    releaseDate timestamp NOT NULL,
    duration int,
    CONSTRAINT fk_mpa FOREIGN KEY(mpa_id) REFERENCES MPAS(mpa_id) ON DELETE CASCADE,
    CONSTRAINT ck_duration CHECK (duration > 0)
);

CREATE TABLE IF NOT EXISTS GENRES
(
    genre_id int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name varchar(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS DIRECTORS
(
    director_id int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name        varchar(250) NOT NULL
);

CREATE TABLE IF NOT EXISTS FILMS_DIRECTORS
(
    film_id     int NOT NULL,
    director_id int NOT NULL,
    PRIMARY KEY (film_id, director_id),
    CONSTRAINT fk_film_d FOREIGN KEY (film_id) REFERENCES FILMS (film_id),
    CONSTRAINT fk_director FOREIGN KEY (director_id) REFERENCES DIRECTORS (director_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS FILMS_GENRES
(
    film_id int NOT NULL,
    genre_id int NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    CONSTRAINT fk_film FOREIGN KEY(film_id) REFERENCES FILMS(film_id) ON DELETE CASCADE ,
    CONSTRAINT fk_genre FOREIGN KEY(genre_id) REFERENCES GENRES(genre_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS FRIENDSHIPS
(
    user_one_id int NOT NULL,
    user_two_id int NOT NULL,
    status int NOT NULL,
    PRIMARY KEY (user_one_id, user_two_id),
    CONSTRAINT fk_user_one FOREIGN KEY(user_one_id) REFERENCES USERS(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_two FOREIGN KEY(user_two_id) REFERENCES USERS(user_id) ON DELETE CASCADE,
    CONSTRAINT ck_status CHECK (status IN (0,1))
);

CREATE TABLE IF NOT EXISTS FILMS_LIKES
(
    film_id int NOT NULL,
    user_id int NOT NULL,
    PRIMARY KEY (film_id, user_id),
    CONSTRAINT fk_film_id FOREIGN KEY(film_id) REFERENCES FILMS(film_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_id FOREIGN KEY(user_id) REFERENCES USERS(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS REVIEWS
(
    review_id int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    content varchar(255),
    positive boolean,
    user_id int NOT NULL,
    film_id int NOT NULL
);

CREATE TABLE IF NOT EXISTS USEFULS
(
    review_id int NOT NULL,
    user_id int NOT NULL,
    useful_status boolean,
    PRIMARY KEY (review_id, user_id),
    CONSTRAINT fk_review_id FOREIGN KEY(review_id) REFERENCES reviews(review_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_d FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS EVENTS
(
    event_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id int not null,
    entity_id int NOT NULL,
    event_type VARCHAR(10) NOT NULL,
    event_operation VARCHAR(10) NOT NULL ,
    timestamp long NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE
);



