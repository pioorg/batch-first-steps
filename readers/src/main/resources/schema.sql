DROP TABLE IF EXISTS characters;
CREATE TABLE characters (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR,
    last_name VARCHAR,
    email VARCHAR
);