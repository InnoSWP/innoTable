-- This file contains only MOCK data which will be filled into the schema
DELETE FROM "user";
DELETE FROM "group";
DELETE FROM role;
DELETE FROM event;

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
VALUES ('d.alekhin@innopolis.university', '12345', (SELECT id FROM role WHERE label = 'admin')),
       ('g.budnik@innopolis.university', '12345', (SELECT id FROM role WHERE label = 'user')),
       ('k.batyshchev@innopolis.university', '12345', (SELECT id FROM role WHERE label = 'user')),
       ('a.palashkina@innopolis.university', '12345', (SELECT id FROM role WHERE label = 'user')),
       ('guz.zakirova@innopolis.university', '12345', (SELECT id FROM role WHERE label = 'user'))
ON CONFLICT DO NOTHING;

INSERT INTO "user_group"(user_id, group_id)
VALUES ((SELECT id FROM "user" WHERE email LIKE 'd.al%'), (SELECT id FROM "group" WHERE label = 'bachelor')),
       ((SELECT id FROM "user" WHERE email LIKE 'g.bud%'), (SELECT id FROM "group" WHERE label = 'bachelor')),
       ((SELECT id FROM "user" WHERE email LIKE 'k.bat%'), (SELECT id FROM "group" WHERE label = 'master')),
       ((SELECT id FROM "user" WHERE email LIKE 'a.pal%'), (SELECT id FROM "group" WHERE label = 'phd')),
       ((SELECT id FROM "user" WHERE email LIKE 'guz.z%'), (SELECT id FROM "group" WHERE label = 'phd'))
ON CONFLICT DO NOTHING;