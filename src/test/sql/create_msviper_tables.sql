
DROP TABLE IF EXISTS `msviperresult`;
CREATE TABLE msviperresult (
  ID bigint(20) NOT NULL,  
  CONSISTENCYVERSION  bigint(20) NOT NULL,
  LABEL VARCHAR(64),
  MRS longblob,
  MRSRESULT longblob,
  SHADOW_PAIRS longblob,
  LEADINGEDGES longblob,
  REGULONS longblob,   
  MRS_SIGNATURES longblob,
  BARCODES longblob,
  RANKS longblob,
  MINVAL double,
  MAXVAL double,  
  PRIMARY KEY (ID) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;