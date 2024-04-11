
------Monthly BILL
CREATE OR REPLACE TYPE total_bill_record AS OBJECT (
    lease_id NUMBER(5),
    apartment_rent NUMBER(7,2),
    total_amenity_cost NUMBER(7,2),
    total_monthly_bill NUMBER(7,2)
);


CREATE OR REPLACE TYPE total_bill_table AS TABLE OF total_bill_record;


CREATE OR REPLACE FUNCTION calculate_total_bill(p_lease_id NUMBER) 
RETURN total_bill_table PIPELINED 
IS
    bill total_bill_record;
BEGIN
    FOR rec IN (
        SELECT
            l.lease_id,
            a.rent AS apartment_rent,
            COALESCE(SUM(am.cost), 0) AS total_amenity_cost,
            (a.rent + COALESCE(SUM(am.cost), 0)) AS total_monthly_bill
        FROM
            lease l
        JOIN
            apartment a ON l.prop_id = a.prop_id AND l.apt_id = a.apt_id
        LEFT JOIN
            select_amenity sa ON l.lease_id = sa.lease_id
        LEFT JOIN
            amenity am ON sa.amenity_id = am.amenity_id
        WHERE
            l.lease_id = p_lease_id
        GROUP BY
            l.lease_id, a.rent
    ) LOOP
        bill := total_bill_record(rec.lease_id, rec.apartment_rent, rec.total_amenity_cost, rec.total_monthly_bill);
        PIPE ROW(bill);
    END LOOP;
    
    RETURN;
END;
select * from calculate_total_bill(20);
drop function calculate_total_bill;





----Calculate How many unpaid payment
CREATE OR REPLACE FUNCTION calculate_due_rent_payments(p_lease_id NUMBER) RETURN NUMBER IS
    v_most_recent_payment DATE;
    v_start_date DATE;
    v_due_payments NUMBER;
BEGIN
    -- find the most recent payment date for the lease
    BEGIN
        SELECT MAX(payment_date)
        INTO v_most_recent_payment
        FROM payment
        WHERE lease_id = p_lease_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_most_recent_payment := NULL;
    END;

    -- Get the start date of the lease
    SELECT start_date
    INTO v_start_date
    FROM lease
    WHERE lease_id = p_lease_id;

    -- Calculate the number of rent payments due
    IF v_most_recent_payment IS NULL THEN
        -- If no payment has been made, calculate from the lease's start date
        v_due_payments := MONTHS_BETWEEN(SYSDATE, v_start_date);
    ELSE
        -- calculate from the most recent payment date
        v_due_payments := MONTHS_BETWEEN(SYSDATE, v_most_recent_payment);
    END IF;

    
    v_due_payments := FLOOR(v_due_payments);

    RETURN v_due_payments;
EXCEPTION
    WHEN OTHERS THEN
        -- Handle other exceptions as necessary
        RETURN NULL;
END;
select * from tenant;
select * from lease natural join tenant where lease_id = 3;
select * from payment;
select * from transfer;
SELECT calculate_due_rent_payments(1) FROM dual;
select * from credit_debit where payment_id = 1;
