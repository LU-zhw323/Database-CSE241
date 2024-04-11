drop function view_apartment_lease;
drop type apartment_lease_tab;
drop type apartment_lease_obj;

drop function view_apartment_private;
drop type apartment_private_tab;
drop type apartment_private_obj;


drop function view_property_common;
drop type property_common_tab;
drop type property_common_obj;




select * from apartment left outer join apt_private on apartment.prop_id = apt_private.prop_id and apartment.apt_id = apt_private.apt_id;
select * from property left outer join prop_common on property.prop_id = prop_common.prop_id;

--Common Amentity Summary
    -------SQL
    SELECT property.prop_id,property.address,description as common_amenity,hour as common_amenity_hour,cost as common_amenity_cost,capacity as common_amenity_capacity 
    FROM 
        (property 
        LEFT OUTER JOIN prop_common ON property.prop_id = prop_common.prop_id)
    LEFT OUTER JOIN amenity ON prop_common.amenity_id = amenity.amenity_id
    LEFT OUTER JOIN common_amenity ON prop_common.amenity_id = common_amenity.amenity_id order by property.prop_id;
    -------TYPE
    CREATE OR REPLACE TYPE property_common_obj AS OBJECT (
        prop_id NUMBER(5),
        address VARCHAR2(20),
        amenity_id NUMBER(5),
        common_amenity VARCHAR2(20),
        common_amenity_hour NUMBER,
        common_amenity_cost NUMBER,
        common_amenity_capacity NUMBER
    );
    
    CREATE OR REPLACE TYPE property_common_tab IS TABLE OF property_common_obj;
    
    
    -------FUNCTION
    CREATE OR REPLACE FUNCTION view_property_common(p_prop_id in CHAR)
        RETURN property_common_tab PIPELINED
    IS
    BEGIN
        IF p_prop_id IS NOT NULL THEN
            FOR rec IN (SELECT property.prop_id,property.address,common_amenity.amenity_id,description as common_amenity,hour as common_amenity_hour,cost as common_amenity_cost,capacity as common_amenity_capacity 
                        FROM 
                            (property 
                            LEFT OUTER JOIN prop_common ON property.prop_id = prop_common.prop_id)
                        LEFT OUTER JOIN amenity ON prop_common.amenity_id = amenity.amenity_id
                        LEFT OUTER JOIN common_amenity ON prop_common.amenity_id = common_amenity.amenity_id
                        WHERE property.prop_id = p_prop_id order by property.prop_id)
            LOOP
                PIPE ROW (property_common_obj(rec.prop_id, rec.address, rec.amenity_id, rec.common_amenity, rec.common_amenity_hour, rec.common_amenity_cost, rec.common_amenity_capacity));
            END LOOP;
        ELSE
            FOR rec IN (SELECT property.prop_id,property.address,common_amenity.amenity_id,description as common_amenity,hour as common_amenity_hour,cost as common_amenity_cost,capacity as common_amenity_capacity 
                        FROM 
                            (property 
                            LEFT OUTER JOIN prop_common ON property.prop_id = prop_common.prop_id)
                        LEFT OUTER JOIN amenity ON prop_common.amenity_id = amenity.amenity_id
                        LEFT OUTER JOIN common_amenity ON prop_common.amenity_id = common_amenity.amenity_id order by property.prop_id)
            LOOP
                PIPE ROW (property_common_obj(rec.prop_id, rec.address, rec.amenity_id,rec.common_amenity, rec.common_amenity_hour,rec.common_amenity_cost, rec.common_amenity_capacity));
            END LOOP;
        END IF;
    
        RETURN;
    END;
    
    select * from ((view_property_common(6)));
    select * from ((view_property_common(null)));




