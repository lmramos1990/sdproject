--------------------------------------------------------
--  File created - Sexta-feira-Dezembro-16-2016
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Sequence ARTICLE_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "BD"."ARTICLE_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 NOCACHE  ORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence AUCTION_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "BD"."AUCTION_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 NOCACHE  ORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence BID_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "BD"."BID_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 NOCACHE  ORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence CLIENTS_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "BD"."CLIENTS_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 NOCACHE  ORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence HISTORY_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "BD"."HISTORY_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 NOCACHE  ORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence MESSAGE_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "BD"."MESSAGE_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 NOCACHE  ORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Sequence NOTIFICATION_SEQ
--------------------------------------------------------

   CREATE SEQUENCE  "BD"."NOTIFICATION_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 NOCACHE  ORDER  NOCYCLE ;
--------------------------------------------------------
--  DDL for Table ARTICLE
--------------------------------------------------------

  CREATE TABLE "BD"."ARTICLE"
   (	"ARTICLE_ID" NUMBER(*,0),
	"CODE" CLOB
   ) SEGMENT CREATION IMMEDIATE
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM"
 LOB ("CODE") STORE AS BASICFILE (
  TABLESPACE "SYSTEM" ENABLE STORAGE IN ROW CHUNK 8192 RETENTION
  NOCACHE LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)) ;
--------------------------------------------------------
--  DDL for Table AUCTION
--------------------------------------------------------

  CREATE TABLE "BD"."AUCTION"
   (	"AUCTION_ID" NUMBER(*,0),
	"CLIENT_ID" NUMBER(*,0),
	"DESCRIPTION" CLOB,
	"DEADLINE" TIMESTAMP (6),
	"INITIAL_VALUE" FLOAT(126),
	"TITLE" CLOB,
	"ARTICLE_ID" NUMBER,
	"CURRENT_VALUE" FLOAT(126)
   ) SEGMENT CREATION IMMEDIATE
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM"
 LOB ("DESCRIPTION") STORE AS BASICFILE (
  TABLESPACE "SYSTEM" ENABLE STORAGE IN ROW CHUNK 8192 RETENTION
  NOCACHE LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT))
 LOB ("TITLE") STORE AS BASICFILE (
  TABLESPACE "SYSTEM" ENABLE STORAGE IN ROW CHUNK 8192 RETENTION
  NOCACHE LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)) ;
--------------------------------------------------------
--  DDL for Table BID
--------------------------------------------------------

  CREATE TABLE "BD"."BID"
   (	"BID_ID" NUMBER(*,0),
	"CLIENT_ID" NUMBER(*,0),
	"AUCTION_ID" NUMBER(*,0),
	"VALUE" FLOAT(126)
   ) SEGMENT CREATION IMMEDIATE
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" ;
--------------------------------------------------------
--  DDL for Table CLIENT
--------------------------------------------------------

  CREATE TABLE "BD"."CLIENT"
   (	"CLIENT_ID" NUMBER(*,0),
	"USERNAME" CLOB,
	"HPASSWORD" CLOB,
	"ESALT" CLOB
   ) SEGMENT CREATION IMMEDIATE
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM"
 LOB ("USERNAME") STORE AS BASICFILE (
  TABLESPACE "SYSTEM" ENABLE STORAGE IN ROW CHUNK 8192 RETENTION
  NOCACHE LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT))
 LOB ("HPASSWORD") STORE AS BASICFILE (
  TABLESPACE "SYSTEM" ENABLE STORAGE IN ROW CHUNK 8192 RETENTION
  NOCACHE LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT))
 LOB ("ESALT") STORE AS BASICFILE (
  TABLESPACE "SYSTEM" ENABLE STORAGE IN ROW CHUNK 8192 RETENTION
  NOCACHE LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)) ;
