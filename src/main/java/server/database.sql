-- Active: 1696603788118@@127.0.0.1@3306


create database `grizzly_entertainment_equipment_rental`  
    DEFAULT CHARACTER SET = 'utf8mb4';

use `grizzly_entertainment_equipment_rental`;

-- drop database grizzly_entertainment_equipment_rental;

CREATE TABLE `customer` (
    `customer_id` BIGINT NOT NULL PRIMARY KEY,
    `first_name` VARCHAR(20) NOT NULL,
    `last_name` VARCHAR(20) NOT NULL,
    `email` VARCHAR(30) NOT NULL,
    `phone` VARCHAR(10) NOT NULL
);

CREATE TABLE `employee` (
    `employee_id` BIGINT NOT NULL PRIMARY KEY,
    `first_name` VARCHAR(20) NOT NULL,
    `last_name` VARCHAR(20) NOT NULL,
    `email` VARCHAR(30) NOT NULL,
    `phone` VARCHAR(10) NOT NULL
);

-- drop table address;
CREATE TABLE `address` (
    `address_id` int AUTO_INCREMENT PRIMARY KEY,
    `customer_id` BIGINT,
    `employee_id` BIGINT,
    `street_name` VARCHAR(20),
    `street_number` VARCHAR(10),
    `city` VARCHAR(20),
    `parish_state` VARCHAR(20),
    `zip_code` VARCHAR(10),
    `country` VARCHAR(30),
    FOREIGN KEY (`customer_id`) REFERENCES `customer`(`customer_id`),
    FOREIGN KEY (`employee_id`) REFERENCES `employee`(`employee_id`)
);


CREATE TABLE `customer_authentication` (
    `customer_id` BIGINT NOT NULL PRIMARY KEY,
    `password` VARCHAR(65) NOT NULL,
    FOREIGN KEY (`customer_id`) REFERENCES `customer`(`customer_id`)
);

CREATE TABLE `employee_authentication` (
    `employee_id` BIGINT NOT NULL PRIMARY KEY,
    `password` VARCHAR(65) NOT NULL,
    FOREIGN KEY (`employee_id`) REFERENCES `employee`(`employee_id`)
);

SELECT * from equipment;
delete from equipment where equipment_id = 3;
CREATE TABLE `equipment` (
    `equipment_id` BIGINT NOT NULL PRIMARY KEY,
    `equipment_name` VARCHAR(40) NOT NULL,
    `category` VARCHAR(9) NOT NULL, -- sounding, powering, lighting, staging
    `availability_status` VARCHAR(10) NOT NULL, -- booked or available
    `quantity` INT NOT NULL,
    `price` DECIMAL(10, 2)
);


-- drop table rental_request;

CREATE TABLE `rental_request` (
    `request_id` int AUTO_INCREMENT PRIMARY KEY,
    `customer_id` BIGINT NOT NULL,
    `equipment_id` BIGINT NOT NULL,
    `date_requested` DATE NOT NULL,
    FOREIGN KEY (`customer_id`) REFERENCES `customer`(`customer_id`),
    FOREIGN KEY (`equipment_id`) REFERENCES `equipment`(`equipment_id`)
);


