DROP PROCEDURE IF EXISTS testClock;
DELIMITER //
CREATE PROCEDURE testClock(IN currentClock CHAR(100), IN newClock CHAR(100), OUT result INT)
BEGIN
DECLARE isConcurrent BOOL;
DECLARE isLesser BOOL;
DECLARE isGreater BOOL;
SET @result = 0;
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
		SET result = 1;
		SELECT 'First clock is GREATER then second' as 'Message';
    ELSEIF(@isLesser) then      
        SET result = 2;
        SELECT 'First clock is LESSER then second' as 'Message';        
	ELSEIF(@isConcurrent) then
		SET result = 3;
		SELECT 'Clocks are concurrent' as 'Message';		
	ELSE
		SET result = 4;
		SELECT 'Clocks are equal' as 'Message';
	END IF;
END //
DELIMITER ;

-- CALL testProc('1-2', '1-1', @out);
-- SELECT @out;
