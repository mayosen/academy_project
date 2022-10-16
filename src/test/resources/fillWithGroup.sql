INSERT INTO system_item(item_id, url, date, parent_id, type, size) VALUES ('f4', 'f4-url', '2022-09-11 12:00:00.000000 +00:00', null, 'FILE', 40);
INSERT INTO system_item(item_id, url, date, parent_id, type, size) VALUES ('a', null, '2022-09-11 12:00:00.000000 +00:00', null, 'FOLDER', 410);

INSERT INTO system_item(item_id, url, date, parent_id, type, size) VALUES ('b1', null, '2022-09-11 12:00:00.000000 +00:00', 'a', 'FOLDER', 210);
INSERT INTO system_item(item_id, url, date, parent_id, type, size) VALUES ('c2', null, '2022-09-11 12:00:00.000000 +00:00', 'b1', 'FOLDER', 100);
INSERT INTO system_item(item_id, url, date, parent_id, type, size) VALUES ('f3', 'f3-url', '2022-09-11 12:00:00.000000 +00:00', 'c2', 'FILE', 100);
INSERT INTO system_item(item_id, url, date, parent_id, type, size) VALUES ('f1', 'f1-url', '2022-09-11 12:00:00.000000 +00:00', 'b1', 'FILE', 50);
INSERT INTO system_item(item_id, url, date, parent_id, type, size) VALUES ('f2', 'f2-url', '2022-09-11 12:00:00.000000 +00:00', 'b1', 'FILE', 60);

INSERT INTO system_item(item_id, url, date, parent_id, type, size) VALUES ('b2', null, '2022-09-11 12:00:00.000000 +00:00', 'a', 'FOLDER', 0);
INSERT INTO system_item(item_id, url, date, parent_id, type, size) VALUES ('b3', null, '2022-09-11 12:00:00.000000 +00:00', 'a', 'FOLDER', 200);
INSERT INTO system_item(item_id, url, date, parent_id, type, size) VALUES ('c1', null, '2022-09-11 12:00:00.000000 +00:00', 'b3', 'FOLDER', 0);
INSERT INTO system_item(item_id, url, date, parent_id, type, size) VALUES ('f6', 'f6-url', '2022-09-11 12:00:00.000000 +00:00', 'c1', 'FILE', 200);