--------------------------------------------------------
--  DDL for Table HISTORY
--------------------------------------------------------

  CREATE TABLE "BD"."HISTORY"
   (	"HISTORY_ID" NUMBER,
	"AUCTION_ID" NUMBER,
	"ARTICLE_ID" NUMBER,
	"TITLE" VARCHAR2(3999 BYTE),
	"DESCRIPTION" VARCHAR2(3999 BYTE),
	"INITIAL_VALUE" FLOAT(126),
	"DEADLINE" TIMESTAMP (6)
   ) SEGMENT CREATION IMMEDIATE
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" ;
--------------------------------------------------------
--  DDL for Table MESSAGE
--------------------------------------------------------

  CREATE TABLE "BD"."MESSAGE"
   (	"MESSAGE_ID" NUMBER(*,0),
	"CLIENT_ID" NUMBER(*,0),
	"AUCTION_ID" NUMBER(*,0),
	"TEXT" CLOB
   ) SEGMENT CREATION IMMEDIATE
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM"
 LOB ("TEXT") STORE AS BASICFILE (
  TABLESPACE "SYSTEM" ENABLE STORAGE IN ROW CHUNK 8192 RETENTION
  NOCACHE LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)) ;
--------------------------------------------------------
--  DDL for Table NOTIFICATION
--------------------------------------------------------

  CREATE TABLE "BD"."NOTIFICATION"
   (	"NOTIFICATION_ID" NUMBER(*,0),
	"CLIENT_ID" NUMBER(*,0),
	"MESSAGE" CLOB,
	"READ" NUMBER,
	"WHOISFROM" CLOB
   ) SEGMENT CREATION IMMEDIATE
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM"
 LOB ("MESSAGE") STORE AS BASICFILE (
  TABLESPACE "SYSTEM" ENABLE STORAGE IN ROW CHUNK 8192 RETENTION
  NOCACHE LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT))
 LOB ("WHOISFROM") STORE AS BASICFILE (
  TABLESPACE "SYSTEM" ENABLE STORAGE IN ROW CHUNK 8192 RETENTION
  NOCACHE LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)) ;
