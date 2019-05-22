ALTER TABLE app_user
    ADD COLUMN ynab_auth_token varchar(255);

ALTER TABLE app_user
    ADD COLUMN ynab_sync_enabled boolean;

CREATE TABLE ynab_sync_config
(
    id          bigint                  NOT NULL,
    budget_name character varying(1024) NOT NULL,
    user_id     bigint                  NOT NULL,
    last_sync   int8                    NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE ynab_sync_config_tags_sync_properties
(
    ynab_sync_config_id  bigint NOT NULL,
    tags_sync_properties character varying(255)
);