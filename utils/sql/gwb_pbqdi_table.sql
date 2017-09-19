-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               5.7.17-log - MySQL Community Server (GPL)
-- Server OS:                    Win64
-- HeidiSQL Version:             9.4.0.5173
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


-- Dumping database structure for gework_web
CREATE DATABASE IF NOT EXISTS `gework_web` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `gework_web`;

-- Dumping structure for table gework_web.pbqdiresult
CREATE TABLE IF NOT EXISTS `pbqdiresult` (
  `ID` bigint(20) NOT NULL,
  `CONSISTENCYVERSION` bigint(20) NOT NULL,
  `JOBID` int(11) DEFAULT NULL,
  `OWNER` bigint(20) DEFAULT NULL,
  `SAMPLEFILENAME` varchar(255) DEFAULT NULL,
  `SUBTYPES` longblob,
  `TIMESTAMP` datetime DEFAULT NULL,
  `TUMORTYPE` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
