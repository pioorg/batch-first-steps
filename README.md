## Preparation
In order to prepare for the workshop, please follow the steps below.

* Clone this repository

  It is recommended do not download the sources in ZIP file or fork this repository, but *clone* it, e.g. by `git clone git@github.com:pioorg/batch-first-steps.git` or `gh repo clone pioorg/batch-first-steps`, or `git clone https://github.com/pioorg/batch-first-steps.git`.

  Then follow with `git pull --all`, please.

* Build and run sample application to ensure you have Java™ installed, version 11 or above. Please navigate in CLI (terminal) to the project directory and call `mvnw spring-boot:run -pl hello`.

  If you do not see a big 'HELLO BATCH', it most probably means your Java installation is missing, corrupted or below Java 11. You can install Java using https://sdkman.io/ or https://adoptopenjdk.net/.

* Import this project into your favourite IDE or set up your favourite text editor. (During the workshop the trainer will use Intellij IDEA™). The project consists of several Maven submodules, therefore the parent project can be imported, there should be no need to import submodules one by one. 

* You should be able to run the H2 database (which will be used during the workshop) by calling `mvnw -N exec:exec@startH2`. To stop it, call `mvnw -N exec:exec@stopH2` in another terminal window.

* After running the H2 DB, the WebConsole should be available as http://localhost:8082/ (JDBC URL: `jdbc:h2:tcp://localhost:9092/first_steps`, User: `sa`, no password).

  Terminal for H2 should be available when running `mvn -N exec:exec@consoleH2`.

* To remove everything from the H2 DB, please run `DROP ALL OBJECTS;`.