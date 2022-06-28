CREATE TABLE IF NOT EXISTS "group"
(
    id    SERIAL PRIMARY KEY,
    label VARCHAR(63) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS role
(
    id    SERIAL PRIMARY KEY,
    label VARCHAR(63) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS "user"
(
    id       SERIAL PRIMARY KEY,
    email    VARCHAR(63) UNIQUE NOT NULL,
    password VARCHAR(63)        NOT NULL,
    role_id  INT REFERENCES role (id)
);

CREATE TABLE IF NOT EXISTS event
(
    id          SERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    location    VARCHAR(255),
    start_dt    TIMESTAMP    NOT NULL,
    end_dt      TIMESTAMP    NOT NULL,
    group_id    INT REFERENCES "group" (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_group
(
    user_id  INT NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    group_id INT NOT NULL REFERENCES "group" (id) ON DELETE CASCADE
);

