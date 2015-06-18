USE userstracking;

ALTER TABLE ActiveGridInteractions ADD email VARCHAR(100) after timestamp_endjob;

ALTER TABLE ActiveGridInteractions ADD e_token_server VARCHAR(100) after email;