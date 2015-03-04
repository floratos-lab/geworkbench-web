-- MySQL dump 10.13  Distrib 5.6.23, for Win64 (x86_64)
--
-- Host: localhost    Database: gework_web
-- ------------------------------------------------------
-- Server version	5.6.12

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `goresult`
--

DROP TABLE IF EXISTS `goresult`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `goresult` (
  `ID` bigint(20) NOT NULL,
  `CHANGEDGENES` longblob,
  `CONSISTENCYVERSION` bigint(20) NOT NULL,
  `REFERENCEGENES` longblob,
  `TERM2GENE` longblob,
  `TIMESTAMP` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `goresult_goresultrow`
--

DROP TABLE IF EXISTS `goresult_goresultrow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `goresult_goresultrow` (
  `ID` bigint(20) NOT NULL,
  `result_ID` bigint(20) NOT NULL,
  `GO_ID` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`,`result_ID`),
  KEY `FK_GORESULT_GORESULTROW_result_ID` (`result_ID`),
  CONSTRAINT `FK_GORESULT_GORESULTROW_result_ID` FOREIGN KEY (`result_ID`) REFERENCES `goresultrow` (`ID`),
  CONSTRAINT `FK_GORESULT_GORESULTROW_ID` FOREIGN KEY (`ID`) REFERENCES `goresult` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `goresultrow`
--

DROP TABLE IF EXISTS `goresultrow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `goresultrow` (
  `ID` bigint(20) NOT NULL,
  `CONSISTENCYVERSION` bigint(20) NOT NULL,
  `NAME` varchar(255) DEFAULT NULL,
  `NAMESPACE` varchar(255) DEFAULT NULL,
  `P` double DEFAULT NULL,
  `PADJUSTED` double DEFAULT NULL,
  `POPCOUNT` int(11) DEFAULT NULL,
  `STUDYCOUNT` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-03-04 11:47:29
