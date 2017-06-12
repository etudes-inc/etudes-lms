
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
