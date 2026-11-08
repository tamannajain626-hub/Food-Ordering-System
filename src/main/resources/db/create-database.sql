-- ============================================================
--  Food Ordering System - database setup
-- ============================================================
--  Run this ONCE, before starting the Spring Boot application:
--
--      /usr/local/mysql/bin/mysql -u root -p < src/main/resources/db/create-database.sql
--
--  (it will ask you for your MySQL password)
-- ============================================================

-- Creates the database if it does not exist yet.
CREATE DATABASE IF NOT EXISTS food_ordering
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE food_ordering;

-- ------------------------------------------------------------
--  NOTE:
--  You do NOT need to create the tables by hand.
--  Hibernate creates them automatically when the application starts
--  (see spring.jpa.hibernate.ddl-auto=update in application.properties).
--
--  The "customer" table it creates looks like this:
--
--  CREATE TABLE customer (
--      customer_id BIGINT       NOT NULL AUTO_INCREMENT,
--      name         VARCHAR(100) NOT NULL,
--      email        VARCHAR(100) NOT NULL,
--      phone        VARCHAR(20)  NOT NULL,
--      PRIMARY KEY (customer_id),
--      UNIQUE KEY uk_customer_email (email)
--  );
--
--  The "menu_item" table (Phase 2) looks like this:
--
--  CREATE TABLE menu_item (
--      item_id      BIGINT        NOT NULL AUTO_INCREMENT,
--      name         VARCHAR(100)  NOT NULL,
--      description  VARCHAR(500),
--      price        DECIMAL(10,2) NOT NULL,
--      category     VARCHAR(50)   NOT NULL,
--      image_url    VARCHAR(500),
--      available    TINYINT(1)    NOT NULL,
--      PRIMARY KEY (item_id),
--      UNIQUE KEY uk_menu_item_name (name)
--  );
--
--  The sample dishes are added by the application itself the first
--  time it starts. They are never added twice.
-- ------------------------------------------------------------
