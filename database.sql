-- SQL Server 2017+; run with SSMS or sqlcmd -f 65001 -b -i database.sql
USE master;
GO
IF DB_ID('WebExam02_24162023') IS NULL CREATE DATABASE WebExam02_24162023;
GO
USE WebExam02_24162023;
GO
-- Do not overwrite existing work. Re-running against an existing schema stops here.
IF OBJECT_ID('dbo.books','U') IS NOT NULL
 THROW 50001, 'Schema already exists. Use the existing database or explicitly choose a fresh database.', 1;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;
CREATE TABLE books (
 bookid int IDENTITY(1,1) PRIMARY KEY, isbn int NULL, title varchar(200) NULL,
 publisher varchar(100) NULL, price decimal(6,2) NULL, description text NULL,
 publish_date date NULL, cover_image varchar(100) NULL, quantity int NULL
);
CREATE TABLE users (
 id int IDENTITY(1,1) PRIMARY KEY, email varchar(50) NOT NULL,
 fullname nvarchar(50) NULL, phone int NULL, passwd varchar(32) NOT NULL,
 signup_date datetime NULL, last_login datetime NULL, is_admin bit NULL
);
CREATE TABLE author (
 author_id int IDENTITY(1,1) PRIMARY KEY, author_name varchar(100) NULL, date_of_birth date NULL
);
CREATE TABLE book_author (
 bookid int NOT NULL, author_id int NOT NULL,
 PRIMARY KEY(bookid,author_id),
 FOREIGN KEY(bookid) REFERENCES books(bookid),
 FOREIGN KEY(author_id) REFERENCES author(author_id)
);
CREATE TABLE rating (
 userid int NOT NULL, bookid int NOT NULL, rating tinyint NULL, review_text text NULL,
 PRIMARY KEY(userid,bookid),
 FOREIGN KEY(userid) REFERENCES users(id),
 FOREIGN KEY(bookid) REFERENCES books(bookid)
);
INSERT users(email,fullname,phone,passwd,signup_date,is_admin) VALUES
('admin@example.com',N'Quản trị viên',901234567,'zA/wK7Gm5s0gnTVfLsrXT2S905f1bYNr',GETDATE(),1),
('user1@example.com',N'Nguyễn Văn An',902345678,'k0FNAJIHdF3Q2rhtzAr7NiMsYtRFP3eV',GETDATE(),0),
('user2@example.com',N'Trần Thị Bình',903456789,'tILAPdsK2LNUnx0WEwPgTI4gXqMiibQP',GETDATE(),0);
INSERT author(author_name,date_of_birth) VALUES
('Nguyen Nhat Anh','1955-05-07'),('Robert Martin','1952-12-05'),('Martin Fowler','1963-12-18');
INSERT books(isbn,title,publisher,price,description,publish_date,cover_image,quantity) VALUES
(100001,'Cho toi xin mot ve di tuoi tho','Tre',85.00,'Cau chuyen tuoi tho va tinh ban.','2008-01-01','assets/cover.svg',12),
(100002,'Mat biec','Tre',95.00,'Mot cau chuyen ve ky uc.','1990-01-01','assets/cover.svg',15),
(100003,'Toi thay hoa vang tren co xanh','Tre',110.00,'Tuoi tho o lang que.','2010-01-01','assets/cover.svg',8),
(100004,'Co gai den tu hom qua','Tre',75.00,'Cau chuyen hoc tro.','1989-01-01','assets/cover.svg',20),
(100005,'Ngoi khoc tren cay','Tre',125.00,'Tinh ban va su truong thanh.','2013-01-01','assets/cover.svg',10),
(100006,'Clean Code','Prentice Hall',250.00,'Writing maintainable code.','2008-08-01','assets/cover.svg',7),
(100007,'The Clean Coder','Prentice Hall',220.00,'Professional software development.','2011-05-01','assets/cover.svg',6),
(100008,'Clean Architecture','Pearson',280.00,'Software architecture principles.','2017-09-01','assets/cover.svg',11),
(100009,'Clean Agile','Pearson',190.00,'Agile software craftsmanship.','2019-09-01','assets/cover.svg',9),
(100010,'Refactoring','Addison Wesley',300.00,'Improving existing code.','2018-11-01','assets/cover.svg',13);
INSERT book_author(bookid,author_id) VALUES
(1,1),(2,1),(3,1),(4,1),(5,1),(6,2),(7,2),(8,2),(9,2),(10,3),(6,3);
INSERT rating(userid,bookid,rating,review_text) VALUES
(2,1,5,'Sach rat hay.'),(3,1,4,'Noi dung tot.'),(2,6,5,'Useful examples.'),(3,10,5,'Highly recommended.');
COMMIT;
GO
