CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR NOT NULL,
    email       VARCHAR NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS items (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR NOT NULL,
    description   VARCHAR NOT NULL,
    is_available  BOOLEAN NOT NULL,
    owner_id      BIGINT NOT NULL REFERENCES users(id),
    request_id    BIGINT
);

CREATE TABLE IF NOT EXISTS bookings (
    id          BIGSERIAL PRIMARY KEY,
    start_date  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    item_id     BIGINT NOT NULL REFERENCES items(id),
    booker_id   BIGINT NOT NULL REFERENCES users(id),
    status      VARCHAR NOT NULL,
    CONSTRAINT bookings_time_check CHECK (end_date > start_date)
);

CREATE TABLE IF NOT EXISTS comments (
    id        BIGSERIAL PRIMARY KEY,
    text      VARCHAR NOT NULL,
    item_id   BIGINT NOT NULL REFERENCES items(id),
    author_id BIGINT NOT NULL REFERENCES users(id),
    created   TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
