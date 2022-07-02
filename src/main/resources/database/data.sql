-- This file contains only MOCK data which will be filled into the schema

INSERT INTO role(label)
VALUES ('admin'),
       ('user')
ON CONFLICT DO NOTHING;

INSERT INTO "group"(label)
VALUES ('bachelor'),
       ('master'),
       ('phd')
ON CONFLICT DO NOTHING;

INSERT INTO "user"(email, password, role_id)
VALUES ('d.alekhin@innopolis.university', '12345', 1),
       ('g.budnik@innopolis.university', '12345', 2),
       ('k.batyshchev@innopolis.university', '12345', 2),
       ('a.palashkina@innopolis.university', '12345', 2),
       ('guz.zakirova@innopolis.university', '12345', 2)
ON CONFLICT DO NOTHING;

INSERT INTO "user_group"(user_id, group_id)
VALUES (1, 1),
       (2, 1),
       (3, 2),
       (4, 3),
       (5, 3)
ON CONFLICT DO NOTHING;