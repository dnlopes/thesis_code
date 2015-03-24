CREATE DATABASE IF NOT EXISTS micro;
USE micro;

DROP TABLE IF EXISTS `t1`;
@ARSETTABLE CREATE TABLE `t1` (
@LWWINTEGER `a` int(10) unsigned NOT NULL,
@NUMDELTAINTEGER `b` int(10) unsigned,
@LWWINTEGER `c` int(10) unsigned,
@LWWINTEGER `d` int(10) unsigned,
@LWWSTRING `e` varchar(50),
PRIMARY KEY  (`a`),
UNIQUE(c),
CHECK(b>2)
) ENGINE=INNODB;

DROP TABLE IF EXISTS `t2`;
@ARSETTABLE CREATE TABLE `t2` (
@LWWINTEGER `a` int(10) unsigned NOT NULL,
@LWWINTEGER `b` int(10) unsigned,
@LWWINTEGER `c` int(10) unsigned,
@LWWINTEGER `d` int(10) unsigned,
@LWWSTRING `e` varchar(50),
PRIMARY KEY  (`a`)
) ENGINE=INNODB;

DROP TABLE IF EXISTS `t3`;
@ARSETTABLE CREATE TABLE `t3` (
@LWWINTEGER `a` int(10) unsigned NOT NULL,
@LWWINTEGER `b` int(10) unsigned,
@LWWINTEGER `c` int(10) unsigned,
@LWWINTEGER `d` int(10) unsigned,
@LWWSTRING `e` varchar(50),
PRIMARY KEY  (`a`)
) ENGINE=INNODB;

DROP TABLE IF EXISTS `t4`;
@ARSETTABLE CREATE TABLE `t4` (
@LWWINTEGER `a` int(10) unsigned NOT NULL,
@LWWINTEGER `b` int(10) unsigned,
@LWWINTEGER `c` int(10) unsigned,
@LWWINTEGER `d` int(10) unsigned,
@LWWSTRING `e` varchar(50),
PRIMARY KEY  (`a`)
) ENGINE=INNODB;

