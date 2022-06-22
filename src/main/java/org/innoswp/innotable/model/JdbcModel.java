package org.innoswp.innotable.model;

import lombok.extern.slf4j.Slf4j;
import org.innoswp.innotable.model.event.CalendarEvent;
import org.innoswp.innotable.model.event.Location;
import org.innoswp.innotable.model.user.Group;
import org.innoswp.innotable.model.user.Role;
import org.innoswp.innotable.model.user.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

@Slf4j
public class JdbcModel implements Model {

    private static final Properties properties = new Properties();

    public JdbcModel() throws SQLException {
        var setupQuery = """                               
                CREATE TABLE IF NOT EXISTS "group"(
                    id SERIAL PRIMARY KEY,
                    label VARCHAR(63) UNIQUE NOT NULL
                );

                CREATE TABLE IF NOT EXISTS role(
                    id SERIAL PRIMARY KEY,
                    label VARCHAR(63) UNIQUE NOT NULL
                );

                CREATE TABLE IF NOT EXISTS "user"(
                    id SERIAL PRIMARY KEY,
                    email VARCHAR(63) UNIQUE NOT NULL,
                    password VARCHAR(63) NOT NULL,
                    role_id INT REFERENCES role (id)
                );

                CREATE TABLE IF NOT EXISTS event(
                    id SERIAL PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    description TEXT,
                    location VARCHAR(255),
                    start_dt TIMESTAMP NOT NULL,
                    end_dt TIMESTAMP NOT NULL,
                    group_id INT REFERENCES "group"(id) ON DELETE CASCADE
                );

                CREATE TABLE IF NOT EXISTS user_group(
                    user_id INT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
                    group_id INT NOT NULL REFERENCES "group"(id) ON DELETE CASCADE
                );
                """;

        try (
                var connection = open();
                var statement = connection.createStatement()
        ) {
            statement.execute(setupQuery);
            log.trace("Created JdbcModel instance");
        }
    }

    static {
        try {
            log.info("Loading database_config.properties for JdbcModel");

            properties.load(new BufferedReader(new FileReader("src/main/resources/database_config.properties")));
        } catch (IOException e) {
            log.error("Cannot read database_config.properties file!");
            throw new RuntimeException("Cannot read database_config.properties file!");
        }
    }

    private static Connection open() {
        try {
            return DriverManager.getConnection(
                    properties.getProperty("db.url"),
                    properties.getProperty("db.username"),
                    properties.getProperty("db.password")
            );

        } catch (SQLException e) {
            log.error("Cannot establish connection with JdbcModel");
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Group> getGroups() throws SQLException {
        var query = """
                SELECT label
                FROM "group"
                """;

        var response = new LinkedList<Group>();

        try (
                var connection = open();
                var statement = connection.createStatement()
        ) {
            var resultSet = statement.executeQuery(query);

            while (resultSet.next())
                response.add(new Group(resultSet.getObject("label", String.class)));
        }

        log.trace("Loaded groups list");
        return response;
    }

    @Override
    public void saveGroup(Group group) throws SQLException {
        var query = """
                INSERT INTO "group" (label)
                VALUES (?)
                """;
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, group.label());
            preparedStatement.execute();
        }

        log.trace("Saved group " + group.label());
    }

