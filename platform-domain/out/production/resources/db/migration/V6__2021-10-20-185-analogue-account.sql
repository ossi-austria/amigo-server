CREATE TABLE IF NOT EXISTS login_token
(
    id         uuid         not null
        constraint login_token_pkey primary key,
    person_id  uuid         not null unique,
    token      varchar(255) not null unique,
    created_at timestamp,
    expires_at timestamp
);
ALTER TABLE login_token
    owner to amigo;

ALTER TABLE login_token
    DROP CONSTRAINT IF EXISTS login_token_person_id_fkey;
ALTER TABLE login_token
    ADD CONSTRAINT login_token_person_id_fkey FOREIGN KEY (person_id) references person (id) ON DELETE CASCADE;

ALTER TABLE account
    add column if not exists created_by_account_id uuid null,
    add column if not exists has_valid_mail        bool default true
;

ALTER TABLE account
    DROP CONSTRAINT IF EXISTS account_created_by_account_id_fkey;
ALTER TABLE account
    ADD CONSTRAINT account_created_by_account_id_fkey FOREIGN KEY (created_by_account_id)
        references account (id) ON DELETE SET NULL;
