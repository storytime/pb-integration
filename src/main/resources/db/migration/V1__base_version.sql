create table app_user (
  id  bigserial not null,
  time_zone varchar(255) not null,
  zen_auth_token varchar(255) not null,
  zen_last_sync_timestamp int8 not null,
  primary key (id)
);

create table currency_rates (
  id  bigserial not null, buy_rate numeric(19, 2) not null,
  currency_source varchar(255) not null,
  currency_type varchar(255) not null,
  date int8 not null, sell_rate numeric(19, 2) not null,
  primary key (id)
);

create table custom_payee (
  id  bigserial not null,
  contains_value varchar(255) not null,
  payee varchar(255) not null,
  primary key (id)
);

create table merchant_info (
  id  bigserial not null,
  card_number varchar(255) not null,
  enabled boolean not null,
  merchant_id int4 not null,
  password varchar(255) not null,
  sync_period int8 not null,
  sync_priority varchar(255),
  sync_start_date int8 not null,
  primary key (id)
);

create table merchant_info_additional_comment (
  merchant_info_id int8 not null,
  additional_comment varchar(255)
);

alter table currency_rates add constraint uniqueCR unique (currency_source, currency_type, date);

alter table custom_payee add constraint uniqueCP unique (contains_value);