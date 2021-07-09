DO
$$
    BEGIN
        CREATE ROLE amigo WITH NOLOGIN;
    EXCEPTION
        WHEN DUPLICATE_OBJECT THEN
            RAISE NOTICE 'not creating role amigo -- it already exists';
    END
$$;

create schema if not exists public;
GRANT ALL ON SCHEMA public to amigo;

CREATE TABLE IF NOT EXISTS account
(
    id                              uuid not null
        constraint account_pkey primary key,
    change_account_token            varchar(255),
    change_account_token_created_at timestamp,
    email                           varchar(255)
        constraint account_unique_email unique,
    last_login                      timestamp,
    last_refresh                    timestamp,
    last_revocation_date            timestamp,
    password_encrypted              varchar(255),
    fcm_token                       varchar(1024)
);
ALTER TABLE account
    owner to amigo;



CREATE TABLE IF NOT EXISTS groups
(
    id   uuid         not null
        constraint groups_pkey primary key,
    name varchar(255) not null
        constraint group_unique_name unique
);
ALTER TABLE groups
    owner to amigo;



CREATE TABLE IF NOT EXISTS person
(
    id          uuid not null
        constraint person_pkey primary key,
    account_id  uuid not null,
    group_id    uuid not null,
    member_type varchar(255),
    name        varchar(255)
);
ALTER TABLE person
    owner to amigo;
ALTER TABLE person
    DROP CONSTRAINT IF EXISTS person_account_account_id_fkey;
ALTER TABLE person
    ADD CONSTRAINT person_account_account_id_fkey FOREIGN KEY (account_id) references account (id) ON DELETE CASCADE;
ALTER TABLE person
    DROP CONSTRAINT IF EXISTS persons_group_group_id_fkey;
ALTER TABLE person
    ADD CONSTRAINT persons_group_group_id_fkey FOREIGN KEY (group_id) references groups (id) ON DELETE CASCADE;
ALTER TABLE person
    DROP CONSTRAINT IF EXISTS person_unique_name_group;
ALTER TABLE person
    ADD CONSTRAINT person_unique_name_group UNIQUE (name, group_id);



CREATE TABLE IF NOT EXISTS call
(
    id             uuid not null
        constraint call_pkey primary key,
    call_type      varchar(255),
    created_at     timestamp,
    finished_at    timestamp,
    receiver_id    uuid not null,
    retrieved_at   timestamp,
    sender_id      uuid not null,
    sent_at        timestamp,
    started_at     timestamp,
    call_state     varchar(255),
    receiver_token varchar(255),
    sender_token   varchar(255)
);
ALTER TABLE call
    owner to amigo;
ALTER TABLE call
    DROP CONSTRAINT IF EXISTS call_person_receiver_id_fkey;
ALTER TABLE call
    ADD CONSTRAINT call_person_receiver_id_fkey FOREIGN KEY (receiver_id) references person (id) ON DELETE CASCADE;
ALTER TABLE call
    DROP CONSTRAINT IF EXISTS call_person_sender_id_fkey;
ALTER TABLE call
    ADD CONSTRAINT call_person_sender_id_fkey FOREIGN KEY (sender_id) references person (id) ON DELETE CASCADE;



CREATE TABLE IF NOT EXISTS message
(
    id            uuid not null
        constraint message_pkey primary key,
    created_at    timestamp,
    receiver_id   uuid not null,
    retrieved_at  timestamp,
    sender_id     uuid not null,
    sent_at       timestamp,
    text          varchar(255),
    multimedia_id uuid null
);
ALTER TABLE message
    owner to amigo;
ALTER TABLE message
    DROP CONSTRAINT IF EXISTS message_person_receiver_id_fkey;
ALTER TABLE message
    ADD CONSTRAINT message_person_receiver_id_fkey FOREIGN KEY (receiver_id) references person (id) ON DELETE CASCADE;
ALTER TABLE message
    DROP CONSTRAINT IF EXISTS message_person_sender_id_fkey;
ALTER TABLE message
    ADD CONSTRAINT message_person_sender_id_fkey FOREIGN KEY (sender_id) references person (id) ON DELETE CASCADE;



CREATE TABLE IF NOT EXISTS album
(
    id         uuid not null
        constraint album_pkey primary key,
    created_at timestamp,
    name       varchar(255),
    owner_id   uuid not null,
    updated_at timestamp
);
ALTER TABLE album
    owner to amigo;
ALTER TABLE album
    DROP CONSTRAINT IF EXISTS album_unique_name_per_owner;
