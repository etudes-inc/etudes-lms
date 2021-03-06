-----------------------------------------------------------------------------
-- CALENDAR_CALENDAR
-----------------------------------------------------------------------------

CREATE TABLE CALENDAR_CALENDAR
(
    CALENDAR_ID VARCHAR (99) NOT NULL,
	NEXT_ID INT,
    XML LONGVARCHAR,
    CONSTRAINT CALENDAR_CALENDAR_INDEX UNIQUE (CALENDAR_ID)
);

-----------------------------------------------------------------------------
-- CALENDAR_EVENT
-----------------------------------------------------------------------------

CREATE TABLE CALENDAR_EVENT
(
    CALENDAR_ID VARCHAR (99) NOT NULL,
	EVENT_ID VARCHAR (36) NOT NULL,
	EVENT_START DATETIME NOT NULL,
	EVENT_END DATETIME NOT NULL,
    XML LONGVARCHAR
);

CREATE INDEX CALENDAR_EVENT_INDEX ON CALENDAR_EVENT
(
	CALENDAR_ID
);
