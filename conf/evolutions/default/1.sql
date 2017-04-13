# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table linked_account (
  id                            bigint not null,
  user_id                       bigint,
  provider_user_id              varchar(255),
  provider_key                  varchar(255),
  constraint pk_linked_account primary key (id)
);
create sequence linked_account_seq;

create table security_role (
  id                            bigint not null,
  role_name                     varchar(255),
  constraint pk_security_role primary key (id)
);
create sequence security_role_seq;

create table token_action (
  id                            bigint not null,
  token                         varchar(255),
  target_user_id                bigint,
  type                          varchar(2),
  created                       timestamp,
  expires                       timestamp,
  constraint ck_token_action_type check (type in ('PR','EV')),
  constraint uq_token_action_token unique (token),
  constraint pk_token_action primary key (id)
);
create sequence token_action_seq;

create table users (
  id                            bigint not null,
  email                         varchar(255),
  name                          varchar(255),
  first_name                    varchar(255),
  last_name                     varchar(255),
  last_login                    timestamp,
  active                        boolean,
  email_validated               boolean,
  constraint pk_users primary key (id)
);
create sequence users_seq;

create table users_security_role (
  users_id                      bigint not null,
  security_role_id              bigint not null,
  constraint pk_users_security_role primary key (users_id,security_role_id)
);

create table users_user_permission (
  users_id                      bigint not null,
  user_permission_id            bigint not null,
  constraint pk_users_user_permission primary key (users_id,user_permission_id)
);

create table user_permission (
  id                            bigint not null,
  value                         varchar(255),
  constraint pk_user_permission primary key (id)
);
create sequence user_permission_seq;

alter table linked_account add constraint fk_linked_account_user_id foreign key (user_id) references users (id) on delete restrict on update restrict;
create index ix_linked_account_user_id on linked_account (user_id);

alter table token_action add constraint fk_token_action_target_user_id foreign key (target_user_id) references users (id) on delete restrict on update restrict;
create index ix_token_action_target_user_id on token_action (target_user_id);

alter table users_security_role add constraint fk_users_security_role_users foreign key (users_id) references users (id) on delete restrict on update restrict;
create index ix_users_security_role_users on users_security_role (users_id);

alter table users_security_role add constraint fk_users_security_role_security_role foreign key (security_role_id) references security_role (id) on delete restrict on update restrict;
create index ix_users_security_role_security_role on users_security_role (security_role_id);

alter table users_user_permission add constraint fk_users_user_permission_users foreign key (users_id) references users (id) on delete restrict on update restrict;
create index ix_users_user_permission_users on users_user_permission (users_id);

alter table users_user_permission add constraint fk_users_user_permission_user_permission foreign key (user_permission_id) references user_permission (id) on delete restrict on update restrict;
create index ix_users_user_permission_user_permission on users_user_permission (user_permission_id);


# --- !Downs

alter table linked_account drop constraint if exists fk_linked_account_user_id;
drop index if exists ix_linked_account_user_id;

alter table token_action drop constraint if exists fk_token_action_target_user_id;
drop index if exists ix_token_action_target_user_id;

alter table users_security_role drop constraint if exists fk_users_security_role_users;
drop index if exists ix_users_security_role_users;

alter table users_security_role drop constraint if exists fk_users_security_role_security_role;
drop index if exists ix_users_security_role_security_role;

alter table users_user_permission drop constraint if exists fk_users_user_permission_users;
drop index if exists ix_users_user_permission_users;

alter table users_user_permission drop constraint if exists fk_users_user_permission_user_permission;
drop index if exists ix_users_user_permission_user_permission;

drop table if exists linked_account;
drop sequence if exists linked_account_seq;

drop table if exists security_role;
drop sequence if exists security_role_seq;

drop table if exists token_action;
drop sequence if exists token_action_seq;

drop table if exists users;
drop sequence if exists users_seq;

drop table if exists users_security_role;

drop table if exists users_user_permission;

drop table if exists user_permission;
drop sequence if exists user_permission_seq;

