@ARSETTABLE @UPDATEWINS CREATE TABLE t1 (
@LWWINTEGER a int(10) unsigned NOT NULL,
@NUMDELTAINTEGER b int(10) unsigned,
@LWWINTEGER c int(10),
@NUMDELTAINTEGER d int(10) unsigned,
@LWWSTRING e varchar(50),
PRIMARY KEY (a),
UNIQUE(e)
) ENGINE=INNODB;

@ARSETTABLE @UPDATEWINS CREATE TABLE t2 (
@LWWINTEGER a int(10) unsigned NOT NULL,
@LWWINTEGER b int(10) unsigned,
@LWWINTEGER c int(10) unsigned,
@LWWINTEGER d int(10) unsigned,
@LWWSTRING e varchar(50),
PRIMARY KEY  (a),
@UPDATEWINS FOREIGN KEY (c) REFERENCES t1(a) ON DELETE RESTRICT
) ENGINE=INNODB;

@ARSETTABLE @DELETEWINS CREATE TABLE t3 (
@LWWINTEGER a int(10) unsigned NOT NULL,
@LWWINTEGER b int(10) unsigned,
@LWWINTEGER c int(10) unsigned,
@LWWINTEGER d int(10) unsigned,
@LWWSTRING e varchar(50),
PRIMARY KEY (a),
@DELETEWINS FOREIGN KEY (b) REFERENCES t1(a) ON DELETE CASCADE
) ENGINE=INNODB;