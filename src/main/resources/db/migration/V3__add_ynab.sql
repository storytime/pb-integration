ALTER TABLE app_user
    ADD COLUMN ynab_auth_token varchar(255);

ALTER TABLE app_user
    ADD COLUMN ynab_sync_enabled boolean;

ALTER TABLE app_user
    ADD COLUMN ynab_last_sync_timestamp int8;

ALTER TABLE app_user
    ADD COLUMN ynab_sync_budget varchar(255);