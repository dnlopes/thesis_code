drop database if exists micro;
create database micro;
use micro;

commit;

--
-- TOC entry 16 (OID 17148)
-- Name: customer; Type: TABLE; Schema: public; Owner: tpcc
--


CREATE TABLE t1 (    
    a int not null, 
    b int not null,
    c int not null,
    d int not null,
    e varchar(50),
    _del boolean default 0,
    _cclock varchar(50),
    _dclock varchar(50),
    PRIMARY KEY (a),
    UNIQUE(b)
) ENGINE=InnoDB;

CREATE TABLE t2 (
    a int not null,
    b int not null,
    c int not null,
    d int not null,
    e varchar(50),
    _del boolean default 0,
    _cclock varchar(50),
    _dclock varchar(50),
    PRIMARY KEY (a),
    UNIQUE(b)
) ENGINE=InnoDB;

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


