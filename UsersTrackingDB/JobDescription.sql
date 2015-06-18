-- phpMyAdmin SQL Dump
-- version 3.5.8.1deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generato il: Lug 23, 2013 alle 10:37
-- Versione del server: 5.5.31-0ubuntu0.13.04.1
-- Versione PHP: 5.4.9-4ubuntu2.2

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `userstracking`
--

-- --------------------------------------------------------

--
-- Struttura della tabella `JobDescription`
--
DROP TABLE `JobDescription`
CREATE TABLE IF NOT EXISTS `JobDescription` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `jobId` varchar(300) DEFAULT NULL,
  `executable` varchar(100) NOT NULL,
  `arguments` varchar(300) DEFAULT NULL,
  `output` varchar(300) DEFAULT NULL,
  `error` varchar(300) DEFAULT NULL,
  `queue` varchar(300) DEFAULT NULL,
  `file_transfer` varchar(300) DEFAULT NULL,
  `total_cpu` varchar(4) DEFAULT NULL,
  `SPDM_variation` varchar(300) DEFAULT NULL,
  `number_of_processes` varchar(4) DEFAULT NULL,
  `JDL_requirements` varchar(300) DEFAULT NULL,
  `output_path` varchar(300) DEFAULT NULL,
  `input_files` text,
  `output_files` text,
  `proxy_renewal` char(1) DEFAULT NULL,
  `resubmit_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
