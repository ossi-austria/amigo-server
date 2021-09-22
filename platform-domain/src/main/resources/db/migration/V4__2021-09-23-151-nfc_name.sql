ALTER TABLE nfc
    add column if not exists name       VARCHAR(1024),
    add column if not exists updated_at timestamp,
    drop column if exists linked_multimedia_id;

