----Sign A lease
CREATE OR REPLACE FUNCTION sign_lease(
    p_tenant_id NUMBER,
    p_prop_id NUMBER,
    p_apt_id NUMBER,
    p_start_date VARCHAR,
    p_deposit NUMBER,
    p_ssn NUMBER
) RETURN NUMBER IS
    v_lease_id NUMBER;
    v_count NUMBER;
BEGIN
    -- Check if tenant has visited the specified property and apartment
    SELECT COUNT(*)
    INTO v_count
    FROM visits
    WHERE tenent_id = p_tenant_id 
    AND prop_id = p_prop_id 
    AND apt_id = p_apt_id;

    IF v_count > 0 THEN
        -- Insert into Lease table
        INSERT INTO lease(prop_id, apt_id, term, start_date, deposit)
        VALUES (p_prop_id, p_apt_id, 12, TO_DATE(p_start_date, 'yyyy/mm/dd'), p_deposit)
        RETURNING lease_id INTO v_lease_id;

        -- Insert into Tenant table
        INSERT INTO tenant(id, lease_id, ssn)
        VALUES (p_tenant_id, v_lease_id, p_ssn);

        -- Return the new lease ID
        RETURN v_lease_id;
    ELSE
        -- Return null or raise an exception if the tenant hasn't visited
        RETURN 0;
    END IF;
END;
