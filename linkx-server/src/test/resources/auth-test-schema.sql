DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    nickname VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    mobile VARCHAR(32),
    email VARCHAR(128),
    avatar_url VARCHAR(255),
    gender TINYINT DEFAULT 0,
    status TINYINT DEFAULT 1,
    last_login_time TIMESTAMP,
    last_login_ip VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE UNIQUE INDEX uk_username ON sys_user (username);
CREATE UNIQUE INDEX uk_mobile ON sys_user (mobile);
CREATE UNIQUE INDEX uk_email ON sys_user (email);
