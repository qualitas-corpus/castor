create table tc7x_as_assoc1 (
  id        int not null,
  name      varchar(200) not null,
  constraint pk_tc7x_as_assoc1 primary key (id)
)
go

insert into tc7x_as_assoc1 (id, name) values (1, 'assoc1')
go

create table tc7x_as_main (
  id        int not null primary key,
  name      varchar(200) not null,
  assoc1_id    int default null,
  constraint pk_tc7x_as_main primary key (id)
)
go

insert into tc7x_as_main (id, name, assoc1_id) values (1, 'main', 1)
go

create table tc7x_as_main_many (
  id        int not null,
  name      varchar(200) not null,
  constraint pk_tc7x_as_main_many primary key (id)
)
go

insert into tc7x_as_main_many (id, name) values (1, 'main')
go

create table tc7x_as_assoc_many (
  id        int not null,
  name      varchar(200) not null,
  main_id    int,
  constraint pk_tc7x_as_assoc_many primary key (id)
)
go

insert into tc7x_as_assoc_many (id, name, main_id) values (1, 'assoc.many.1', 1)
go
insert into tc7x_as_assoc_many (id, name, main_id) values (2, 'assoc.many.2', 1)
go
