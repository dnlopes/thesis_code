DROP FUNCTION IF EXISTS testClock;
DELIMITER //
CREATE FUNCTION testClock(currentClock CHAR(100), newClock CHAR(100)) RETURNS int
BEGIN
DECLARE isConcurrent BOOL;
DECLARE isLesser BOOL;
DECLARE isGreater BOOL;
DECLARE returnValue INT;
SET @returnValue = 0;
SET @isConcurrent = FALSE;
SET @isLesser = FALSE;
SET @isGreater = FALSE;

loopTag: WHILE (TRUE) DO
    SET @currEntry = CONVERT ( LEFT(currentClock, 1), SIGNED);
    SET @newEntry = CONVERT ( LEFT(newClock, 1), SIGNED);

    IF(@currEntry > @newEntry) then
    	IF(@isLesser) then
    		SET @isConcurrent = TRUE;
    		SET @isLesser = FALSE;
    		SET @isGreater = FALSE;
    		LEAVE loopTag;
    	END IF;
    	SET @isGreater = TRUE;   
    END IF;
    IF(@currEntry < @newEntry) then
    	IF(@isGreater) then
    		SET @isConcurrent = TRUE;
    		SET @isLesser = FALSE;
    		SET @isGreater = FALSE;    		   
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
	IF(@isGreater) then		
		SELECT 1 INTO @returnValue;
/*		SELECT 'First clock is GREATER then second' as 'Message';*/
    ELSEIF(@isLesser) then      
        SELECT 2 INTO @returnValue;
/*      SELECT 'First clock is LESSER then second' as 'Message';*/
	ELSEIF(@isConcurrent) then
		SELECT 3 INTO @returnValue;
/*		SELECT 'Clocks are concurrent' as 'Message';*/		
	ELSE
		SELECT 4 INTO @returnValue;
/*		SELECT 'Clocks are equal' as 'Message';*/
	END IF;
	RETURN @returnValue;	
END //
DELIMITER ;

-- select testFunc('2-0-1', '1-0-0');
-- select * from t1 where (select testFunc('2-0-1', '1-0-0') = 1) limit 5;