ALTER TABLE IF EXISTS system_item DROP CONSTRAINT IF EXISTS fk_parent_id;
DROP TABLE IF EXISTS system_item CASCADE;

CREATE TABLE system_item(
    item_id VARCHAR(255) PRIMARY KEY,
    url VARCHAR(255),
    date timestamptz NOT NULL,
    parent_id VARCHAR(255),
    type VARCHAR(10) NOT NULL,
    size bigint
);

ALTER TABLE system_item ADD CONSTRAINT fk_parent_id
    FOREIGN KEY (parent_id) REFERENCES system_item(item_id) ON DELETE CASCADE;