--Private Amenity Summary
    ------SQL
    with property_apt as (
        select * from property natural join apartment
    )
    SELECT property_apt.prop_id, property_apt.address, property_apt.apt_id, description, cost , feature , warranty 
    FROM
        (property_apt LEFT OUTER JOIN apt_private ON property_apt.apt_id = apt_private.apt_id AND property_apt.prop_id = apt_private.prop_id)
    LEFT OUTER JOIN amenity ON apt_private.amenity_id = amenity.amenity_id
    LEFT OUTER JOIN private_amenity ON private_amenity.amenity_id = apt_private.amenity_id order by property_apt.prop_id, property_apt.apt_id;
    
    -------TYPE
    CREATE TYPE apartment_private_obj AS OBJECT (
        prop_id NUMBER(5),
        address VARCHAR(20),
        apt_id NUMBER(5),
        amenity_id NUMBER(5),
        description VARCHAR(20),
        cost NUMBER,
        feature VARCHAR(20),
        warranty VARCHAR(20)
    );
    CREATE TYPE apartment_private_tab IS TABLE OF apartment_private_obj;
    -------FUNCTION
    CREATE OR REPLACE FUNCTION view_apartment_private(p_prop_id in CHAR, a_apt_id in CHAR)
        RETURN apartment_private_tab PIPELINED
    IS
    BEGIN
        IF p_prop_id IS NOT NULL AND a_apt_id IS NOT NULL THEN
            FOR rec IN (with property_apt as (
                            select * from property natural join apartment
                        )
                        SELECT property_apt.prop_id, property_apt.address, property_apt.apt_id, apt_private.amenity_id, description, cost , feature , warranty 
                        FROM
                            (property_apt LEFT OUTER JOIN apt_private ON property_apt.apt_id = apt_private.apt_id AND property_apt.prop_id = apt_private.prop_id)
                        LEFT OUTER JOIN amenity ON apt_private.amenity_id = amenity.amenity_id
                        LEFT OUTER JOIN private_amenity ON private_amenity.amenity_id = apt_private.amenity_id
                        WHERE property_apt.prop_id = p_prop_id AND property_apt.apt_id = a_apt_id order by property_apt.prop_id, property_apt.apt_id)
            LOOP
                PIPE ROW (apartment_private_obj(rec.prop_id, rec.address, rec.apt_id,rec.amenity_id, rec.description, rec.cost, rec.feature, rec.warranty));
            END LOOP;
        ELSE
            FOR rec IN (with property_apt as (
                            select * from property natural join apartment
                        )
                        SELECT property_apt.prop_id, property_apt.address, property_apt.apt_id,apt_private.amenity_id, description, cost , feature , warranty 
                        FROM
                            (property_apt LEFT OUTER JOIN apt_private ON property_apt.apt_id = apt_private.apt_id AND property_apt.prop_id = apt_private.prop_id)
                        LEFT OUTER JOIN amenity ON apt_private.amenity_id = amenity.amenity_id
                        LEFT OUTER JOIN private_amenity ON private_amenity.amenity_id = apt_private.amenity_id order by property_apt.prop_id, property_apt.apt_id)
            LOOP
                PIPE ROW (apartment_private_obj(rec.prop_id, rec.address, rec.apt_id,rec.amenity_id, rec.description, rec.cost, rec.feature, rec.warranty));
            END LOOP;
        END IF;
        RETURN;
    END;
    
    select * from (view_apartment_private(null,null));




