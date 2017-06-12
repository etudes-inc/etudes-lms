-----------------------------------------------------------------------------
-- ANNOUNCEMENT_CHANNEL
-----------------------------------------------------------------------------

CREATE TABLE ANNOUNCEMENT_CHANNEL
(
    CHANNEL_ID VARCHAR (99) NOT NULL,
	NEXT_ID INT,
    XML LONGTEXT
);

CREATE UNIQUE INDEX ANNOUNCEMENT_CHANNEL_INDEX ON ANNOUNCEMENT_CHANNEL
(
	CHANNEL_ID
);

-----------------------------------------------------------------------------
-- ANNOUNCEMENT_MESSAGE
-----------------------------------------------------------------------------

CREATE TABLE ANNOUNCEMENT_MESSAGE (
       CHANNEL_ID           VARCHAR(99) NOT NULL,
       MESSAGE_ID           VARCHAR(36) NOT NULL,
       DRAFT                CHAR(1) NULL
                                   CHECK (DRAFT IN (1, 0)),
       PUBVIEW              CHAR(1) NULL
                                   CHECK (PUBVIEW IN (1, 0)),
       OWNER                VARCHAR (99) NULL,
       MESSAGE_DATE         DATETIME NOT NULL,
       XML                  LONGTEXT NULL
);


ALTER TABLE ANNOUNCEMENT_MESSAGE
       ADD  ( PRIMARY KEY (CHANNEL_ID, MESSAGE_ID) ) ;

CREATE INDEX IE_ANNC_MSG_CHANNEL ON ANNOUNCEMENT_MESSAGE
(
       CHANNEL_ID                     ASC
);

CREATE INDEX IE_ANNC_MSG_ATTRIB ON ANNOUNCEMENT_MESSAGE
(
       DRAFT                          ASC,
       PUBVIEW                        ASC,
       OWNER                          ASC
);

CREATE INDEX IE_ANNC_MSG_DATE ON ANNOUNCEMENT_MESSAGE
(
       MESSAGE_DATE                   ASC
);

CREATE INDEX IE_ANNC_MSG_DATE_DESC ON ANNOUNCEMENT_MESSAGE
(
       MESSAGE_DATE                   DESC
);

CREATE INDEX ANNOUNCEMENT_MESSAGE_CDD ON ANNOUNCEMENT_MESSAGE
(
	CHANNEL_ID,
	MESSAGE_DATE,
	DRAFT
);

-----------------------------------------------------------------------------
-- for ANNC-8 delayed posting of announcements
-----------------------------------------------------------------------------

CREATE TABLE et_sch_delayed_invocation
(
	INVOCATION_ID varchar(36) NOT NULL default '',
	INVOCATION_TIME datetime NOT NULL default '0000-00-00 00:00:00',
	COMPONENT VARCHAR (99) NOT NULL,
	CONTEXT VARCHAR (255),
	PRIMARY KEY (`INVOCATION_ID`)
);

CREATE INDEX et_sch_delayed_invocation_it ON et_sch_delayed_invocation
(
	INVOCATION_TIME asc
);
