drop database if exists tpcc;
create database tpcc;

use tpcc;

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;

drop table if exists warehouse;
drop table if exists district;
drop table if exists customer;
drop table if exists history;
drop table if exists new_orders;
drop table if exists orders;
drop table if exists order_line;
drop table if exists item;
drop table if exists stock;

create table warehouse (
w_id smallint not null,
w_name varchar(10), 
w_street_1 varchar(20), 
w_street_2 varchar(20), 
w_city varchar(20), 
w_state char(2), 
w_zip char(9), 
w_tax decimal(4,2), 
w_ytd decimal(12,2),
_del boolean default 0,
_cclock varchar(20),
_dclock varchar(20),
primary key (w_id) ) Engine=InnoDB;


create table district (
d_id tinyint not null, 
d_w_id smallint not null, 
d_name varchar(10), 
d_street_1 varchar(20), 
d_street_2 varchar(20), 
d_city varchar(20), 
d_state char(2), 
d_zip char(9), 
d_tax decimal(4,2), 
d_ytd decimal(12,2), 
d_next_o_id int,
_del boolean default 0,
_cclock varchar(20),
_dclock varchar(20),
primary key (d_w_id, d_id) ) Engine=InnoDB;


create table customer (
c_id int not null, 
c_d_id tinyint not null,
c_w_id smallint not null, 
c_first varchar(16), 
c_middle char(2), 
c_last varchar(16), 
c_street_1 varchar(20), 
c_street_2 varchar(20), 
c_city varchar(20), 
c_state char(2), 
c_zip char(9), 
c_phone char(16), 
c_since date, 
c_credit char(2), 
c_credit_lim bigint, 
c_discount decimal(4,2), 
c_balance decimal(12,2), 
c_ytd_payment decimal(12,2), 
c_payment_cnt smallint, 
c_delivery_cnt smallint, 
c_data varchar(500),
_del boolean default 0,
_cclock varchar(20),
_dclock varchar(20),
PRIMARY KEY(c_w_id, c_d_id, c_id) ) Engine=InnoDB;


create table history (
h_c_id int, 
h_c_d_id tinyint, 
h_c_w_id smallint,
h_d_id tinyint,
h_w_id smallint,
h_date date,
h_amount decimal(6,2), 
h_data varchar(24),
_del boolean default 0,
_cclock varchar(20),
_dclock varchar(20)
) Engine=InnoDB;


create table new_orders (
no_o_id int not null,
no_d_id tinyint not null,
no_w_id smallint not null,
_del bit default 0,
_cclock varchar(20),
_dclock varchar(20),
PRIMARY KEY(no_w_id, no_d_id, no_o_id)) Engine=InnoDB;


create table orders (
o_id int not null, 
o_d_id tinyint not null, 
o_w_id smallint not null,
o_c_id int,
o_entry_d date,
o_carrier_id tinyint,
o_ol_cnt tinyint, 
o_all_local tinyint,
_del boolean default 0,
_cclock varchar(20),
_dclock varchar(20),
PRIMARY KEY(o_w_id, o_d_id, o_id) ) Engine=InnoDB;


create table order_line ( 
ol_o_id int not null, 
ol_d_id tinyint not null,
ol_w_id smallint not null,
ol_number tinyint not null,
ol_i_id int, 
ol_supply_w_id smallint,
ol_delivery_d timestamp, 
ol_quantity tinyint, 
ol_amount decimal(6,2), 
ol_dist_info char(24),
_del boolean default 0,
_cclock varchar(20),
_dclock varchar(20),
PRIMARY KEY(ol_w_id, ol_d_id, ol_o_id, ol_number) ) Engine=InnoDB ;


create table item (
i_id int not null, 
i_im_id int, 
i_name varchar(24), 
i_price decimal(5,2), 
i_data varchar(50),
_del boolean default 0,
_cclock varchar(20),
_dclock varchar(20),
PRIMARY KEY(i_id) ) Engine=InnoDB;


create table stock (
s_i_id int not null, 
s_w_id smallint not null, 
s_quantity smallint, 
s_dist_01 char(24), 
s_dist_02 char(24),
s_dist_03 char(24),
s_dist_04 char(24), 
s_dist_05 char(24), 
s_dist_06 char(24), 
s_dist_07 char(24), 
s_dist_08 char(24), 
s_dist_09 char(24), 
s_dist_10 char(24), 
s_ytd decimal(8,0), 
s_order_cnt smallint, 
s_remote_cnt smallint,
s_data varchar(50),
_del boolean default 0,
_cclock varchar(20),
_dclock varchar(20),
PRIMARY KEY(s_w_id, s_i_id) ) Engine=InnoDB ;

SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;


CREATE INDEX idx_customer ON customer (c_w_id,c_d_id,c_last,c_first);
CREATE INDEX idx_orders ON orders (o_w_id,o_d_id,o_c_id,o_id);
CREATE INDEX fkey_stock_2 ON stock (s_i_id);
CREATE INDEX fkey_order_line_2 ON order_line (ol_supply_w_id,ol_i_id);

commit;


DROP FUNCTION IF EXISTS compareClocks;
DELIMITER //
CREATE FUNCTION compareClocks(currentClock CHAR(100), newClock CHAR(100)) RETURNS int
BEGIN
DECLARE isConcurrent BOOL;
DECLARE isLesser BOOL;
DECLARE dumbFlag BOOL;
DECLARE isGreater BOOL;
DECLARE cycleCond BOOL;
DECLARE returnValue INT;
SET @dumbFlag = FALSE;
SET @returnValue = 0;
SET @isConcurrent = FALSE;
SET @isLesser = FALSE;
SET @isGreater = FALSE;

IF(currentClock IS NULL) then
    RETURN 1;
END IF;

loopTag: WHILE (TRUE) DO
    SET @currEntry = CONVERT ( LEFT(currentClock, 1), SIGNED);
    SET @newEntry = CONVERT ( LEFT(newClock, 1), SIGNED);

    IF(@currEntry > @newEntry) then
            SET @dumbFlag = TRUE;    
    	IF(@isLesser) then
    		SET @isConcurrent = TRUE;
    		LEAVE loopTag;
    	END IF;
    	SET @isGreater = TRUE;

    ELSEIF(@currEntry < @newEntry) then
    	IF(@isGreater) then
    		SET @isConcurrent = TRUE;
            IF(@dumbFlag = FALSE) then
                SET @isGreater = TRUE;
            END IF;
    		LEAVE loopTag;
    	END IF;    
    	SET @isLesser = TRUE;  
    END IF;
 
	IF (LENGTH(currentClock) = 1) then
		LEAVE loopTag;
	END IF;

	SET currentClock = SUBSTRING(currentClock, LOCATE('-', currentClock) + 1);
	SET newClock = SUBSTRING(newClock, LOCATE('-', newClock) + 1);
END WHILE;
    IF(@isConcurrent AND @dumbFlag = FALSE) then
        SELECT 0 INTO @returnValue;
/*      SELECT 'Clocks are concurrent' as 'Message';*/  
	ELSEIF(@isLesser) then		
		SELECT 1 INTO @returnValue;
/*      SELECT 'Second clock is GREATER then second' as 'Message';*/
    ELSE
        SELECT -1 INTO @returnValue;
/*      SELECT 'Second clock is LESSER then second' as 'Message';*/
	END IF;
	RETURN @returnValue;	
END //
DELIMITER ;

