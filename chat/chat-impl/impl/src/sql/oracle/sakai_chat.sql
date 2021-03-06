-----------------------------------------------------------------------------
-- CHAT_CHANNEL
-----------------------------------------------------------------------------

CREATE TABLE CHAT_CHANNEL
(
    CHANNEL_ID VARCHAR2 (99) NOT NULL,
	NEXT_ID INT,
    XML LONG
);

CREATE UNIQUE INDEX CHAT_CHANNEL_INDEX ON CHAT_CHANNEL
(
	CHANNEL_ID
);

-----------------------------------------------------------------------------
-- CHAT_MESSAGE
-----------------------------------------------------------------------------

CREATE TABLE CHAT_MESSAGE (
       CHANNEL_ID           VARCHAR2(99 BYTE) NOT NULL,
       MESSAGE_ID           VARCHAR2(36 BYTE) NOT NULL,
       DRAFT                CHAR(1) NULL
                                   CHECK (DRAFT IN (1, 0)),
       PUBVIEW              CHAR(1) NULL
                                   CHECK (PUBVIEW IN (1, 0)),
       OWNER                VARCHAR2(99) NULL,
       MESSAGE_DATE         DATE NOT NULL,
       XML                  LONG NULL
);

ALTER TABLE CHAT_MESSAGE
       ADD  ( PRIMARY KEY (CHANNEL_ID, MESSAGE_ID) ) ;

CREATE INDEX IE_CHAT_MESSAGE_CHANNEL ON CHAT_MESSAGE
(
       CHANNEL_ID                     ASC
);

CREATE INDEX IE_CHAT_MESSAGE_ATTRIB ON CHAT_MESSAGE
(
       DRAFT                          ASC,
       PUBVIEW                        ASC,
       OWNER                          ASC
);

CREATE INDEX IE_CHAT_MESSAGE_DATE ON CHAT_MESSAGE
(
       MESSAGE_DATE                   ASC
);

CREATE INDEX IE_CHAT_MESSAGE_DATE_DESC ON CHAT_MESSAGE
(
       MESSAGE_DATE                   DESC
);

CREATE INDEX CHAT_MSG_CDD ON CHAT_MESSAGE
(
	CHANNEL_ID,
	MESSAGE_DATE,
	DRAFT
);