    @Override
    public Group dropGroup(Group group) throws SQLException {
        var query = """
                DELETE FROM "group"
                WHERE label = ?
                RETURNING label
                """;
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, group.label());
            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                log.trace("Dropped group " + group.label());
                return new Group(resultSet.getObject("label", String.class));
            }
        }

        log.warn("In dropGroup query group " + group.label() + " does not exist, returning null!");
        return null;
    }

    @Override
    public List<Role> getRoles() throws SQLException {
        var query = """
                SELECT label
                FROM role
                """;
        List<Role> result = new LinkedList<>();
        try (
                var connection = open();
                var statement = connection.createStatement()
        ) {
            var resultSet = statement.executeQuery(query);

            while (resultSet.next())
                result.add(new Role(resultSet.getObject("label", String.class)));
        }

        log.trace("Loaded roles list");

        return result;
    }

    @Override
    public void saveRole(Role role) throws SQLException {
        var query = """
                INSERT INTO role (label)
                VALUES (?)
                """;
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, role.label());
            preparedStatement.execute();
        }

        log.trace("Saved role " + role.label());
    }

    @Override
    public void saveEvent(CalendarEvent event, Group group) throws SQLException {
        var query = """
                INSERT INTO event(title, description, location, start_dt, end_dt, group_id)
                VALUES (?, ?, ?, ?, ?, (SELECT id FROM "group" WHERE label = ?))
                """;
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, event.title());
            preparedStatement.setString(2, event.description());
            preparedStatement.setString(3, event.location().label());

            preparedStatement.setTimestamp(4, Timestamp.valueOf(event.startTime()));
            preparedStatement.setTimestamp(5, Timestamp.valueOf(event.endTime()));

            preparedStatement.setString(6, group.label());

            preparedStatement.execute();

            log.trace("Saved event for group " + group.label());
        }
    }

    @Override
    public Pair<Group, CalendarEvent> dropEvent(String title) throws SQLException {
        var query = """
                DELETE FROM event
                WHERE title = ?
                RETURNING title, description, location, start_dt, end_dt,
                    (SELECT label FROM "group" WHERE "group".id =
                        (SELECT group_id FROM event WHERE title = ?)) grp
                """;
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, title);

            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                log.trace("Dropped event by its title: " + title);

                return new Pair<>(
                        new Group(resultSet.getObject("grp", String.class)),

                        new CalendarEvent(
                                resultSet.getObject("title", String.class),
                                resultSet.getObject("description", String.class),
                                new Location(resultSet.getObject("location", String.class)),
                                resultSet.getObject("start_dt", LocalDateTime.class),
                                resultSet.getObject("end_dt", LocalDateTime.class)
                        )
                );
            }
        }

        log.warn("In dropEvent query event with title (" + title + ") does not exist, returning null!");
        return null;
    }

    @Override
    public List<Pair<Group, CalendarEvent>> loadEventsByUser(User user) throws Exception {
        var query = """
                SELECT title, description, location, start_dt, end_dt,
                    (SELECT label FROM "group" WHERE event.group_id = id) "group" FROM event
                WHERE (SELECT label FROM "group" WHERE event.group_id = id) = ?
                """;
        var events = new LinkedList<Pair<Group, CalendarEvent>>();
        try (var connection = open()) {
            for (var group : user.group()) {
                try (var preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, group.label());
                    formEventList(events, preparedStatement.executeQuery());
                }
            }
        }

        log.trace("Loaded events list by the given user");
        return events;
    }

    @Override
    public List<Pair<Group, CalendarEvent>> loadEventsByGroup(Group group) throws Exception {
        var query = """
                SELECT title, description, location, start_dt, end_dt,
                    (SELECT label FROM "group" WHERE event.group_id = id) "group" FROM event
                WHERE (SELECT label FROM "group" WHERE event.group_id = id) = ?
                """;
        var events = new LinkedList<Pair<Group, CalendarEvent>>();
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, group.label());
            formEventList(events, preparedStatement.executeQuery());
        }

        log.trace("Loaded events list by the given group: " + group.label());
        return events;
    }

    @Override
    public List<Pair<Group, CalendarEvent>> loadEventsByDay(LocalDate date) throws Exception {

        log.trace("Loaded events list by the given day: " + date);
        return getEventsFromInterval(
                date.atStartOfDay(),
                date.atTime(23, 59, 59, 999999999)
        );
    }

    @Override
    public List<Pair<Group, CalendarEvent>> loadEventsIn(LocalDateTime start, LocalDateTime end)
            throws Exception {

        log.trace("Loaded events list by the given duration: " + start + " - " + end);
        return getEventsFromInterval(start, end);
    }

    @Override
    public List<Pair<Group, CalendarEvent>> loadEventsDuring(LocalDateTime time) throws Exception {
        log.trace("Loaded actual events list by the given time: " + time);
        return getEventsFromInterval(time, time);
    }

    private List<Pair<Group, CalendarEvent>> getEventsFromInterval(LocalDateTime start, LocalDateTime end)
            throws SQLException {
        var query = """
                SELECT title, description, location, start_dt, end_dt,
                    (SELECT label FROM "group" WHERE event.group_id = id) "group" FROM event
                WHERE start_dt <= ? AND ? <= end_dt;
                """;
        var events = new LinkedList<Pair<Group, CalendarEvent>>();
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(end));
            preparedStatement.setTimestamp(2, Timestamp.valueOf(start));

            var resultSet = preparedStatement.executeQuery();
            formEventList(events, resultSet);
        }

        return events;
    }

    @Override
    public LinkedList<Pair<Group, CalendarEvent>> loadEventsByTitle(String title) throws SQLException {
        var query = """
                SELECT title, description, location, start_dt, end_dt,
                    (SELECT label FROM "group" WHERE event.group_id = id) "group" FROM event
                WHERE title = ?;
                """;
        var events = new LinkedList<Pair<Group, CalendarEvent>>();
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, title);
            var resultSet = preparedStatement.executeQuery();
            formEventList(events, resultSet);
        }

        log.trace("Loaded events list by the given title: " + title);
        return events;
    }

    private void formEventList(LinkedList<Pair<Group, CalendarEvent>> events, ResultSet resultSet)
            throws SQLException {
        while (resultSet.next())
            events.add(new Pair<>(
                            new Group(resultSet.getObject("group", String.class)),
                            new CalendarEvent(
                                    resultSet.getObject("title", String.class),
                                    resultSet.getObject("description", String.class),
                                    new Location(resultSet.getObject("location", String.class)),
                                    resultSet.getObject("start_dt", LocalDateTime.class),
                                    resultSet.getObject("end_dt", LocalDateTime.class)
                            )
                    )
            );
    }

    @Override
    public List<Pair<Group, CalendarEvent>> loadAllEvents() throws SQLException {
        var query = """
                SELECT title, description, location, start_dt, end_dt,
                    (SELECT label FROM "group" WHERE event.group_id = id) "group" FROM event
                ORDER BY start_dt;
                """;

        var events = new LinkedList<Pair<Group, CalendarEvent>>();
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            var resultSet = preparedStatement.executeQuery();
            formEventList(events, resultSet);
        }

        log.trace("Loaded all events list");
        return events;
    }

    @Override
    public void addUser(User user) throws SQLException {
        var firstQuery = """
                INSERT INTO "user"(email, password, role_id)
                VALUES (?, ?, (SELECT id FROM "role" WHERE label = ?));
                                
                                
                """;
        var secondQuery = """
                INSERT INTO user_group(user_id, group_id)
                VALUES ((SELECT id FROM "user" WHERE email = ?), (SELECT id FROM "group" WHERE label = ?))
                """;
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(firstQuery)
        ) {
            preparedStatement.setString(1, user.email());
            preparedStatement.setString(2, user.password());
            preparedStatement.setString(3, user.role().label());

            preparedStatement.execute();

            for (Group group : user.group())
                try (var pStatement = connection.prepareStatement(secondQuery)) {
                    pStatement.setString(1, user.email());
                    pStatement.setString(2, group.label());
                    pStatement.execute();
                }
        }

        log.trace("Saved user");
    }

    @Override
    public void assignUsersToGroup(List<User> users, Group group) throws SQLException {
        var query = """
                INSERT INTO user_group(user_id, group_id)
                VALUES ((SELECT id FROM "user" WHERE email = ?), (SELECT id FROM "group" WHERE label = ?))
                 """;

        try (var connection = open()) {
            for (User user : users) {
                user.group().add(group);

                try (var pStatement = connection.prepareStatement(query)) {
                    pStatement.setString(1, user.email());
                    pStatement.setString(2, group.label());
                    pStatement.execute();
                }
            }
        }

        log.trace("Assigned list of users to the group: " + group.label());
    }

    @Override
    public User dropUser(String email) throws SQLException {
        var query = """
                DELETE FROM "user"
                WHERE email = ?
                RETURNING email, password, (SELECT label FROM "role" WHERE "role".id =
                    (SELECT role_id FROM "user" WHERE email = ?)) rl
                """;

        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, email);

            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                log.trace("Dropped user");
                return new User(
                        resultSet.getObject("email", String.class),
                        resultSet.getObject("password", String.class),
                        new LinkedList<>(),
                        new Role(resultSet.getObject("rl", String.class))
                );
            }
        }

        log.warn("In dropUser query user with email: " + email + " does not exist, returning null!");
        return null;
    }

    @Override
    public List<User> getUsers() throws SQLException {
        var query = """
                SELECT email, password,
                    (SELECT label FROM "role" WHERE "role".id = "user".role_id) rl FROM "user";
                """;

        var users = new LinkedList<User>();

        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            var resultSet = preparedStatement.executeQuery();
            getUsersFromResultSet(users, connection, resultSet);
        }

        log.trace("Loaded users");
        return users;
    }

    @Override
    public List<User> getUsersByGroup(Group group) throws SQLException {
        var query = """
                SELECT email, password, (SELECT label FROM "role" WHERE "role".id = "user".role_id) rl FROM "user"
                                         WHERE id IN (SELECT user_id FROM user_group
                                                            WHERE group_id = (SELECT id FROM "group"
                                                                              WHERE label = ?));
                """;
        List<User> users = new LinkedList<>();
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, group.label());
            var resultSet = preparedStatement.executeQuery();
            getUsersFromResultSet(users, connection, resultSet);
        }

        log.trace("Loaded users by the given group: " + group.label());
        return users;
    }

    private void getUsersFromResultSet(List<User> users, Connection connection, ResultSet resultSet)
            throws SQLException {
        var query = """
                SELECT label FROM "group"
                WHERE id IN (SELECT group_id FROM user_group
                            WHERE user_id = (SELECT id FROM "user"
                                            WHERE email = ?))
                """;

        while (resultSet.next()) {

            var user = new User(
                    resultSet.getObject("email", String.class),
                    resultSet.getObject("password", String.class),
                    new LinkedList<>(),
                    new Role(resultSet.getObject("rl", String.class))
            );

            try (var pStatement = connection.prepareStatement(query)) {
                pStatement.setString(1, user.email());
                var resultSet1 = pStatement.executeQuery();
                while (resultSet1.next())
                    user.group().add(new Group(resultSet1.getObject("label", String.class)));
            }

            users.add(user);
        }
    }

    @Override
    public List<User> getUsersByRole(Role role) throws SQLException {
        var query = """
                SELECT email, password, (SELECT label FROM "role" WHERE "role".id = "user".role_id) rl FROM "user"
                                         WHERE role_id = (SELECT id FROM role
                                                          WHERE label = ?);
                """;
        var users = new LinkedList<User>();
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, role.label());
            var resultSet = preparedStatement.executeQuery();
            getUsersFromResultSet(users, connection, resultSet);
        }

        log.trace("Loaded users by the given role: " + role.label());
        return users;
    }
}
