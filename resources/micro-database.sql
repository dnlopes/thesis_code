@ARSETTABLE @UPDATEWINS CREATE TABLE `t1` (
@LWWINTEGER `a` int(10) unsigned NOT NULL,
@NUMDELTAINTEGER `b` int(10) unsigned,
@LWWINTEGER `c` int(10) auto_increment,
@LWWINTEGER `d` int(10) unsigned,
@LWWSTRING `e` varchar(50),
PRIMARY KEY  (a,b)
) ENGINE=INNODB;

@ARSETTABLE @UPDATEWINS CREATE TABLE `t2` (
@LWWINTEGER `a` int(10) unsigned NOT NULL,
@LWWINTEGER `b` int(10) unsigned,
@LWWINTEGER `c` int(10) unsigned,
@LWWINTEGER `d` int(10) unsigned,
@LWWSTRING `e` varchar(50),
PRIMARY KEY  (`a`),
@UPDATEWINS FOREIGN KEY (b) REFERENCES t1(b)
) ENGINE=INNODB;

@ARSETTABLE @DELETEWINS CREATE TABLE `t3` (
@LWWINTEGER `a` int(10) unsigned NOT NULL,
@LWWINTEGER `b` int(10) unsigned,
@LWWINTEGER `c` int(10) unsigned,
@LWWINTEGER `d` int(10) unsigned,
@LWWSTRING `e` varchar(50),
PRIMARY KEY  (`a`),
UNIQUE(b,d),
@DELETEWINS FOREIGN KEY (b) REFERENCES t1(b)
) ENGINE=INNODB;