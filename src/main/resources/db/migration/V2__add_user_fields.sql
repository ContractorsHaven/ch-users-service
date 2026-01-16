ALTER TABLE users
    ADD COLUMN first_name VARCHAR(100),
    ADD COLUMN last_name VARCHAR(100),
    ADD COLUMN mobile_number VARCHAR(20),
    ADD COLUMN created_by VARCHAR(255),
    ADD COLUMN modified_by VARCHAR(255);