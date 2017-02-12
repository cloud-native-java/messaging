DROP TABLE contact IF EXISTS;

CREATE TABLE contact(
    id IDENTITY primary key,
    email VARCHAR (20),
    full_name VARCHAR(20),
    valid_email BOOLEAN NULL
);