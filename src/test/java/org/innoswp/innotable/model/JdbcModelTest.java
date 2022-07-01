package org.innoswp.innotable.model;

import org.innoswp.innotable.model.event.CalendarEvent;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JdbcModelTest {
    private static final Properties properties = new Properties();

    private static final JdbcModel MODEL = new JdbcModel();
    private final Pair<String, CalendarEvent> EVENT1 = new Pair<>("bachelor", new CalendarEvent("DROP", "DROP of bachelors from IU", "IU", new Date(2022 - 1900, Calendar.JULY, 1, 9, 3, 50), new Date(2022 - 1900, Calendar.JULY, 2, 9, 3, 50)));
    private final Pair<String, CalendarEvent> EVENT2 = new Pair<>("master", new CalendarEvent("RETAKE", "RETAKE for all master students", "108", new Date(2022 - 1900, Calendar.JULY, 3, 9, 0, 0), new Date(2022 - 1900, Calendar.JULY, 3, 15, 0, 0)));
    private final Pair<String, CalendarEvent> EVENT3 = new Pair<>("319", new CalendarEvent("VACATION", "VACATION for all 319 staff", "319", new Date(2022 - 1900, Calendar.JULY, 4, 0, 0, 0), new Date(2022 - 1900, Calendar.JULY, 11, 0, 0, 0)));
    private final Pair<String, CalendarEvent> EVENT4 = new Pair<>("phd", new CalendarEvent("EXTRA LECTURE", "phd students have extra lecture on ***", "107", new Date(2022 - 1900, Calendar.JULY, 6, 9, 0, 0), new Date(2022 - 1900, Calendar.JULY, 6, 10, 30, 0)));
    private final Pair<String, CalendarEvent> EVENT5 = new Pair<>("bachelor", new CalendarEvent("RETAKE1", "RETAKE on AGLA", "IU", new Date(2022 - 1900, Calendar.JUNE, 30, 9, 0, 0), new Date(2022 - 1900, Calendar.JUNE, 30, 12, 0, 0)));
    private final User USER1 = new User("USER1@innopolis.university", "pwd0", new ArrayList<>(List.of("319")), "staff");
    private final User USER2 = new User("USER2@innopolis.university", "pwd1", new ArrayList<>(List.of("319", "phd")), "staff");
    private final User USER3 = new User("USER3@innopolis.university", "pwd2", new ArrayList<>(List.of("319", "master")), "student");
    private final User USER4 = new User("USER4@innopolis.university", "pwd3", new ArrayList<>(List.of("master")), "student");
    private final User USER5 = new User("USER5@innopolis.university", "pwd4", new ArrayList<>(List.of("bachelor")), "student");
    private final String GROUP1 = "bachelor";
    private final String GROUP2 = "master";
    private final String GROUP3 = "phd";
    private final String GROUP4 = "319";
    private final String ROLE1 = "staff";
    private final String ROLE2 = "student";
    private final Pair<String, CalendarEvent> NEW_EVENT = new Pair<>(GROUP2, new CalendarEvent("EXTRA LECTURE", "master students have extra lecture on ***", "106", new Date(2022 - 1900, Calendar.JULY, 7, 9, 0, 0), new Date(2022 - 1900, Calendar.JULY, 8, 10, 30, 0)));
    private final User NEW_USER = new User("new@iu", "pwd_new", new ArrayList<>(List.of(GROUP4, GROUP1)), "staff");
    private final String NEW_ROLE = "new role";

    static {
        try {
            properties.load(new BufferedReader(new FileReader("src/test/resources/application.properties")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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

    private final String DB_URL = properties.getProperty("db.url");

    private final String DB_USERNAME = properties.getProperty("db.username");

    private final String DB_PASSWORD = properties.getProperty("db.password");

    void execute(String query) throws SQLException {
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.execute();
        }
    }

    void clear() throws SQLException {
        execute("""
                DELETE FROM event;
                DELETE FROM "user";
                DELETE FROM "group";
                DELETE FROM role;
                """);
    }

    void fill() throws SQLException {
        execute("""
                INSERT INTO role(label)
                VALUES ('staff'),
                        ('student');
                INSERT INTO "user"(email, password, role_id)
                VALUES ('USER1@innopolis.university', 'pwd0', (SELECT id FROM role WHERE label = 'staff')),
                ('USER2@innopolis.university', 'pwd1', (SELECT id FROM role WHERE label = 'staff')),
                ('USER3@innopolis.university', 'pwd2', (SELECT id FROM role WHERE label = 'student')),
                ('USER4@innopolis.university', 'pwd3', (SELECT id FROM role WHERE label = 'student')),
                ('USER5@innopolis.university', 'pwd4', (SELECT id FROM role WHERE label = 'student'));
                INSERT INTO "group"(label)
                VALUES ('bachelor'),
                        ('master'),
                        ('phd'),
                        ('319');
                INSERT INTO user_group(user_id, group_id)
                VALUES ((SELECT id FROM "user" WHERE email LIKE '%1%'), (SELECT id FROM "group" WHERE label = '319')),
                ((SELECT id FROM "user" WHERE email LIKE '%2%'), (SELECT id FROM "group" WHERE label = '319')),
                ((SELECT id FROM "user" WHERE email LIKE '%2%'), (SELECT id FROM "group" WHERE label = 'phd')),
                ((SELECT id FROM "user" WHERE email LIKE '%3%'), (SELECT id FROM "group" WHERE label = '319')),
                ((SELECT id FROM "user" WHERE email LIKE '%3%'), (SELECT id FROM "group" WHERE label = 'master')),
                ((SELECT id FROM "user" WHERE email LIKE '%4%'), (SELECT id FROM "group" WHERE label = 'master')),
                ((SELECT id FROM "user" WHERE email LIKE '%5%'), (SELECT id FROM "group" WHERE label = 'bachelor'));
                                
                INSERT INTO event(title, description, location, start_dt, end_dt, group_id)
                VALUES ('DROP',   'DROP of bachelors from IU',              'IU', '2022-07-01 09:03:50.000000 +00:00', '2022-07-02 09:03:50.000000 +00:00', (SELECT id FROM "group" WHERE label = 'bachelor')),
                ('RETAKE',        'RETAKE for all master students',         '108', '2022-07-03 09:00:00.000000 +00:00', '2022-07-03 15:00:00.000000 +00:00', (SELECT id FROM "group" WHERE label = 'master')),
                ('VACATION',      'VACATION for all 319 staff',             '319', '2022-07-04 00:00:00.000000 +00:00', '2022-07-11 00:00:00.000000 +00:00', (SELECT id FROM "group" WHERE label = '319')),
                ('EXTRA LECTURE', 'phd students have extra lecture on ***', '107', '2022-07-06 09:00:00.000000 +00:00', '2022-07-06 10:30:00.000000 +00:00', (SELECT id FROM "group" WHERE label = 'phd')),
                ('RETAKE1',       'RETAKE on AGLA',                         'IU', '2022-06-30 09:00:00.000000 +00:00', '2022-06-30 12:00:00.000000 +00:00', (SELECT id FROM "group" WHERE label = 'bachelor'));
                """);
    }

    @Test
    void getGroups() throws SQLException {
        clear();
        assertEquals(MODEL.getGroups().size(), 0);
        fill();
        var expected = new String[]{GROUP1, GROUP2, GROUP3, GROUP4};
        var got = MODEL.getGroups();
        assertEquals(got.size(), 4);
        for (int i = 0; i < 4; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void saveGroup() throws SQLException {
        clear();
        fill();
        String NEW_GROUP = "new group";
        MODEL.saveGroup(NEW_GROUP);
        var expected = new String[]{GROUP1, GROUP2, GROUP3, GROUP4, NEW_GROUP};
        var got = MODEL.getGroups();
        assertEquals(5, got.size());
        for (int i = 0; i < 5; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void dropGroup() throws SQLException {
        clear();
        fill();
        MODEL.dropGroup(GROUP1);
        var expected = new String[]{GROUP2, GROUP3, GROUP4};
        var got = MODEL.getGroups();
        assertEquals(3, got.size());
        for (int i = 0; i < 3; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void getRoles() throws SQLException {
        clear();
        assertEquals(MODEL.getGroups().size(), 0);
        fill();
        var expected = new String[]{ROLE1, ROLE2};
        var got = MODEL.getRoles();
        assertEquals(2, got.size());
        for (int i = 0; i < 2; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void saveRole() throws SQLException {
        clear();
        fill();
        MODEL.saveRole(NEW_ROLE);
        var expected = new String[]{ROLE1, ROLE2, NEW_ROLE};
        var got = MODEL.getRoles();
        assertEquals(3, got.size());
        for (int i = 0; i < 3; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void saveEvent() throws SQLException {
        clear();
        fill();
        MODEL.saveEvent(NEW_EVENT.second(), NEW_EVENT.first());
        var expected = new Pair[]{EVENT5, EVENT1, EVENT2, EVENT3, EVENT4, NEW_EVENT};
        var got = MODEL.loadAllEvents();
        assertEquals(6, got.size());
        for (int i = 0; i < 6; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void dropEvent() throws SQLException {
        clear();
        fill();
        MODEL.dropEvent(EVENT2.second().title());
        var expected = new Pair[]{EVENT5, EVENT1, EVENT3, EVENT4};
        var got = MODEL.loadAllEvents();
        assertEquals(4, got.size());
        for (int i = 0; i < 4; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void loadEventsByUser() throws SQLException {
        clear();
        var users = new User[]{USER1, USER2, USER3, USER4, USER5, new User("wrong@innopolis.university", "pwd", new ArrayList<>(), "staff")};
        for (var user : users)
            assertEquals(MODEL.loadEventsByUser(user).size(), 0);
        fill();
        var expected = new Pair[][]{{EVENT3}, {EVENT3, EVENT4}, {EVENT3, EVENT2}, {EVENT2}, {EVENT5, EVENT1}, {}};
        for (int i = 0; i < users.length; i++) {
            var got = MODEL.loadEventsByUser(users[i]);
            assertEquals(got.size(), expected[i].length);
            for (int j = 0; j < expected[i].length; j++) {
                assertEquals(expected[i][j], got.get(j));
            }
        }
    }

    @Test
    void loadEventsByGroup() throws SQLException {
        clear();
        var groups = new String[]{GROUP1, GROUP2, GROUP3, GROUP4, "wrong group"};
        for (String group : groups)
            assertEquals(MODEL.loadEventsByGroup(group).size(), 0);
        fill();
        var expected = new Pair[][]{{EVENT5, EVENT1}, {EVENT2}, {EVENT4}, {EVENT3}, {}};
        for (int i = 0; i < groups.length; i++) {
            var got = MODEL.loadEventsByGroup(groups[i]);
            assertEquals(got.size(), expected[i].length);
            for (int j = 0; j < expected[i].length; j++) {
                assertEquals(expected[i][j], got.get(j));
            }
        }
    }

    @Test
    void loadEventsIn() throws SQLException {
        clear();
        var time = new Date[]{new Date(2022 - 1900, Calendar.JUNE, 30, 0, 0, 0),
                new Date(2022 - 1900, Calendar.JUNE, 30, 10, 0, 0),
                new Date(2022 - 1900, Calendar.JULY, 6, 9, 1, 0),
                new Date(2023 - 1900, Calendar.JULY, 6, 9, 0, 0)};

        assertEquals(0, MODEL.loadEventsIn(time[0], time[3]).size());
        fill();
        var expected = new Pair[][]{{EVENT5, EVENT1, EVENT2, EVENT3, EVENT4}, {EVENT5}, {EVENT3, EVENT4}};
        var got = MODEL.loadEventsIn(time[0], time[3]);
        assertEquals(expected[0].length, got.size());
        for (int j = 0; j < expected[0].length; j++) {
            assertEquals(expected[0][j], got.get(j));
        }
        got = MODEL.loadEventsIn(time[0], time[1]);
        assertEquals(expected[1].length, got.size());
        for (int j = 0; j < expected[1].length; j++) {
            assertEquals(expected[1][j], got.get(j));
        }
        got = MODEL.loadEventsIn(time[1], time[2]);
        assertEquals(expected[0].length, got.size());
        for (int j = 0; j < expected[0].length; j++) {
            assertEquals(expected[0][j], got.get(j));
        }
        got = MODEL.loadEventsIn(time[2], time[3]);
        assertEquals(expected[2].length, got.size());
        for (int j = 0; j < expected[2].length; j++) {
            assertEquals(expected[2][j], got.get(j));
        }
    }

    @Test
    void loadEventsDuring() throws SQLException {
        clear();
        var time = new Date[]{new Date(2022 - 1900, Calendar.JUNE, 30, 10, 0, 0), new Date(2022 - 1900, Calendar.JULY, 6, 9, 1, 0), new Date(2023 - 1900, Calendar.JULY, 6, 9, 0, 0)};
        for (var t : time)
            assertEquals(0, MODEL.loadEventsDuring(t).size());
        fill();
        var expected = new Pair[][]{{EVENT5}, {EVENT3, EVENT4}, {}};
        for (int i = 0; i < time.length; i++) {
            var got = MODEL.loadEventsDuring(time[i]);
            assertEquals(expected[i].length, got.size());
            for (int j = 0; j < expected[i].length; j++) {
                assertEquals(expected[i][j], got.get(j));
            }
        }
    }

    @Test
    void loadEventsByTitle() throws SQLException {
        clear();
        var titles = new String[]{"retake", "drop", "vacation", "lecture", "retake1", "wrong title", "r"};
        for (String title : titles)
            assertEquals(MODEL.loadEventsByTitle(title).size(), 0);
        fill();
        var expected = new Pair[][]{{EVENT5, EVENT2}, {EVENT1}, {EVENT3}, {EVENT4}, {EVENT5}, {}, {EVENT5, EVENT1, EVENT2, EVENT4}};
        for (int i = 0; i < titles.length; i++) {
            var got = MODEL.loadEventsByTitle(titles[i]);
            assertEquals(got.size(), expected[i].length);
            for (int j = 0; j < expected[i].length; j++) {
                assertEquals(expected[i][j], got.get(j));
            }
        }
    }

    @Test
    void loadAllEvents() throws SQLException {
        clear();
        assertEquals(MODEL.loadAllEvents().size(), 0);
        fill();
        var expectedResults = new Pair[]{EVENT5, EVENT1, EVENT2, EVENT3, EVENT4};
        var loaded = MODEL.loadAllEvents();
        assertEquals(loaded.size(), 5);
        for (int i = 0; i < 5; i++) {
            assertEquals(expectedResults[i], loaded.get(i));
        }
    }

    @Test
    void getUsers() throws SQLException {
        clear();
        assertEquals(MODEL.loadAllEvents().size(), 0);
        fill();
        var expectedResults = new User[]{USER1, USER2, USER3, USER4, USER5};
        var loaded = MODEL.getUsers();
        assertEquals(loaded.size(), 5);
        for (int i = 0; i < 5; i++) {
            assertEquals(expectedResults[i], loaded.get(i));
        }

    }

    @Test
    void addUser() throws SQLException {
        clear();
        fill();
        MODEL.addUser(NEW_USER);
        var expected = new User[]{USER1, USER2, USER3, USER4, USER5, NEW_USER};
        var got = MODEL.getUsers();
        assertEquals(6, got.size());
        for (int i = 0; i < 6; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void assignUsersToGroup() throws SQLException {
        clear();
        fill();
        var query = """
                SELECT id FROM user_group
                WHERE (user_id = (SELECT id FROM "user" WHERE email LIKE ?)
                OR user_id = (SELECT id FROM "user" WHERE email LIKE ?))
                AND group_id = (SELECT id FROM "group" WHERE label LIKE ?)
                """;
        ResultSet resultSet;
        try (
                var connection = open();
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, "%1%");
            preparedStatement.setString(2, "%2%");
            preparedStatement.setString(3, "bach%");
            resultSet = preparedStatement.executeQuery();
            assertFalse(resultSet.next());
            MODEL.assignUsersToGroup(new ArrayList<>(List.of(USER1, USER2)), GROUP1);
            resultSet = preparedStatement.executeQuery();
            assertTrue(resultSet.next());
            assertTrue(resultSet.next());
            assertFalse(resultSet.next());
        }
    }

    @Test
    void dropUser() throws SQLException {
        clear();
        fill();
        MODEL.dropUser(USER4.email());
        var expected = new User[]{USER1, USER2, USER3, USER5};
        var got = MODEL.getUsers();
        assertEquals(4, got.size());
        for (int i = 0; i < 4; i++) assertEquals(expected[i], got.get(i));
    }

    @Test
    void getUsersByGroup() throws SQLException {
        clear();
        var groups = new String[]{GROUP1, GROUP2, GROUP3, GROUP4, "wrong group"};
        for (String group : groups) {
            assertEquals(MODEL.getUsersByGroup(group).size(), 0);
        }
        fill();
        var expected = new User[][]{{USER5}, {USER3, USER4}, {USER2}, {USER1, USER2, USER3}, {}};
        for (int i = 0; i < groups.length; i++) {
            var got = MODEL.getUsersByGroup(groups[i]);
            assertEquals(got.size(), expected[i].length);
            for (int j = 0; j < expected[i].length; j++) {
                assertEquals(expected[i][j], got.get(j));
            }
        }
    }

    @Test
    void getUsersByRole() throws SQLException {
        clear();
        var roles = new String[]{ROLE1, ROLE2, "wrong role"};
        for (String role : roles) {
            assertEquals(MODEL.getUsersByRole(role).size(), 0);
        }
        fill();
        var expected = new User[][]{{USER1, USER2}, {USER3, USER4, USER5}, {}};
        for (int i = 0; i < roles.length; i++) {
            var got = MODEL.getUsersByRole(roles[i]);
            assertEquals(got.size(), expected[i].length);
            for (int j = 0; j < expected[i].length; j++) {
                assertEquals(expected[i][j], got.get(j));
            }
        }
    }
}