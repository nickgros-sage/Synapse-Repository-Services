CREATE TABLE IF NOT EXISTS `DOWNLOAD_ORDER` (
  `ORDER_ID` bigint(20) NOT NULL,
  `CREATED_BY` bigint(20) NOT NULL,
  `CREATED_ON` bigint(20) NOT NULL,
  `FILE_NAME` varchar(256) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `TOTAL_SIZE_MB` bigint(20) NOT NULL,
  `TOTAL_NUM_FILES` bigint(20) NOT NULL,
  `FILES_BLOB` blob,
  PRIMARY KEY (`ORDER_ID`),
  INDEX (`CREATED_BY`),
  CONSTRAINT `CREATED_BY_FK` FOREIGN KEY (`CREATED_BY`) REFERENCES `JDOUSERGROUP` (`ID`) ON DELETE CASCADE
)