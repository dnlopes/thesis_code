SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;

drop database if exists tpcc;
create database tpcc;
use tpcc;

drop table if exists warehouse;
drop table if exists district;
drop table if exists customer;
drop table if exists history;
drop table if exists new_order;
drop table if exists orders;
drop table if exists order_line;
drop table if exists item;
drop table if exists stock;

commit;

--
-- TOC entry 16 (OID 17148)
-- Name: customer; Type: TABLE; Schema: public; Owner: tpcc
--

CREATE TABLE customer (    
    c_id integer NOT NULL,
    c_d_id integer NOT NULL,
    c_w_id integer NOT NULL,
    c_first varchar(16),
    c_middle varchar(2),
    c_last varchar(16),
    c_street_1 varchar(20),
    c_street_2 varchar(20),
    c_city varchar(20),
    c_state varchar(2),
    c_zip varchar(9),
    c_phone varchar(16),
    c_since date,
    c_credit varchar(2),
    c_credit_lim numeric(12,2),
    c_discount numeric(4,4),
    c_balance numeric(12,2),
    c_ytd_payment numeric(12,2),
    c_payment_cnt integer,
    c_delivery_cnt integer,
    c_data varchar(500),
    _del boolean default 0,
    _cclock varchar(20),
    _dclock varchar(20)
) ENGINE=InnoDB;


--
-- TOC entry 17 (OID 17158)
-- Name: district; Type: TABLE; Schema: public; Owner: tpcc
--

CREATE TABLE district (    
    d_id integer NOT NULL,
    d_w_id integer NOT NULL,
    d_name varchar(10),
    d_street_1 varchar(20),
    d_street_2 varchar(20),
    d_city varchar(20),
    d_state varchar(2),
    d_zip varchar(9),
    d_tax numeric(4,4),
    d_ytd numeric(12,2),
    d_next_o_id integer,
    _del boolean default 0,
    _cclock varchar(20),
    _dclock varchar(20)
) ENGINE=InnoDB;;

--
-- TOC entry 18 (OID 17165)
-- Name: history; Type: TABLE; Schema: public; Owner: tpcc
--

CREATE TABLE history (
    h_c_id integer,
    h_c_d_id integer,
    h_c_w_id integer,
    h_d_id integer,
    h_w_id integer,
    h_date date,
    h_amount numeric(6,2),
    h_data varchar(24),
    _del boolean default 0,
    _cclock varchar(20),
    _dclock varchar(20)
) ENGINE=InnoDB;;


--
-- TOC entry 19 (OID 17170)
-- Name: item; Type: TABLE; Schema: public; Owner: tpcc
--

CREATE TABLE item (    
    i_id integer NOT NULL,
    i_im_id integer,
    i_name varchar(24),
    i_price numeric(5,2),
    i_data varchar(50),
    _del boolean default 0,
    _cclock varchar(20),
    _dclock varchar(20)
) ENGINE=InnoDB;;


--
-- TOC entry 20 (OID 17177)
-- Name: new_order; Type: TABLE; Schema: public; Owner: tpcc
--

CREATE TABLE new_order (    
    no_o_id integer,
    no_d_id integer,
    no_w_id integer,
    _del bit default 0,
    _cclock varchar(20),
    _dclock varchar(20)
) ENGINE=InnoDB;;

--
-- TOC entry 21 (OID 17182)
-- Name: order_line; Type: TABLE; Schema: public; Owner: tpcc
--

CREATE TABLE order_line (    
    ol_o_id integer NOT NULL,
    ol_d_id integer NOT NULL,
    ol_w_id integer NOT NULL,
    ol_number integer NOT NULL,
    ol_i_id integer,
    ol_supply_w_id integer,
    ol_delivery_d date,
    ol_quantity integer,
    ol_amount numeric(6,2),
    ol_dist_info varchar(24),
    _del boolean default 0,
    _cclock varchar(20),
    _dclock varchar(20)
) ENGINE=InnoDB;;


--
-- TOC entry 22 (OID 17187)
-- Name: orders; Type: TABLE; Schema: public; Owner: tpcc
--

CREATE TABLE orders (    
    o_id integer,
    o_d_id integer,
    o_w_id integer,
    o_c_id integer,
    o_entry_d date,
    o_carrier_id integer,
    o_ol_cnt integer,
    o_all_local integer,
    _del boolean default 0,
    _cclock varchar(20),
    _dclock varchar(20)
) ENGINE=InnoDB;;

--
-- TOC entry 23 (OID 17192)
-- Name: stock; Type: TABLE; Schema: public; Owner: tpcc
--

CREATE TABLE stock (    
    s_i_id integer NOT NULL,
    s_w_id integer NOT NULL,
    s_quantity integer,
    s_dist_01 varchar(24),
    s_dist_02 varchar(24),
    s_dist_03 varchar(24),
    s_dist_04 varchar(24),
    s_dist_05 varchar(24),
    s_dist_06 varchar(24),
    s_dist_07 varchar(24),
    s_dist_08 varchar(24),
    s_dist_09 varchar(24),
    s_dist_10 varchar(24),
    s_ytd integer,
    s_order_cnt integer,
    s_remote_cnt integer,
    s_data varchar(50),
    _del boolean default 0,
    _cclock varchar(20),
    _dclock varchar(20)
) ENGINE=InnoDB;;


--
-- TOC entry 24 (OID 17199)
-- Name: warehouse; Type: TABLE; Schema: public; Owner: tpcc
--

