CREATE TABLE IF NOT EXISTS `reminders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `player` varchar(255) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `start` bigint(20) DEFAULT NULL,
  `tag` varchar(255) DEFAULT NULL,
  `delay` int(11) DEFAULT NULL,
  `rate` int(11) DEFAULT NULL,
  `echo` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;