-- phpMyAdmin SQL Dump
-- version 4.4.15.1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Dec 04, 2015 at 03:24 PM
-- Server version: 5.5.44-MariaDB
-- PHP Version: 5.6.16

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `sirs26`
--
CREATE DATABASE IF NOT EXISTS `sirs26` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `sirs26`;

-- --------------------------------------------------------

--
-- Table structure for table `cl_child`
--

CREATE TABLE IF NOT EXISTS `cl_child` (
  `id` int(11) unsigned NOT NULL,
  `parent_id` int(11) unsigned NOT NULL,
  `token` varchar(13) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '72bits/6bits de letra',
  `name` varchar(254) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `cl_child_location`
--

CREATE TABLE IF NOT EXISTS `cl_child_location` (
  `child_id` int(11) unsigned NOT NULL,
  `lat` blob NOT NULL,
  `lon` blob NOT NULL,
  `accuracy` float NOT NULL DEFAULT '0',
  `last_update` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `cl_parent`
--

CREATE TABLE IF NOT EXISTS `cl_parent` (
  `id` int(11) unsigned NOT NULL,
  `email` varchar(254) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `password` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'sha256/4bits = 64',
  `salt` varchar(25) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '18*8/6 +1',
  `person_name` varchar(254) NOT NULL,
  `activation_code` varchar(64) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '0',
  `gcm_token` varchar(200) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `cl_child`
--
ALTER TABLE `cl_child`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `token` (`token`),
  ADD KEY `parent_id` (`parent_id`);

--
-- Indexes for table `cl_child_location`
--
ALTER TABLE `cl_child_location`
  ADD PRIMARY KEY (`child_id`),
  ADD UNIQUE KEY `child_id` (`child_id`),
  ADD KEY `child_id_2` (`child_id`),
  ADD KEY `child_id_3` (`child_id`);

--
-- Indexes for table `cl_parent`
--
ALTER TABLE `cl_parent`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `cl_child`
--
ALTER TABLE `cl_child`
  MODIFY `id` int(11) unsigned NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `cl_parent`
--
ALTER TABLE `cl_parent`
  MODIFY `id` int(11) unsigned NOT NULL AUTO_INCREMENT;
--
-- Constraints for dumped tables
--

--
-- Constraints for table `cl_child`
--
ALTER TABLE `cl_child`
  ADD CONSTRAINT `fk_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `cl_parent` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;

--
-- Constraints for table `cl_child_location`
--
ALTER TABLE `cl_child_location`
  ADD CONSTRAINT `fk_child_id` FOREIGN KEY (`child_id`) REFERENCES `cl_child` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;


/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