ALTER TABLE album
    ADD CONSTRAINT album_unique_name_per_owner UNIQUE (name, owner_id);
ALTER TABLE album
    DROP CONSTRAINT IF EXISTS albums_person_owner_id_fkey;
ALTER TABLE album
    ADD CONSTRAINT albums_person_owner_id_fkey FOREIGN KEY (owner_id) references person (id) ON DELETE CASCADE;



CREATE TABLE IF NOT EXISTS multimedia
(
    id           uuid not null
        constraint multimedia_pkey primary key,
    album_id     uuid null,
    created_at   timestamp,
    owner_id     uuid not null,
    remote_url   varchar(255),
    size         bigint,
    type         varchar(255),
    content_type varchar(255),
    filename     varchar(255)
);
ALTER TABLE multimedia
    owner to amigo;
ALTER TABLE multimedia
    DROP CONSTRAINT IF EXISTS multimedia_person_owner_id_fkey;
ALTER TABLE multimedia
    ADD CONSTRAINT multimedia_person_owner_id_fkey FOREIGN KEY (owner_id) references person (id) ON DELETE CASCADE;
ALTER TABLE multimedia
    DROP CONSTRAINT IF EXISTS multimedia_album_album_id_fkey;
ALTER TABLE multimedia
    ADD CONSTRAINT multimedia_album_album_id_fkey FOREIGN KEY (album_id) references album (id) ON DELETE CASCADE;



CREATE TABLE IF NOT EXISTS album_share
(
    id           uuid not null
        constraint album_share_pkey primary key,
    album_id     uuid null
        constraint account_subject_person_id_fk references album,
    created_at   timestamp,
    receiver_id  uuid not null,
    retrieved_at timestamp,
    sender_id    uuid not null,
    sent_at      timestamp
);
ALTER TABLE album_share
    owner to amigo;
ALTER TABLE album_share
    DROP CONSTRAINT IF EXISTS album_share_person_receiver_id_fkey;
ALTER TABLE album_share
    ADD CONSTRAINT album_share_person_receiver_id_fkey FOREIGN KEY (receiver_id) references person (id) ON DELETE CASCADE;
ALTER TABLE album_share
    DROP CONSTRAINT IF EXISTS album_share_person_sender_id_fkey;
ALTER TABLE album_share
    ADD CONSTRAINT album_share_person_sender_id_fkey FOREIGN KEY (sender_id) references person (id) ON DELETE CASCADE;



CREATE TABLE IF NOT EXISTS nfc
(
    id                   uuid not null
        constraint nfc_pkey primary key,
    created_at           timestamp,
    linked_album_id      uuid null,
    linked_multimedia_id uuid null,
    linked_person_id     uuid null,
    type                 varchar(255),
    creator_id           uuid not null,
    owner_id             uuid not null

);
ALTER TABLE nfc
    owner to amigo;
ALTER TABLE nfc
    DROP CONSTRAINT IF EXISTS nfcs_album_linked_album_id_fk;
ALTER TABLE nfc
    ADD CONSTRAINT nfcs_album_linked_album_id_fk
        FOREIGN KEY (linked_album_id) references album (id) ON DELETE SET NULL;
ALTER TABLE nfc
    DROP CONSTRAINT IF EXISTS nfcs_multimedia_linked_multimedia_id_fk;
ALTER TABLE nfc
    ADD CONSTRAINT nfcs_multimedia_linked_multimedia_id_fk
        FOREIGN KEY (linked_multimedia_id) references multimedia (id) ON DELETE SET NULL;
ALTER TABLE nfc
    DROP CONSTRAINT IF EXISTS nfcs_person_linked_person_id_fk;
ALTER TABLE nfc
    ADD CONSTRAINT nfcs_person_linked_person_id_fk
        FOREIGN KEY (linked_person_id) references person (id) ON DELETE SET NULL;
ALTER TABLE nfc
    DROP CONSTRAINT IF EXISTS nfcs_person_creator_id_fk;
ALTER TABLE nfc
    ADD CONSTRAINT nfcs_person_creator_id_fk
        FOREIGN KEY (creator_id) references person (id) ON DELETE SET NULL;
ALTER TABLE nfc
    DROP CONSTRAINT IF EXISTS nfcs_person_owner_id_fk;
ALTER TABLE nfc
    ADD CONSTRAINT nfcs_person_owner_id_fk
        FOREIGN KEY (owner_id) references person (id) ON DELETE SET NULL;


