----Trigger to Check if a tenant is a pet
CREATE OR REPLACE TRIGGER check_pet_before_tenant_insert_update
BEFORE INSERT OR UPDATE ON tenant
FOR EACH ROW
DECLARE
    pet_count NUMBER;
BEGIN
    -- Check if the id exists in the pet table
    SELECT COUNT(*)
    INTO pet_count
    FROM pet
    WHERE id = :NEW.id;

    -- If the id exists in the pet table, set ssn to all zeros
    IF pet_count > 0 THEN
        :NEW.ssn := 0;
    END IF;
END;
drop trigger check_pet_before_tenant_insert_update;

----Trigger to Check if a customer has already visit a apartment 
----before sign a lease
CREATE OR REPLACE TRIGGER check_visit_before_lease
BEFORE INSERT ON tenant
FOR EACH ROW
DECLARE
    v_count NUMBER;
BEGIN
    -- Check if the lease_id in the lease table exists in the visits table
    SELECT COUNT(*)
    INTO v_count
    FROM visits v
    JOIN lease l ON v.prop_id = l.prop_id AND v.apt_id = l.apt_id
    WHERE l.lease_id = :NEW.lease_id AND v.tenent_id = :NEW.id;

    -- If the lease_id is not found in the visits table, do not allow the insert and delete the lease
    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Tenant has not visited the apartment. Lease and Tenant insertion cancelled.');
    END IF;
END;

drop trigger check_visit_before_lease;




--Trigger to prevent same amenity being common and private at the same time
CREATE OR REPLACE TRIGGER check_common_amenity
BEFORE INSERT ON common_amenity
FOR EACH ROW
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM private_amenity
    WHERE amenity_id = :NEW.amenity_id;

    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'This amenity is already listed as a private amenity.');
    END IF;
END;

CREATE OR REPLACE TRIGGER check_private_amenity
BEFORE INSERT ON private_amenity
FOR EACH ROW
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM common_amenity
    WHERE amenity_id = :NEW.amenity_id;

    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'This amenity is already listed as a common amenity.');
    END IF;
END;




---Trigger to prevent same prospective tenant being person and pet at the same time
CREATE OR REPLACE TRIGGER check_person
BEFORE INSERT ON person
FOR EACH ROW
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM pet
    WHERE id = :NEW.id;

    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'This prospect tenant is already listed as a pet.');
    END IF;
END;

CREATE OR REPLACE TRIGGER check_pet
BEFORE INSERT ON pet
FOR EACH ROW
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM person
    WHERE id = :NEW.id;

    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'This prospect tenant is already listed as a person.');
    END IF;
END;


CREATE OR REPLACE TRIGGER check_transfer
BEFORE INSERT ON transfer
FOR EACH ROW
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM credit_debit
    WHERE payment_id = :NEW.payment_id;

    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'This payment is already recorded as a card payment.');
    END IF;
END;

CREATE OR REPLACE TRIGGER check_card
BEFORE INSERT ON credit_debit
FOR EACH ROW
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM transfer
    WHERE payment_id = :NEW.payment_id;

    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'This payment is already recorded as a card payment.');
    END IF;
END;