CREATE TABLE `transactions` (
    `transaction_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `customer_id` BIGINT NOT NULL,
    `equipment_id` BIGINT NOT NULL,
    `dateTransacted` DATE NOT NULL,
    `cost` DECIMAL(10, 2),
    FOREIGN KEY (`customer_id`) REFERENCES `customer`(`customer_id`),
    FOREIGN KEY (`equipment_id`) REFERENCES `equipment`(`equipment_id`)
);


drop table messages;
CREATE TABLE `messages` (
    `message_id` int AUTO_INCREMENT PRIMARY KEY,
    `message_type` int,
    `customer_id` BIGINT,
    `employee_id` BIGINT,
    `message` VARCHAR(500)
    -- Foreign Key (`customer_id`) REFERENCES `customer`(`customer_id`),
    -- Foreign Key (`employee_id`) REFERENCES `employee`(`employee_id`)
);


ALTER table `messages`
ADD constraint `fk_messages_customer`
Foreign Key (`customer_id`) 
REFERENCES `customer`(`customer_id`);


ALTER table `messages`
ADD constraint `fk_messages_employee`
FOREIGN KEY (`employee_id`)
REFERENCES `employee`(`employee_id`);


create table `generator` (
    `generator_id` int AUTO_INCREMENT PRIMARY KEY,
    `customer_constant` BIGINT not null,
    `employee_constant` BIGINT not null
);


--- testing section

drop table generator;

insert into generator (`customer_constant`, `employee_constant`) VALUES (100000, 111111);
select * from generator;

select * from generator;

select * from rental_request;

select * from address;

select * from customer;
select * from employee;


-- delete from `customer_generator` where `constant_value` = 100000;

select * from `customer` where `customer_id` = 100002;
select * from `address` where `customer_id` = 100002;
select * from `customer_authentication` where `customer_id` = 100002;

select * from `customer`;
select * from `employee`;
select * from `address`;
select * from `customer_authentication`;
select * from `employee_authentication`;

drop table address;
drop table customer;
drop table customer_authentication;

drop table rental_request;
drop table transactions;


delete from `address` where `customer_id` = 100002;
delete from `customer_authentication` where `customer_id` = -999999;
delete from `customer` where `customer_id` = -999999;

delete from `customer` where customer_id = 123;
delete from `address` where customer_id = 123;
delete from `customer_authentication` where customer_id = 123;
select * from `address`;
select * from `customer_authentication`;

select * from equipment;


create table `image_content` (
    `image_id` int AUTO_INCREMENT NOT NULL PRIMARY KEY,
    `image_blob` LONGBLOB NOT NULL,
    `image_name` varchar(20)
);

-- DROP TABLE `IMAGE_CONTENT`

SELECT * FROM IMAGE_CONTENT;




INSERT INTO equipment (equipment_id, equipment_name, category, availability_status, quantity, price)
VALUES
(100001, 'Generator - 5kW', 'powering', 'Available', 2, 3500.35),
(100002, 'Power Distribution Box', 'powering', 'Available', 2, 2000.50),
(100003, 'Extension Cables', 'powering', 'Available', 2, 1800.90),
(100004, 'Voltage Stabilizer - 10kVA', 'powering', 'Available', 2, 1800.35),
(100005, 'Battery Backup System', 'powering', 'Available', 2, 3000.28),
(100006, 'Power Inverter - 2000W', 'powering', 'Available', 2, 1900.82),
(100007, 'Power Strip with Surge Protector', 'powering', 'Available', 2, 1593.03),
(100008, 'Outdoor Power Box', 'powering', 'Available', 2, 9358.35),
(100009, 'Circuit Breaker Panel', 'powering', 'Available', 2, 3800.50),
(100010, 'Industrial-grade Extension Cords', 'powering', 'Available', 2, 852.933),
(100011, 'UPS (Uninterruptible Power Supply) - 1500VA', 'powering', 'Available', 2, 852.58),
(100012, 'Power Cable Protectors', 'powering', 'Available', 2, 750.68);


ALTER TABLE equipment MODIFY equipment_name VARCHAR(100) NOT NULL;



INSERT INTO equipment (equipment_id, equipment_name, category, availability_status, quantity, price)
VALUES
    (200001, 'PA System', 'sounding', 'available', 2, 3499.36),
    (200002, 'Microphone Set', 'sounding', 'available',2, 2500.25),
    (200003, 'Mixer Board', 'sounding', 'available', 2, 2896.93),
    (200004, 'Speakers - Pair', 'sounding', 'available', 2, 252.69),
    (200005, 'Subwoofer', 'sounding', 'available', 2, 355.95),
    (200006, 'Amplifier', 'sounding', 'available',2, 4306.64),
    (200007, 'Audio Interface', 'sounding', 'available', 2, 8269.64),
    (200008, 'Wireless Microphone System', 'sounding', 'available', 2, 362.39),
    (200009, 'In-Ear Monitor System', 'sounding', 'available', 2, 8212.35),
    (200010, 'Digital Mixer', 'sounding', 'available', 2, 3921.35),
    (200011, 'DI Box', 'sounding', 'available', 2, 2252.80),
    (200012, 'Audio Snake Cable', 'sounding', 'available', 2,3000.35);



INSERT INTO equipment (equipment_id, equipment_name, category, availability_status, quantity, price)
VALUES
    (300001, 'Stage Lights - LED', 'lighting', 'available', 2, 3985.50),
    (300002, 'Spotlight', 'lighting', 'available', 2, 6100.70),
    (300003, 'Fog Machine', 'lighting', 'available', 2, 4000.20),
    (300004, 'Strobe Lights', 'lighting', 'available', 2, 5300.35),
    (300005, 'Intelligent Moving Head Lights', 'lighting', 'available', 2, 2400.80),
    (300006, 'UV Blacklight', 'lighting', 'available', 12, 1985.53),
    (300007, 'Gobo Projector', 'lighting', 'available', 4, 2950.50),
    (300008, 'Laser Light System', 'lighting', 'available', 7,8500.52),
    (300009, 'Par Can Lights', 'lighting', 'available', 15, 3300.21),
    (300010, 'Dance Floor Lighting', 'lighting', 'available', 2, 1993.35),
    (300011, 'Pixel Mapping Lights', 'lighting', 'available', 9, 1985.3),
    (300012, 'Emergency Exit Signs', 'lighting', 'available', 8, 1859.19);


INSERT INTO equipment (equipment_id, equipment_name, category, availability_status, quantity, price)
VALUES
    (400001, 'Portable Stage - 4x4', 'staging', 'available', 2, 3592.95),
    (400002, 'Podium', 'staging', 'available', 3, 3200.35),
    (400003, 'Crowd Control Barriers', 'staging', 'available', 8, 2932.34),
    (400004, 'Stage Platforms', 'staging', 'available', 5, 2993.39),
    (400005, 'Stage Backdrops', 'staging', 'available', 4, 2000.35),
    (400006, 'Stage Curtains', 'staging', 'available', 7, 3992.03),
    (400007, 'Stage Risers', 'staging', 'available', 6, 2993.39),
    (400008, 'Stage Skirting', 'staging', 'available', 3, 4000.34),
    (400009, 'Stage Props and Decoration', 'staging', 'available', 9, 2999.99),
    (400010, 'Stage Step Stairs', 'staging', 'available', 5, 2499.50),
    (400011, 'Stage Carpeting', 'staging', 'available', 10, 1999.99),
    (400012, 'Stage Ramp', 'staging', 'available', 2, 1200.35);


UPDATE equipment
SET QUANTITY = 2
WHERE QUANTITY > 2;

select * from equipment;

delete from equipment where equipment_id IN (400001, 400006, 400010);
delete from equipment where equipment_id IN (100011, 100006, 100010);
delete from equipment where equipment_id IN (300002, 300006, 300009);
delete from equipment where equipment_id IN (200009, 200010, 200011);


-- testing


select equipment_id from equipment;


select * from messages;