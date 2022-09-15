DROP INDEX IF EXISTS system_item_date_index;
CREATE INDEX system_item_date_index ON system_item(date);
