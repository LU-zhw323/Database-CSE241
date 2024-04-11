drop table payment;
drop table transfer;
drop table credit_debit;
drop table select_amenity;
drop table apt_private;
drop table prop_common;
drop table common_amenity;
drop table private_amenity;
drop table amenity;
drop table tenant;
drop table lease;
drop table pet;
drop table visits;
drop table person;
drop table prospect_tenant;
drop table apartment;
drop table property;


create table property(
    prop_id NUMBER(5) GENERATED ALWAYS AS IDENTITY, 
    address varchar(20) not null, 
    primary key(prop_id),
    unique(address)
);



create table apartment(
    prop_id number(5) not null,
    apt_id NUMBER(5) not null, 
    rent numeric(7,2) not null,
    apt_size numeric(7,2) not null,
    bedroom_num int not null, 
    bathroom_num int not null,
    primary key(prop_id, apt_id),
    foreign key(prop_id) references property(prop_id)
);



create table prospect_tenant(
    id NUMBER(5) GENERATED ALWAYS AS IDENTITY,
    name varchar(20) not null,
    date_of_birth DATE not null,
    primary key(id)
);


create table person(
    id number(5) not null,
    month_income numeric(7,2) not null,
    rental_history varchar(20) not null,
    crime_history varchar(20) not null,
    primary key(id),
    foreign key(id) references prospect_tenant(id)
);


create table visits(
    prop_id number(5) not null,
    apt_id number(5) not null,
    tenent_id number(5) not null,
    primary key(prop_id,apt_id, tenent_id),
    foreign key(tenent_id) references person(id),
    foreign key(prop_id,apt_id) references apartment(prop_id, apt_id)
);



create table pet(
    id number(5) not null,
    species varchar(20) not null,
    vaccination varchar(20) not null,
    primary key(id),
    foreign key(id) references prospect_tenant(id)
);





create table lease(
    lease_id NUMBER(5) GENERATED ALWAYS AS IDENTITY,
    prop_id number(5) not null,
    apt_id number(5) not null,
    term int not null,
    start_date DATE not null,
    deposit numeric(7,2) not null,
    primary key(lease_id),
    foreign key(prop_id,apt_id) references apartment(prop_id, apt_id),
    unique(prop_id, apt_id),
    CHECK (term = 12)
);


create table lease_pet(
    lease_id NUMBER(5) not null,
    id NUMBER(5) not null,
    primary key(lease_id),
    foreign key(id) references pet(id),
    foreign key(lease_id) references lease(lease_id) on delete cascade
);


create table tenant(
    id number(5) not null,
    lease_id number(5) not null,
    ssn numeric(9,0) not null,
    primary key(id),
    foreign key(id) references person(id),
    foreign key(lease_id) references lease(lease_id) on delete cascade,
    unique(ssn)
);

create table amenity(
    amenity_id NUMBER(5) GENERATED ALWAYS AS IDENTITY,
    description varchar(20) not null,
    cost numeric(7,2) not null,
    primary key(amenity_id)
);

create table common_amenity(
    amenity_id number(5) not null,
    hour int not null,
    capacity int not null,
    primary key(amenity_id),
    foreign key(amenity_id) references amenity(amenity_id)

);

create table private_amenity(
    amenity_id number(5) not null,
    feature varchar(20) not null,
    warranty varchar(20) not null,
    primary key(amenity_id),
    foreign key(amenity_id) references amenity(amenity_id)

);

create table prop_common(
    amenity_id number(5) not null,
    prop_id number(5) not null,
    primary key(amenity_id, prop_id),
    foreign key(amenity_id) references common_amenity(amenity_id),
    foreign key(prop_id) references property(prop_id)
);

create table apt_private(
    amenity_id number(5) not null,
    prop_id number(5) not null,
    apt_id number(5) not null,
    primary key(amenity_id, prop_id,apt_id),
    foreign key(amenity_id) references amenity(amenity_id),
    foreign key(prop_id, apt_id) references apartment(prop_id, apt_id)
);

create table select_amenity(
    amenity_id number(5) not null,
    lease_id number(5) not null,
    primary key(amenity_id, lease_id),
    foreign key(amenity_id) references amenity(amenity_id),
    foreign key(lease_id) references lease(lease_id) on delete cascade
);

create table payment(
    payment_id NUMBER(5) GENERATED ALWAYS AS IDENTITY,
    lease_id number(5),
    payment_date DATE not null,
    amount numeric(7,2) not null,
    primary key(payment_id),
    foreign key(lease_id) references lease(lease_id) on delete set null
);




create table credit_debit(
    payment_id number(5) not null,
    card_number numeric(16,0) not null,
    card_name varchar(20) not null,
    expire_date DATE not null,
    bill_address varchar(20) not null,
    primary key(payment_id),
    foreign key(payment_id) references payment(payment_id)
);
select * from credit_debit;


create table transfer(
    payment_id number(5) not null,
    account_number numeric(12,0) not null,
    bank_name varchar(20) not null,
    routing_number numeric(9,0) not null,
    reference_number varchar(20) not null,
    primary key(payment_id),
    foreign key(payment_id) references payment(payment_id),
    CHECK (REGEXP_LIKE(reference_number, '^[A-Z0-9]{8,20}$'))
);






