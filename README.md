# InnoTable+
A web application designed for university administration for creating events for students. Combines events from LMS Moodle, Outlook and the Web-platform, forming a common calendar in Outlook.

## Agenda
- [Technologies used](#technologies-used)
- [Installation](#installation)
- [Project architecture](#project-architecture)
- [Contributing](#contributing)
- [Project team](#project-team)

## Technologies used
- OpenJDK 17 and JS
- Lombok annotations
- jQuery 3.6.0
- Log4j2 (slf4j impl)
- Maven build system
- Springboot 2.7.0
- PostgreSQL and JDBC
- Exchange Web Services (EWS)

## Installation

1. Install OpenJDK 17
2. Install [Apache Tomcat 10](https://tomcat.apache.org/download-10.cgi)
3. Install [PostgreSQL 14](https://www.postgresql.org/download/)
4. Create an empty database for this project, run:
```shell
$ su - postgres # Switching to postgres user
$ psql # Calling Query console
> CREATE DATABASE innotable # Create empty database
> \q
```
4. Download and extract [project release version](https://github.com/InnoSWP/innoTable/releases/)
5. Configure `config/application.properties` file: replace CAPS words to your server data:
```properties
server.port=8080
spring.thymeleaf.cache=false
server.error.whitelabel.enabled=false
spring.datasource.url=DATABASE_URL
spring.datasource.username=DATABASE_ADMIN_USERNAME
spring.datasource.password=DATABASE_ADMIN_PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL10Dialect
spring.sql.init.mode=always
spring.sql.init.username=DATABASE_ADMIN_USERNAME
spring.sql.init.password=DATABASE_ADMIN_PASSWORD
spring.sql.init.schema-locations=classpath*:database/schema.sql
spring.sql.init.data-locations=classpath*:database/data.sql
spring.sql.init.continue-on-error=true
spring.jpa.hibernate.ddl-auto=none
ews.service.url=https://YOUR_OUTLOOK_SITE/ews/exchange.asmx
ews.service.email=EXCHANGE_ADMIN_EMAIL
ews.service.password=EXCHANGE_ADMIN_PASSWORD
```
6. Run JAR file:
```shell
$ java -jar innoTable-exec.jar
```

## Project architecture

### Use-case diagram
![Use-case diagram](https://i.imgur.com/wPP2NUc.png)

### Static architecture diagram
![Static view on architecture](https://i.imgur.com/v90RhEd.png)

### Dynamic architecture diagram
![Dynamic view on architecture](https://i.imgur.com/SFmUJfA.png)



## Contributing
If you have questions and suggestions on how to improve our application, write to our Leads by mail: g.budnik@innoplis.university, d.alekhin@innopolis.university

## FAQ
1. If your event is not displayed in your calendar, reload it before the corresponding event appears.
2. If you see unfamiliar events in the Outlook calendar, pay attention to whether you are logged out of the adding events account. Perhaps they are scammers.

### Why did we develop this project?
The project was developed as part of one of the educational subjects of the summer semester 2022 of Innopolis University. The main idea is teamwork on the project and this application is the result of the work of students who deserved A in the subject :)

## Project team

To contact a developer, clearly define your survey, define the question areas (development area) and email to the appropriate team member:

- [Alexey Potyomkin](a.potyomkink@innopolis.university) - Mentor, TA
- [Budnik Georgii](g.budnik@innopolis.university) — Team Leader, Product manager
- [Dmitrii Alekhin](d.alekhin@innopolis.university) - Tech Leader, Back-end developer
- [Kirill Batyshchev](k.batyshchev@innopolis.university) - Back-end developer, Quality Assurance
- [Guzel Zakirova](g.zakirova@innopolis.university) - Front-end developer
- [Anastasia Palashkina](a.palashkina@innopolis.university) - Back-end developer, Quality Assurance
