CREATE DATABASE IF NOT EXISTS micro;
USE micro;

DROP TABLE IF EXISTS `t1`;
@ARSETTABLE CREATE TABLE `t1` (
@LWWINTEGER `a` int(10) unsigned NOT NULL,
@LWWINTEGER `b` int(10) unsigned,
@LWWINTEGER `c` int(10) unsigned,
@LWWINTEGER `d` int(10) unsigned,
@LWWSTRING `e` varchar(50),
@LWWSTRING `lc` varchar(50) default '1#2#3#4',
`del` bool default false,
`ts` int default 0,
PRIMARY KEY  (`a`)
) ENGINE=INNODB;

DROP TABLE IF EXISTS `t2`;
CREATE TABLE `t2` (
`a` int(10) unsigned NOT NULL,
`b` int(10) unsigned,
`c` int(10) unsigned,
`d` int(10) unsigned,
`e` varchar(50),
`lc` varchar(50) default '1#2#3#4',
`del` bool default false,
`ts` int default 0,
PRIMARY KEY  (a,b)
) ENGINE=INNODB;

DROP TABLE IF EXISTS `t3`;
CREATE TABLE `t3` (
`a` int(10) unsigned NOT NULL,
`b` int(10) unsigned,
`c` int(10) unsigned,
`d` int(10) unsigned,
`e` varchar(50),
`del` bool default false,
`ts` int default 0,
PRIMARY KEY  (`a`)
) ENGINE=INNODB;

DROP TABLE IF EXISTS `t4`;
CREATE TABLE `t4` (
`a` int(10) unsigned NOT NULL,
`b` int(10) unsigned,
`c` int(10) unsigned,
`d` int(10) unsigned,
`e` varchar(50),
`del` bool default false,
`ts` int default 0,
PRIMARY KEY  (`a`)
) ENGINE=INNODB;