REM INSERTING into BD.ARTICLE
SET DEFINE OFF;
REM INSERTING into BD.AUCTION
SET DEFINE OFF;
REM INSERTING into BD.BID
SET DEFINE OFF;
REM INSERTING into BD.CLIENT
SET DEFINE OFF;
REM INSERTING into BD.HISTORY
SET DEFINE OFF;
REM INSERTING into BD.MESSAGE
SET DEFINE OFF;
REM INSERTING into BD.NOTIFICATION
SET DEFINE OFF;
--------------------------------------------------------
--  DDL for Index PK_MESSAGE
--------------------------------------------------------

  CREATE UNIQUE INDEX "BD"."PK_MESSAGE" ON "BD"."MESSAGE" ("MESSAGE_ID")
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" ;
--------------------------------------------------------
--  DDL for Index HAS_FK
--------------------------------------------------------

  CREATE INDEX "BD"."HAS_FK" ON "BD"."MESSAGE" ("AUCTION_ID")
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" ;
--------------------------------------------------------
--  DDL for Index DOES_FK
--------------------------------------------------------

  CREATE INDEX "BD"."DOES_FK" ON "BD"."BID" ("CLIENT_ID")
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" ;
--------------------------------------------------------
--  DDL for Index PK_NOTIFICATION
--------------------------------------------------------

  CREATE UNIQUE INDEX "BD"."PK_NOTIFICATION" ON "BD"."NOTIFICATION" ("NOTIFICATION_ID")
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" ;
--------------------------------------------------------
--  DDL for Index HISTORY_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "BD"."HISTORY_PK" ON "BD"."HISTORY" ("HISTORY_ID")
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" ;
--------------------------------------------------------
--  DDL for Index RECEIVES_FK
--------------------------------------------------------

  CREATE INDEX "BD"."RECEIVES_FK" ON "BD"."NOTIFICATION" ("CLIENT_ID")
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" ;
--------------------------------------------------------
--  DDL for Index PK_ARTICLE
--------------------------------------------------------

  CREATE UNIQUE INDEX "BD"."PK_ARTICLE" ON "BD"."ARTICLE" ("ARTICLE_ID")
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" ;
--------------------------------------------------------
--  DDL for Index PK_USER
--------------------------------------------------------

  CREATE UNIQUE INDEX "BD"."PK_USER" ON "BD"."CLIENT" ("CLIENT_ID")
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" ;
--------------------------------------------------------
--  DDL for Index HAS_1_FK
--------------------------------------------------------

  CREATE INDEX "BD"."HAS_1_FK" ON "BD"."BID" ("AUCTION_ID")
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" ;
--------------------------------------------------------
--  DDL for Index PK_BID
--------------------------------------------------------

  CREATE UNIQUE INDEX "BD"."PK_BID" ON "BD"."BID" ("BID_ID")
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" ;
--------------------------------------------------------
--  DDL for Index CREATES_FK
--------------------------------------------------------

  CREATE INDEX "BD"."CREATES_FK" ON "BD"."AUCTION" ("CLIENT_ID")
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" ;
--------------------------------------------------------
--  DDL for Index PK_AUCTION
--------------------------------------------------------

  CREATE UNIQUE INDEX "BD"."PK_AUCTION" ON "BD"."AUCTION" ("AUCTION_ID")
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" ;
--------------------------------------------------------
--  DDL for Index CREATE_FK
--------------------------------------------------------

  CREATE INDEX "BD"."CREATE_FK" ON "BD"."MESSAGE" ("CLIENT_ID")
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM" ;
--------------------------------------------------------
--  Constraints for Table AUCTION
--------------------------------------------------------

  ALTER TABLE "BD"."AUCTION" MODIFY ("AUCTION_ID" NOT NULL ENABLE);
  ALTER TABLE "BD"."AUCTION" ADD CONSTRAINT "PK_AUCTION" PRIMARY KEY ("AUCTION_ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM"  ENABLE;
  ALTER TABLE "BD"."AUCTION" MODIFY ("CLIENT_ID" NOT NULL ENABLE);
  ALTER TABLE "BD"."AUCTION" MODIFY ("DEADLINE" NOT NULL ENABLE);
  ALTER TABLE "BD"."AUCTION" MODIFY ("INITIAL_VALUE" NOT NULL ENABLE);
  ALTER TABLE "BD"."AUCTION" MODIFY ("ARTICLE_ID" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table ARTICLE
--------------------------------------------------------

  ALTER TABLE "BD"."ARTICLE" MODIFY ("ARTICLE_ID" NOT NULL ENABLE);
  ALTER TABLE "BD"."ARTICLE" ADD CONSTRAINT "PK_ARTICLE" PRIMARY KEY ("ARTICLE_ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM"  ENABLE;
--------------------------------------------------------
--  Constraints for Table CLIENT
--------------------------------------------------------

  ALTER TABLE "BD"."CLIENT" MODIFY ("CLIENT_ID" NOT NULL ENABLE);
  ALTER TABLE "BD"."CLIENT" ADD CONSTRAINT "PK_USER" PRIMARY KEY ("CLIENT_ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM"  ENABLE;
--------------------------------------------------------
--  Constraints for Table BID
--------------------------------------------------------

  ALTER TABLE "BD"."BID" MODIFY ("BID_ID" NOT NULL ENABLE);
  ALTER TABLE "BD"."BID" ADD CONSTRAINT "PK_BID" PRIMARY KEY ("BID_ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM"  ENABLE;
  ALTER TABLE "BD"."BID" MODIFY ("CLIENT_ID" NOT NULL ENABLE);
  ALTER TABLE "BD"."BID" MODIFY ("AUCTION_ID" NOT NULL ENABLE);
  ALTER TABLE "BD"."BID" MODIFY ("VALUE" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table MESSAGE
--------------------------------------------------------

  ALTER TABLE "BD"."MESSAGE" MODIFY ("MESSAGE_ID" NOT NULL ENABLE);
  ALTER TABLE "BD"."MESSAGE" MODIFY ("CLIENT_ID" NOT NULL ENABLE);
  ALTER TABLE "BD"."MESSAGE" ADD CONSTRAINT "PK_MESSAGE" PRIMARY KEY ("MESSAGE_ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM"  ENABLE;
  ALTER TABLE "BD"."MESSAGE" MODIFY ("AUCTION_ID" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table HISTORY
--------------------------------------------------------

  ALTER TABLE "BD"."HISTORY" MODIFY ("HISTORY_ID" NOT NULL ENABLE);
  ALTER TABLE "BD"."HISTORY" ADD CONSTRAINT "HISTORY_PK" PRIMARY KEY ("HISTORY_ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM"  ENABLE;
  ALTER TABLE "BD"."HISTORY" MODIFY ("AUCTION_ID" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table NOTIFICATION
--------------------------------------------------------

  ALTER TABLE "BD"."NOTIFICATION" MODIFY ("NOTIFICATION_ID" NOT NULL ENABLE);
  ALTER TABLE "BD"."NOTIFICATION" ADD CONSTRAINT "PK_NOTIFICATION" PRIMARY KEY ("NOTIFICATION_ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "SYSTEM"  ENABLE;
  ALTER TABLE "BD"."NOTIFICATION" MODIFY ("CLIENT_ID" NOT NULL ENABLE);
  ALTER TABLE "BD"."NOTIFICATION" MODIFY ("READ" NOT NULL ENABLE);
--------------------------------------------------------
--  Ref Constraints for Table AUCTION
--------------------------------------------------------

  ALTER TABLE "BD"."AUCTION" ADD CONSTRAINT "FK_AUCTION_CREATES_USER" FOREIGN KEY ("CLIENT_ID")
	  REFERENCES "BD"."CLIENT" ("CLIENT_ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table BID
--------------------------------------------------------

  ALTER TABLE "BD"."BID" ADD CONSTRAINT "FK_BID_DOES_USER" FOREIGN KEY ("CLIENT_ID")
	  REFERENCES "BD"."CLIENT" ("CLIENT_ID") ENABLE;
  ALTER TABLE "BD"."BID" ADD CONSTRAINT "FK_BID_HAS_1_AUCTION" FOREIGN KEY ("AUCTION_ID")
	  REFERENCES "BD"."AUCTION" ("AUCTION_ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table HISTORY
--------------------------------------------------------

  ALTER TABLE "BD"."HISTORY" ADD CONSTRAINT "HISTORY_AUCTION_FK1" FOREIGN KEY ("AUCTION_ID")
	  REFERENCES "BD"."AUCTION" ("AUCTION_ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table MESSAGE
--------------------------------------------------------

  ALTER TABLE "BD"."MESSAGE" ADD CONSTRAINT "FK_MESSAGE_CREATE_USER" FOREIGN KEY ("CLIENT_ID")
	  REFERENCES "BD"."CLIENT" ("CLIENT_ID") ENABLE;
  ALTER TABLE "BD"."MESSAGE" ADD CONSTRAINT "FK_MESSAGE_HAS_AUCTION" FOREIGN KEY ("AUCTION_ID")
	  REFERENCES "BD"."AUCTION" ("AUCTION_ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table NOTIFICATION
--------------------------------------------------------

  ALTER TABLE "BD"."NOTIFICATION" ADD CONSTRAINT "FK_NOTIFICA_RECEIVES_USER" FOREIGN KEY ("CLIENT_ID")
	  REFERENCES "BD"."CLIENT" ("CLIENT_ID") ENABLE;
--------------------------------------------------------
--  DDL for Function CREATEARTICLE
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "BD"."CREATEARTICLE" (pcode in varchar2) RETURN NUMBER AS
articleSeq number;
BEGIN
  LOCK TABLE article IN ROW EXCLUSIVE MODE;

  articleSeq := article_seq.nextVal;

  INSERT INTO article (article_id, code) VALUES (articleSeq, pcode);

  COMMIT;
  return articleSeq;

  EXCEPTION
  WHEN others THEN
    ROLLBACK;
    DBMS_OUTPUT.put_line(SQLCODE);
    return -1;

END CREATEARTICLE;

/
--------------------------------------------------------
--  DDL for Function GETARTICLEID
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "BD"."GETARTICLEID" (pcode in varchar2)
return number
as
  myID number;
begin

  SELECT article_id INTO myID FROM article WHERE to_char(code) = pcode;

  return myID;

  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      return -1;

end;

/
--------------------------------------------------------
--  DDL for Function GETAUCTIONID
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "BD"."GETAUCTIONID" (pid in varchar2)
return number
as
  myID number;
begin

  SELECT auction_id INTO myID FROM auction WHERE auction_id = pid;

  return myID;

  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      return -1;

end;

/
--------------------------------------------------------
--  DDL for Function GETCLIENTID
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "BD"."GETCLIENTID" (pusername in varchar2)
return number
as
  myID number;
begin

  SELECT client_id INTO myID FROM client WHERE to_char(username) = pusername;

  return myID;

  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      return -1;

end;

/
--------------------------------------------------------
--  DDL for Function GETSALT
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "BD"."GETSALT" (pusername in varchar2)
return clob
as
  salt clob;
begin

  SELECT esalt INTO salt FROM client WHERE to_char(username) = pusername;

  return salt;

  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      return '';

end GETSALT;

/
--------------------------------------------------------
--  DDL for Function HASENDED
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "BD"."HASENDED" (auctionID in number) RETURN number AS
theDate timestamp;
BEGIN

      SELECT deadline INTO theDate FROM auction WHERE auction_id = auctionID;

      if systimestamp > theDate then
        return -1;
      else
        return 1;
      end if;
END HASENDED;

/
--------------------------------------------------------
--  DDL for Function SQUIRREL_GET_ERROR_OFFSET
--------------------------------------------------------

  CREATE OR REPLACE FUNCTION "BD"."SQUIRREL_GET_ERROR_OFFSET" (query IN varchar2) return number authid current_user is      l_theCursor     integer default dbms_sql.open_cursor;      l_status        integer; begin          begin          dbms_sql.parse(  l_theCursor, query, dbms_sql.native );          exception                  when others then l_status := dbms_sql.last_error_position;          end;          dbms_sql.close_cursor( l_theCursor );          return l_status; end;

/
--------------------------------------------------------
--  DDL for Procedure CREATEAUCTION
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "BD"."CREATEAUCTION" (pusername in varchar2, pcode in varchar2,  ptitle in varchar2, pdescription in varchar2, pinitial_value in float, pdeadline timestamp, message out varchar2) AS
clientID number;
articleID number;
BEGIN
  LOCK TABLE auction in ROW EXCLUSIVE MODE;

  clientID := getclientID(pusername);

  if clientID = -1 then
    message := 'type: create_auction, ok: false';
  else

    articleID := getarticleid(pcode);

    if articleID = -1 then
      articleID := createarticle(pcode);
    end if;

    INSERT INTO auction (auction_id, client_id, article_id, title, description, initial_value, deadline) VALUES(auction_seq.nextVal, clientID, articleID, ptitle, pdescription, pinitial_value, pdeadline);

    COMMIT;
    message := 'type: create_auction, ok: true';
  end if;

  EXCEPTION
    WHEN others THEN
      ROLLBACK;
      DBMS_OUTPUT.put_line(SQLCODE);
      message := 'type: create_auction, ok: false';

END CREATEAUCTION;

/
--------------------------------------------------------
--  DDL for Procedure CREATEBID
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "BD"."CREATEBID" (pusername in varchar2, pid in number, amount in float, auctionID in number, clientID in number, message out varchar2) AS

isOver number;
myValue float;
myInitial float;

BEGIN
  LOCK TABLE auction in ROW EXCLUSIVE MODE;

  isOver := hasended(auctionID);

  if isOver = -1 then
      message := 'type: bid, ok: false';
  else
    SELECT current_value INTO myValue FROM auction WHERE auction_id = auctionID;
    SELECT initial_value INTO myInitial FROM auction WHERE auction_id = auctionID;

    if (myValue = 0 and myInitial > amount) or (myValue != 0 and myValue > amount) then
      INSERT INTO bid (bid_id, client_id, auction_id, value) VALUES (bid_seq.nextVal, clientID, auctionID, amount);
      COMMIT;
      message := 'type: bid, ok: true';
    else
      message := 'type: bid, ok: false';
    end if;

  end if;

  EXCEPTION
    WHEN others THEN
      ROLLBACK;
      DBMS_OUTPUT.put_line(SQLCODE);
      message := 'type: bid, ok: false';

END CREATEBID;

/
--------------------------------------------------------
--  DDL for Procedure CREATEMESSAGE
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "BD"."CREATEMESSAGE" (pusername in varchar2, pid in number,  ptext in varchar2, message out varchar2) AS
clientID number;
auctionID number;


BEGIN
  LOCK TABLE message IN ROW EXCLUSIVE MODE;

  clientID := getclientID(pusername);

  if clientID = -1 then
    message := 'type: message, ok: false';
  else

    auctionID := getauctionid(pid);

    if auctionID = -1 then
      message := 'type: message, ok: false';
    else
      INSERT INTO message (message_id, client_id, auction_id, text) VALUES(message_seq.nextVal, clientID, auctionID, ptext);
      message := 'type: message, ok: true';
    end if;

  end if;

  EXCEPTION
    WHEN others THEN
      ROLLBACK;
      DBMS_OUTPUT.put_line(SQLCODE);
      message := 'type: message, ok: false';

END CREATEMESSAGE;

/
--------------------------------------------------------
--  DDL for Procedure EDITAUCTION
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "BD"."EDITAUCTION" (myquery in varchar2)AS
BEGIN

EXECUTE IMMEDIATE myquery;

COMMIT;

END EDITAUCTION;

/
--------------------------------------------------------
--  DDL for Procedure SIGNIN
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "BD"."SIGNIN" (pusername in varchar2, phpassword in varchar2, message out VARCHAR2) AS
match_count number;
BEGIN
    select count(*)
    into match_count
    from client
    where to_char(username) = pusername
    and to_char(hpassword) = phpassword;
    if match_count = 1 then
      message := 'type: login, ok: true';
    else
      message := 'type: login, ok: false';
  end if;
END SIGNIN;

/
--------------------------------------------------------
--  DDL for Procedure SIGNUP
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "BD"."SIGNUP" (pusername in varchar2, phpassword in varchar2, salt in varchar2, message out VARCHAR2) AS
myID number;
BEGIN
  LOCK TABLE client IN SHARE MODE;

  myID := getclientid(pusername);

  if myID = -1 then
      message := 'type: register, ok: false';
  end if;

  INSERT INTO client (client_id, username, hpassword, esalt) VALUES(clients_seq.nextVal, pusername, phpassword, salt);

  COMMIT;
  message := 'type: register, ok: true';

  EXCEPTION
  WHEN others THEN
    ROLLBACK;
    DBMS_OUTPUT.put_line(SQLCODE);
    message := 'type: register, ok: false';

END SIGNUP;

/
--------------------------------------------------------
--  DDL for Procedure UPDATEBID
--------------------------------------------------------
set define off;

  CREATE OR REPLACE PROCEDURE "BD"."UPDATEBID" (pusername in varchar2, pid in number, amount in float, auctionID in number, clientID in number, message out varchar2) AS
BEGIN
  UPDATE auction SET current_value = amount WHERE auction_id = auctionID;
  COMMIT;
END UPDATEBID;

/
