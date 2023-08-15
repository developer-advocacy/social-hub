-- users and ayrshare accounts
create table if not exists hub_users
(
    id           serial primary key,
    created_date timestamp not null default NOW(),
    name         text unique
);

create table if not exists hub_ayrshare_accounts
(
    id           serial primary key,
    label        varchar(255) not null,
    created_date timestamp    not null default NOW(),
    bearer_token text,
    unique (label)
);

create table if not exists hub_users_ayrshare_accounts
(
    ayrshare_accounts_fk int references hub_ayrshare_accounts (id),
    users_fk             int references hub_users (id),
    primary key (ayrshare_accounts_fk, users_fk)
);


create table if not exists hub_posts
(
    id             serial primary key,
    text           text      not null,
    date           timestamp not null,
    posted         timestamp null,
    scheduled_date timestamp null,
    user_fk        bigint references hub_users (id)

);

-- media
create table if not exists hub_media
(
    id           serial primary key,
    uuid         varchar(255) not null,
    content      bytea        not null,
    content_type varchar(255) not null,
    unique (uuid)
);


create table if not exists hub_posts_media
(
    post_fk  bigint references hub_posts (id),
    media_fk bigint references hub_media (id),
    ordering int default 0 not null,
    unique (post_fk, media_fk)
);



create table if not exists hub_posts_target_platforms
(
    ayrshare_account_fk bigint references hub_ayrshare_accounts (id),
    post_fk             bigint references hub_posts (id),
    platform            varchar(255),
    unique (ayrshare_account_fk, post_fk, platform)
);



