-- ЕTHis file contains only MOCK data which will be filled into the schema

INSERT INTO role
VALUES ('admin'),
       ('user');

INSERT INTO "group"
VALUES ('bachelor'),
       ('master'),
       ('phd');

INSERT INTO "user"
VALUES ('d.alekhin@innopolis.university', '12345', 1),
       ('g.budnik@innopolis.university', '12345', 1),
       ('k.batyshchev@innopolis.university', '12345', 2),
       ('a.palashkina@innopolis.university', '12345', 2),
       ('guz.zakirova@innopolis.university', '12345', 2);

INSERT INTO "user_group"
VALUES (1, 1),
       (2, 1),
       (3, 2),
       (4, 3),
       (5, 4);