-- backend/infrastructure/src/main/resources/db/migration/V1__init.sql


create table if not exists accounts (
    id          uuid primary key,
    version     bigint          not null default 0,
    type        varchar(20)     not null,
    balance     numeric(19,2)   not null default 0,
    overdraft   numeric(19,2)               default 0,
    ceiling     numeric(19,2),
    created_at  timestamptz     not null default now(),
    updated_at  timestamptz     not null default now()
);

create table if not exists operations (
    id          uuid primary key,
    account_id  uuid            not null,
    amount      numeric(19,2)   not null,
    type        varchar(20)     not null,
    at          timestamptz     not null default now(),
    label       varchar(255),

    constraint fk_operations_account
        foreign key (account_id) references accounts(id) on delete cascade
);

create index if not exists idx_operations_account on operations(account_id);