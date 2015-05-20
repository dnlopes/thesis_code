DROP FUNCTION IF EXISTS compareClocks;
DELIMITER //
CREATE FUNCTION compareClocks(currentClock CHAR(100), newClock CHAR(100)) RETURNS INT
BEGIN
DECLARE newIsGreater BOOL;
DECLARE dumbFlag BOOL;
DECLARE returnValue INT;
SET @newIsGreater = FALSE;
SET @clockResult = FALSE;
SET @dumbFlag = FALSE;
SET @returnValue = FALSE;

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
        SELECT TRUE INTO @returnValue;      
    ELSE
        SELECT FALSE INTO @returnValue;      
	END IF;
    RETURN @returnValue;
END //
DELIMITER ;

-- select compareClocks('2-0-1', '1-0-0');