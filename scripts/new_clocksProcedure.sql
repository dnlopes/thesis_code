DROP PROCEDURE IF EXISTS testClock;
DELIMITER //
CREATE PROCEDURE testClock(IN currentClock CHAR(100), IN newClock CHAR(100), OUT clockResult BOOL)
BEGIN
DECLARE newIsGreater BOOL;
DECLARE dumbFlag BOOL;
SET @newIsGreater = FALSE;
SET @clockResult = FALSE;
SET @dumbFlag = FALSE;

loopTag: WHILE (TRUE) DO
    SET @currEntry = CONVERT ( LEFT(currentClock, 1), SIGNED);
    SET @newEntry = CONVERT ( LEFT(newClock, 1), SIGNED);

    IF(@newEntry > @currEntry AND @dumbFlag = FALSE) then        
        SET @newIsGreater = TRUE;
        LEAVE loopTag;            
    ELSEIF(@newEntry < @currEntry) then
        SET @dumbFlag = TRUE;    
    END IF;  
 
    IF(LENGTH(currentClock) = 1) then
        LEAVE loopTag;
    END IF;

	SET currentClock = SUBSTRING(currentClock, LOCATE('-', currentClock) + 1);
	SET newClock = SUBSTRING(newClock, LOCATE('-', newClock) + 1);
END WHILE;	
    IF(@newIsGreater) then        
        SET @clockResult = TRUE;
        SELECT 'New is greater!' as 'Message';	
    ELSE
        SET @clockResult = FALSE;
        SELECT 'Old is greater!' as 'Message';  
	END IF;
END //
DELIMITER ;

-- CALL testClock('2-1', '1-2', @out);
-- SELECT @out;
