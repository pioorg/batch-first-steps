DROP TABLE IF EXISTS TEMPERATURE_READING;
DROP TABLE IF EXISTS WIND_READING;
DROP TABLE IF EXISTS STATION;
CREATE TABLE STATION
(
    id         UUID PRIMARY KEY,
    temp_unit  VARCHAR NOT NULL,
    speed_unit VARCHAR NOT NULL
);
CREATE TABLE TEMPERATURE_READING
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    station_id  UUID                     NOT NULL,
    taken       TIMESTAMP WITH TIME ZONE NOT NULL,
    temperature DOUBLE                   NOT NULL,
    FOREIGN KEY (station_id) REFERENCES station (id)
);
CREATE TABLE WIND_READING
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    station_id UUID                     NOT NULL,
    taken      TIMESTAMP WITH TIME ZONE NOT NULL,
    speed      DOUBLE                   NOT NULL,
    direction  INT                      NOT NULL,
    FOREIGN KEY (station_id) REFERENCES station (id)
);