CREATE TABLE warehouse (    
    w_id integer NOT NULL,
    w_name varchar(10),
    w_street_1 varchar(20),
    w_street_2 varchar(20),
    w_city varchar(20),
    w_state varchar(2),
    w_zip varchar(9),
    w_tax numeric(4,4),
    w_ytd numeric(12,2),
    _del boolean default 0,
    _cclock varchar(20),
    _dclock varchar(20)
) ENGINE=InnoDB;;


commit;

--
-- TOC entry 27 (OID 1115795)
-- Name: pk_customer; Type: CONSTRAINT; Schema: public; Owner: tpcc
--

ALTER TABLE customer
    ADD CONSTRAINT pk_customer PRIMARY KEY (c_w_id, c_d_id, c_id);


--
-- TOC entry 29 (OID 1115798)
-- Name: pk_district; Type: CONSTRAINT; Schema: public; Owner: tpcc
--

ALTER TABLE district
    ADD CONSTRAINT pk_district PRIMARY KEY (d_w_id, d_id);


--
-- TOC entry 31 (OID 1115800)
-- Name: pk_item; Type: CONSTRAINT; Schema: public; Owner: tpcc
--

ALTER TABLE item
    ADD CONSTRAINT pk_item PRIMARY KEY (i_id);


--
-- TOC entry 34 (OID 1115802)
-- Name: pk_order_line; Type: CONSTRAINT; Schema: public; Owner: tpcc
--

ALTER TABLE order_line
    ADD CONSTRAINT pk_order_line PRIMARY KEY (ol_w_id, ol_d_id, ol_o_id, ol_number);


--
-- TOC entry 39 (OID 1115808)
-- Name: pk_stock; Type: CONSTRAINT; Schema: public; Owner: tpcc
--

ALTER TABLE stock
    ADD CONSTRAINT pk_stock PRIMARY KEY (s_w_id, s_i_id);


--
-- TOC entry 41 (OID 1115811)
-- Name: pk_warehouse; Type: CONSTRAINT; Schema: public; Owner: tpcc
--

ALTER TABLE warehouse
    ADD CONSTRAINT pk_warehouse PRIMARY KEY (w_id);

commit;



SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;


--
-- TOC entry 25 (OID 1115797)
-- Name: ix_customer; Type: INDEX; Schema: public; Owner: tpcc
--
CREATE INDEX ix_customer ON customer (c_w_id, c_d_id, c_id);
--
-- TOC entry 33 (OID 1115804)
-- Name: ix_order_line; Type: INDEX; Schema: public; Owner: tpcc
--
CREATE INDEX ix_order_line ON order_line (ol_i_id);
--
-- TOC entry 36 (OID 1115805)
-- Name: pk_orders; Type: INDEX; Schema: public; Owner: tpcc
--
CREATE INDEX pk_orders ON orders (o_w_id, o_d_id, o_id);
--
-- TOC entry 35 (OID 1115806)
-- Name: ix_orders; Type: INDEX; Schema: public; Owner: tpcc
--
CREATE INDEX ix_orders ON orders (o_w_id, o_d_id, o_c_id);
--
-- TOC entry 32 (OID 1115807)
-- Name: ix_new_order; Type: INDEX; Schema: public; Owner: tpcc
--
CREATE INDEX ix_new_order ON new_order (no_w_id, no_d_id, no_o_id);
--
-- TOC entry 37 (OID 1115810)
-- Name: ix_stock; Type: INDEX; Schema: public; Owner: tpcc
--
-- CREATE INDEX ix_stock ON stock (s_i_id);

commit;

ALTER TABLE district ADD CONSTRAINT fkey_district_1 FOREIGN KEY(d_w_id) REFERENCES warehouse(w_id) ON DELETE CASCADE;
ALTER TABLE customer ADD CONSTRAINT fkey_customer_1 FOREIGN KEY(c_w_id,c_d_id) REFERENCES district(d_w_id,d_id) ON DELETE CASCADE;
ALTER TABLE history ADD CONSTRAINT fkey_history_2 FOREIGN KEY(h_w_id,h_d_id) REFERENCES district(d_w_id,d_id) ON DELETE CASCADE;
ALTER TABLE new_order ADD CONSTRAINT fkey_new_orders_1 FOREIGN KEY(no_w_id,no_d_id,no_o_id) REFERENCES orders(o_w_id,o_d_id,o_id) ON DELETE CASCADE;
ALTER TABLE orders ADD CONSTRAINT fkey_orders_1 FOREIGN KEY(o_w_id,o_d_id,o_c_id) REFERENCES customer(c_w_id,c_d_id,c_id) ON DELETE CASCADE;
ALTER TABLE order_line ADD CONSTRAINT fkey_order_line_1 FOREIGN KEY(ol_w_id,ol_d_id,ol_o_id) REFERENCES orders(o_w_id,o_d_id,o_id) ON DELETE CASCADE;
ALTER TABLE stock ADD CONSTRAINT fkey_stock_1 FOREIGN KEY(s_w_id) REFERENCES warehouse(w_id) ON DELETE CASCADE;
ALTER TABLE stock ADD CONSTRAINT fkey_stock_2 FOREIGN KEY(s_i_id) REFERENCES item(i_id) ON DELETE CASCADE;

-- ALTER TABLE order_line ADD CONSTRAINT fkey_order_line_2 FOREIGN KEY(ol_supply_w_id,ol_i_id) REFERENCES stock(s_w_id,s_i_id) ON DELETE CASCADE;
-- ALTER TABLE history ADD CONSTRAINT fkey_history_1 FOREIGN KEY(h_c_w_id,h_c_d_id,h_c_id) REFERENCES customer(c_w_id,c_d_id,c_id) ON DELETE CASCADE;


SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

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


commit;




