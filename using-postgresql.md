
### If you would like to run PostgreSQL database using Docker, that is possible too. Here is a little how-to. For more details please visit  https://hub.docker.com/_/postgres

This will create and run PostgreSQL 13.2 container as daemon using `postgres_13` volume.
```
docker container run -d --name=postgres_13.2 -p 5432:5432 -e POSTGRES_PASSWORD=p@ssw0rd -v postgres_13:/var/lib/postgresql/data postgres:13.2
```

This will create database `first_steps` owned by user `batch` with password `b4tch`
```
docker exec -it --user postgres postgres_13.2 bash -c "createuser batch && createdb --encoding=UTF-8 --owner=batch first_steps && psql -c \"ALTER ROLE batch WITH ENCRYPTED PASSWORD 'b4tch'; \" "
```

To use the DB in Spring Batch, please add dependency to PostgreSQL JDBC driver to `pom.xml`:
```
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>
```
and paste into `application.properties`:
```
spring.datasource.url=jdbc:postgresql://localhost:5432/first_steps
spring.datasource.driverClassName=org.postgresql.Driver
spring.datasource.username=batch
spring.datasource.password=b4tch
```

This will drop and recreate `first_steps` database
```
docker exec -it --user postgres postgres_13.2 bash -c "dropdb first_steps && createdb --encoding=UTF-8 --owner=batch first_steps"
```

This will stop the PostgreSQL container
```
docker container stop postgres_13.2
```

This will start the PostgreSQL container
```
docker container start postgres_13.2
```

This will connect to the DB as dbadmin
```
docker exec -it --user postgres postgres_13.2 psql
```

This will connect to the `first_steps` DB as `batch` user
```
docker exec -it postgres_13.2 psql -U batch -d first_steps
```

Should you need to stop the container and purge the container, the image and the volume, please run
```
docker container stop postgres_13.2
docker container rm postgres_13.2
docker volume rm postgres_13
docker image rm postgres:13.2
```
