-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: jpast3
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `CategoryId` int NOT NULL AUTO_INCREMENT,
  `Status` int DEFAULT NULL,
  `Categoryname` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `Images` text,
  PRIMARY KEY (`CategoryId`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,1,'Iphone','1788628916508.jpg'),(3,1,'iphone 8','1788628873137.jpg'),(5,1,'Iphone 5','c8964b0b-1ae1-4ecf-8cfe-85ac1466e648.png'),(6,1,'danh mục thử','e489cc9c-9742-4588-87ea-7b630308cfb9.png');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `email_otps`
--

DROP TABLE IF EXISTS `email_otps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_otps` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `purpose` varchar(30) NOT NULL,
  `code_hash` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `attempts` int NOT NULL DEFAULT '0',
  `used` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_otp_user_purpose` (`user_id`,`purpose`,`id`),
  KEY `idx_otp_created` (`user_id`,`purpose`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `email_otps`
--

LOCK TABLES `email_otps` WRITE;
/*!40000 ALTER TABLE `email_otps` DISABLE KEYS */;
INSERT INTO `email_otps` VALUES (1,2,'ACTIVATION','pbkdf2-sha256$600000$zcXcMFDVCrVdZcBPMcn2UA==$G6TUxMPYafamq1jQh0OHyxJtiq02dihheMRF6+eXKGk=','2026-09-06 08:25:41.917974','2026-09-06 08:30:41.917974',0,1),(2,2,'PASSWORD_RESET','pbkdf2-sha256$600000$t5DJh0znE0B1Q3m87qIYIA==$2xzhXjR0FMt0lxr/j+AosACF/BN+Zu3byddVATmYYUM=','2026-09-06 08:36:46.870553','2026-09-06 08:41:46.870553',0,1),(3,2,'PASSWORD_RESET','pbkdf2-sha256$600000$4cG3R36hJmhggc5dpW648w==$SHWysaKV1QYk2hKQGxs9QXi8aZoNQwDmEYqXYqY07ek=','2026-09-06 08:38:31.441593','2026-09-06 08:43:31.441593',5,1),(4,2,'PASSWORD_RESET','pbkdf2-sha256$600000$LP5InP7zaxxVz/OB8ra4nw==$ZTyGBzLFo4KWw9vmBzks7w+Ua7XP/3nl/nvL7cbFAwk=','2026-09-06 08:40:28.595065','2026-09-06 08:45:28.595065',0,1),(5,3,'ACTIVATION','pbkdf2-sha256$600000$0UneQgjTnHee3Z33GyB0xA==$IXXDG+hvTRr7sofp50fyS1BCtBqlSmFWv8wlF7pAKUw=','2026-09-06 14:02:58.158128','2026-09-06 14:07:58.158128',0,0);
/*!40000 ALTER TABLE `email_otps` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `description` text,
  `price` decimal(15,2) NOT NULL,
  `quantity` int NOT NULL,
  `images` varchar(255) DEFAULT NULL,
  `active` tinyint NOT NULL DEFAULT '1',
  `created_at` datetime(6) NOT NULL,
  `category_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_products_category` (`category_id`),
  KEY `idx_products_created` (`created_at`,`id`),
  CONSTRAINT `fk_products_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`CategoryId`) ON DELETE RESTRICT,
  CONSTRAINT `FKog2rp4qthbtt2lfyhfo32lsw9` FOREIGN KEY (`category_id`) REFERENCES `categories` (`CategoryId`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'iphone test 1','đẹp',15000000.00,10,'27600b8e-fd4e-4142-ac5c-08f095fd4892.png',1,'2026-09-06 09:25:59.767184',6),(2,'iphone 11','11',5000000.00,10,'7cd94781-e337-40e5-9f9d-2c3f86723db4.png',1,'2026-09-06 10:20:57.900938',1),(3,'iphone 12','12',6000000.00,10,'3c7f36ab-2513-45ce-9220-e7af9fd21603.png',1,'2026-09-06 10:21:48.957018',1),(4,'iphone 13','13',7000000.00,10,'4a5a591c-520e-438e-bb0e-892d60968832.png',1,'2026-09-06 10:22:39.039502',1),(5,'iphone 14','14',8000000.00,10,'6113c188-00eb-418e-b597-f0877e778585.png',1,'2026-09-06 10:23:27.642072',1),(6,'iphone 15','15',9000000.00,10,'09c5a798-7cf1-4067-890b-ec5f6fd37613.png',1,'2026-09-06 10:23:46.246414',1),(7,'iphone 16','16',10000000.00,10,'cd020c1d-780a-4782-b2a5-6827512f036d.png',1,'2026-09-06 10:24:30.095090',1),(9,'iphone 17','17',12000000.00,10,'7451ea04-de09-42da-8a68-be5ddea69bbc.png',1,'2026-09-06 10:26:01.235162',1),(10,'iphone 18','18',13000000.00,10,'5361d92d-cb80-485f-adde-175ace22775c.png',1,'2026-09-06 10:26:30.602948',1),(11,'iphone 19','19',14000000.00,10,'d112a273-81d6-4311-9e7f-0482104e1002.png',1,'2026-09-06 10:26:59.824976',1),(12,'iphone 20','20',15000000.00,10,'fefc34cc-df63-45fa-832d-e32f531f803b.png',1,'2026-09-06 10:27:34.361861',1);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci DEFAULT NULL,
  `fullname` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `images` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_vietnamese_ci DEFAULT NULL,
  `phone` varchar(50) COLLATE utf8mb4_vietnamese_ci DEFAULT NULL,
  `roleid` int DEFAULT NULL,
  `createDate` datetime DEFAULT NULL,
  `active` tinyint NOT NULL DEFAULT '1',
  `session_version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_username` (`username`),
  UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'quangnd','duyquang22042005@gmail.com','Nguyễn Duy Quang',NULL,'pbkdf2-sha256$600000$jfs1PiqRwaHTeQZ7SKXLdw==$4kmBODiXqPQqtJAdkrhTyG8XPwn+rys+aocTpSpC/+E=',NULL,2,NULL,1,0),(2,'quangnd1','23110290@student.hcmute.edu.vn','Duy Quang',NULL,'pbkdf2-sha256$600000$fzzqkLzcUnolbsdLHuFdYA==$aBeiVB476E/LAQ3LCFq5yx0NFdQyOFaXe6thOvnfTws=','',1,'2026-09-06 00:00:00',1,0),(3,'test123','quangduy22042005@gmail.com','test',NULL,'pbkdf2-sha256$600000$5M+Pg6tG1mJ8ieTOeK5EiQ==$x0Tyao6wi+xFvugsZkoh8SMLD4e8CkCogpIw8hAPTb0=','',1,'2026-09-06 00:00:00',0,0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `videos`
--

DROP TABLE IF EXISTS `videos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `videos` (
  `Active` int DEFAULT NULL,
  `CategoryId` int DEFAULT NULL,
  `Views` int DEFAULT NULL,
  `Description` text,
  `Poster` varchar(255) DEFAULT NULL,
  `Title` text,
  `VideoId` varchar(255) NOT NULL,
  PRIMARY KEY (`VideoId`),
  KEY `FK7t06wiw587llhee3ychrp37kl` (`CategoryId`),
  CONSTRAINT `FK7t06wiw587llhee3ychrp37kl` FOREIGN KEY (`CategoryId`) REFERENCES `categories` (`CategoryId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `videos`
--

LOCK TABLES `videos` WRITE;
/*!40000 ALTER TABLE `videos` DISABLE KEYS */;
/*!40000 ALTER TABLE `videos` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-09 20:52:28
