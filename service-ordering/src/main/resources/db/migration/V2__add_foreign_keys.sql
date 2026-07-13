ALTER TABLE credential
    ADD CONSTRAINT fk_credential_member FOREIGN KEY (member_id) REFERENCES member (member_id);