--Property & Apartment & Lease
    with property_apt as (
        select * from property natural join apartment
    )
    select property_apt.prop_id, property_apt.address, property_apt.apt_id, rent, apt_size, bedroom_num, bathroom_num, lease_id
    from property_apt left outer join lease on property_apt.prop_id = lease.prop_id and property_apt.apt_id = lease.apt_id order by property_apt.prop_id, property_apt.apt_id;
    
    CREATE TYPE apartment_lease_obj AS OBJECT (
        prop_id NUMBER(5),
        address VARCHAR(20),
        apt_id NUMBER(5),
        rent NUMBER,
        apt_size NUMBER,
        bedroom_num NUMBER,
        bathroom_num NUMBER,
        lease_id NUMBER(5),
        term NUMBER,
        start_date DATE,
        depoist NUMBER
    );
    CREATE TYPE apartment_lease_tab IS TABLE OF apartment_lease_obj;
    
    CREATE OR REPLACE FUNCTION view_apartment_lease(p_prop_id in CHAR, a_apt_id in CHAR, l_lease_id in CHAR)
        RETURN apartment_lease_tab PIPELINED
    IS
    BEGIN
        IF p_prop_id IS NOT NULL AND a_apt_id IS NOT NULL THEN
            FOR rec IN (with property_apt as (
                            select * from property natural join apartment
                        )
                        select property_apt.prop_id, property_apt.address, property_apt.apt_id, rent, apt_size, bedroom_num, bathroom_num, lease_id,term, start_date,deposit
                        from property_apt left outer join lease on property_apt.prop_id = lease.prop_id and property_apt.apt_id = lease.apt_id
                        WHERE property_apt.prop_id = p_prop_id AND property_apt.apt_id = a_apt_id order by property_apt.prop_id, property_apt.apt_id )
            LOOP
                PIPE ROW (apartment_lease_obj(rec.prop_id, rec.address, rec.apt_id, rec.rent, rec.apt_size, rec.bedroom_num, rec.bathroom_num, rec.lease_id, rec.term, rec.start_date, rec.deposit));
            END LOOP;
        ELSIF l_lease_id IS NOT NULL THEN
            FOR rec IN (with property_apt as (
                            select * from property natural join apartment
                        )
                        select property_apt.prop_id, property_apt.address, property_apt.apt_id, rent, apt_size, bedroom_num, bathroom_num, lease_id,term, start_date,deposit
                        from property_apt left outer join lease on property_apt.prop_id = lease.prop_id and property_apt.apt_id = lease.apt_id
                        WHERE lease.lease_id = l_lease_id order by property_apt.prop_id, property_apt.apt_id)
            LOOP
                PIPE ROW (apartment_lease_obj(rec.prop_id, rec.address, rec.apt_id, rec.rent, rec.apt_size, rec.bedroom_num, rec.bathroom_num, rec.lease_id,rec.term, rec.start_date, rec.deposit));
            END LOOP;
        ELSE
            FOR rec IN (with property_apt as (
                            select * from property natural join apartment
                        )
                        select property_apt.prop_id, property_apt.address, property_apt.apt_id, rent, apt_size, bedroom_num, bathroom_num, lease_id,term, start_date,deposit
                        from property_apt left outer join lease on property_apt.prop_id = lease.prop_id and property_apt.apt_id = lease.apt_id order by property_apt.prop_id, property_apt.apt_id)
            LOOP
                PIPE ROW (apartment_lease_obj(rec.prop_id, rec.address, rec.apt_id, rec.rent, rec.apt_size, rec.bedroom_num, rec.bathroom_num, rec.lease_id, rec.term, rec.start_date, rec.deposit));
            END LOOP;
        END IF;
        RETURN;
    END;
    
    select * from (view_apartment_lease(null,null,3));
    
    
    
    ----SearchByRent+Address+Amenities   
   with amenity_summary as (
    select prop_id, address, apt_id, rent, amenity_id from (select * from property natural join apartment) natural join apt_private
    union
    select prop_id, address, apt_id, rent, amenity_id from (select * from property natural join apartment) natural join prop_common
   )
   select distinct prop_id, address, apt_id from amenity_summary;
    
    
    ----Amenity Availble at a given apartment
    with amenity_summary as (
        select prop_id, address, apt_id, rent, amenity_id from (select * from property natural join apartment) natural join apt_private
        union
        select prop_id, address, apt_id, rent, amenity_id from (select * from property natural join apartment) natural join prop_common
       )
       select * from amenity_summary;