package org.innoswp.innotable.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JdbcModelTest {

    private static final Properties properties = new Properties();

    private final Model model = new JdbcModel();

    static {
        try {
            properties.load(new BufferedReader(new FileReader("src/test/resources/application.properties")));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private final String DB_URL = properties.getProperty("db.url");

    private final String DB_USERNAME = properties.getProperty("db.username");

    private final String DB_PASSWORD = properties.getProperty("db.password");

    private Connection open() {
        try {
            return DriverManager.getConnection(
                    DB_URL,
                    DB_USERNAME,
                    DB_PASSWORD
            );

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void execute(String query) throws SQLException {
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.execute();
        }
    }

    ResultSet executeQuery(String query) throws SQLException {
        try (
                var connection = open();
                var statement = connection.createStatement()
        ) {
            return statement.executeQuery(query);
        }
    }

    void clear() throws SQLException {
        execute("""
                DELETE FROM "user";
                DELETE FROM "group";
                DELETE FROM event;
                DELETE FROM role;
                """);
    }

    void fill() throws SQLException {
        execute("""
                INSERT INTO role(label)
                VALUES ('staff'),
                       ('student');
                INSERT INTO "group"(label)
                VALUES ('bachelor'),
                       ('master'),
                       ('phd');
                INSERT INTO "user"(email, password, role_id)
                VALUES ('k.batyshchev@innopolis.university', 'psswrd', (SELECT id FROM role WHERE label = 'student')),
                ('d.alekhin@innopolis.university', 'psswrd1', (SELECT id FROM role WHERE label = 'student')),
                ('g.budnik@innopolis.university', 'psswrd2', (SELECT id FROM role WHERE label = 'student')),
                ('a.palashkina@innopolis.university', 'psswrd3', (SELECT id FROM role WHERE label = 'student')),
                ('guz.zakirova@innopolis.university', 'psswrd4', (SELECT id FROM role WHERE label = 'student')),
                ('a.potemkin@innopolis.university', 'psswrd5', (SELECT id FROM role WHERE label = 'student')),
                ('a.elbatanony@innopolis.university', 'psswrd6', (SELECT id FROM role WHERE label = 'student')),
                ('v.shelkovnikov@innopolis.university', 'psswrd7', (SELECT id FROM role WHERE label = 'student')),
                ('z.kholmatova@innopolis.university', 'psswr8', (SELECT id FROM role WHERE label = 'staff'));
                INSERT INTO user_group(user_id, group_id)
                VALUES ((SELECT id FROM "user" WHERE email LIKE 'k.bat%'), (SELECT id FROM "group" WHERE label = 'bachelor')),
                ((SELECT id FROM "user" WHERE email LIKE 'd.al%'), (SELECT id FROM "group" WHERE label = 'bachelor')),
                ((SELECT id FROM "user" WHERE email LIKE 'g.bud%'), (SELECT id FROM "group" WHERE label = 'bachelor')),
                ((SELECT id FROM "user" WHERE email LIKE 'a.pal%'), (SELECT id FROM "group" WHERE label = 'bachelor')),
                ((SELECT id FROM "user" WHERE email LIKE 'guz.z%'), (SELECT id FROM "group" WHERE label = 'bachelor')),
                ((SELECT id FROM "user" WHERE email LIKE 'a.pot%'), (SELECT id FROM "group" WHERE label = 'master')),
                ((SELECT id FROM "user" WHERE email LIKE 'a.el%'), (SELECT id FROM "group" WHERE label = 'master')),
                ((SELECT id FROM "user" WHERE email LIKE 'v.sh%'), (SELECT id FROM "group" WHERE label = 'master')),
                ((SELECT id FROM "user" WHERE email LIKE 'z.kh%'), (SELECT id FROM "group" WHERE label = 'phd'));
                INSERT INTO event(title, description, location, start_dt, end_dt, group_id)
                VALUES ('DROP', 'Drop of bachelors', 'IU', now(), now(), (SELECT id FROM "group" WHERE label = 'bachelor'))
                """);
    }

    @Test
    void getGroups() throws SQLException {
        clear();
        assertEquals(0, model.getGroups().size());
        fill();
        String[] toCompareArr = {"bachelor", "master", "phd"};
        List<String> toCompare = Arrays.stream(toCompareArr).toList();
        assertTrue(toCompare.containsAll(model.getGroups()));
        clear();
    }

    @Test
    void dropGroup() throws SQLException {
        clear();
        fill();
        String[] toCompareArr = {"master", "phd"};
        List<String> toCompare = new ArrayList<>(Arrays.stream(toCompareArr).toList());
        model.dropGroup("bachelor");
        assertEquals(model.getGroups(), toCompare);
        toCompare.remove("master");
        model.dropGroup("master");
        assertEquals(model.getGroups(), toCompare);
        model.dropGroup("phd");
        assertEquals(0, model.getGroups().size());
    }

    @Test
    void getRoles() throws SQLException {
        clear();
        fill();
        String[] toCompare = {"staff", "student"};
        assertEquals(Arrays.stream(toCompare).toList(), model.getRoles());
    }

    @Test
    void saveRole() throws SQLException {
        clear();
        fill();
        String[] toCompare = {"staff", "student", "new role"};
        model.saveRole("new role");
        assertEquals(Arrays.stream(toCompare).toList(), model.getRoles());
    }

    @Test
    void saveEvent() {
    }

    @Test
    void dropEvent() {
    }

    @Test
    void loadEventsByUser() {
    }

    @Test
    void loadEventsByGroup() {
    }

    @Test
    void loadEventsByDay() {
    }

    @Test
    void loadEventsIn() {
    }

    @Test
    void loadEventsDuring() {
    }

    @Test
    void loadEventsByTitle() {
    }

    @Test
    void loadAllEvents() {
    }

    @Test
    void getUsers() throws SQLException {
        clear();
        fill();
        var result = model.getUsers();
        assertEquals(9, result.size());
        assertEquals(result.get(0), new User("k.batyshchev@innopolis.university", "psswrd", Arrays.stream(new String[]{"bachelor"}).toList(), "student"));
        assertEquals(result.get(1), new User("d.alekhin@innopolis.university", "psswrd1", Arrays.stream(new String[]{"bachelor"}).toList(), "student"));
        assertEquals(result.get(2), new User("g.budnik@innopolis.university", "psswrd2", Arrays.stream(new String[]{"bachelor"}).toList(), "student"));
        assertEquals(result.get(3), new User("a.palashkina@innopolis.university", "psswrd3", Arrays.stream(new String[]{"bachelor"}).toList(), "student"));
        assertEquals(result.get(4), new User("guz.zakirova@innopolis.university", "psswrd4", Arrays.stream(new String[]{"bachelor"}).toList(), "student"));
        assertEquals(result.get(5), new User("a.potemkin@innopolis.university", "psswrd5", Arrays.stream(new String[]{"master"}).toList(), "student"));
        assertEquals(result.get(6), new User("a.elbatanony@innopolis.university", "psswrd6", Arrays.stream(new String[]{"master"}).toList(), "student"));
        assertEquals(result.get(7), new User("v.shelkovnikov@innopolis.university", "psswrd7", Arrays.stream(new String[]{"master"}).toList(), "student"));
        assertEquals(result.get(8), new User("z.kholmatova@innopolis.university", "psswr8", Arrays.stream(new String[]{"phd"}).toList(), "staff"));

    }

    @Test
    void addUser() throws SQLException {
        clear();
        fill();
        model.addUser(new User("new.user@innopolis.university", "psswr9", Arrays.stream(new String[]{"phd"}).toList(), "staff"));
        model.addUser(new User("second.user@innopolis.university", "psswr10", Arrays.stream(new String[]{"bachelor"}).toList(), "staff"));
        var result = model.getUsers();
        assertEquals(11, result.size());
        assertEquals(result.get(0), new User("k.batyshchev@innopolis.university", "psswrd", Arrays.stream(new String[]{"bachelor"}).toList(), "student"));
        assertEquals(result.get(1), new User("d.alekhin@innopolis.university", "psswrd1", Arrays.stream(new String[]{"bachelor"}).toList(), "student"));
        assertEquals(result.get(2), new User("g.budnik@innopolis.university", "psswrd2", Arrays.stream(new String[]{"bachelor"}).toList(), "student"));
        assertEquals(result.get(3), new User("a.palashkina@innopolis.university", "psswrd3", Arrays.stream(new String[]{"bachelor"}).toList(), "student"));
        assertEquals(result.get(4), new User("guz.zakirova@innopolis.university", "psswrd4", Arrays.stream(new String[]{"bachelor"}).toList(), "student"));
        assertEquals(result.get(5), new User("a.potemkin@innopolis.university", "psswrd5", Arrays.stream(new String[]{"master"}).toList(), "student"));
        assertEquals(result.get(6), new User("a.elbatanony@innopolis.university", "psswrd6", Arrays.stream(new String[]{"master"}).toList(), "student"));
        assertEquals(result.get(7), new User("v.shelkovnikov@innopolis.university", "psswrd7", Arrays.stream(new String[]{"master"}).toList(), "student"));
        assertEquals(result.get(8), new User("z.kholmatova@innopolis.university", "psswr8", Arrays.stream(new String[]{"phd"}).toList(), "staff"));
        assertEquals(result.get(9), new User("new.user@innopolis.university", "psswr9", Arrays.stream(new String[]{"phd"}).toList(), "staff"));
        assertEquals(result.get(10), new User("second.user@innopolis.university", "psswr10", Arrays.stream(new String[]{"bachelor"}).toList(), "staff"));
    }

    @Test
    void assignUsersToGroup() throws SQLException {
        clear();
        fill();
        var initialGroups = new ArrayList<String>();
        initialGroups.add("bachelor");
        var me = new User("k.batyshchev@innopolis.university", "psswrd", new ArrayList<>(initialGroups), "student");
        var dima = new User("d.alekhin@innopolis.university", "psswrd1", new ArrayList<>(initialGroups), "student");
        var gosha = new User("g.budnik@innopolis.university", "psswrd2", new ArrayList<>(initialGroups), "student");
        ArrayList<User> toAssign = new ArrayList<>(List.of(new User[]{me, dima, gosha}));
        model.assignUsersToGroup(toAssign, "master");
        var result = model.getUsers();
        for (int i = 0; i < 3; i++) {
            assertEquals(toAssign.get(i), result.get(i));
        }
    }

    @Test
    void dropUser() {
    }

    @Test
    void getUsersByGroup() {
    }

    @Test
    void getUsersByRole() {
    }
}