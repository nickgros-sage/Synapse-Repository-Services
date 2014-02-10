CREATE TABLE `SESSION_TOKEN` (
  `PRINCIPAL_ID` bigint(20) NOT NULL,
  `VALIDATED_ON` datetime DEFAULT NULL,
  `DOMAIN` varchar(20) NOT NULL,
  `SESSION_TOKEN` varchar(100) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  PRIMARY KEY (`PRINCIPAL_ID`, `DOMAIN`), 
  UNIQUE KEY `UNIQUE_SESSION_TOKEN` (`SESSION_TOKEN`), 
  CONSTRAINT `SESSION_TOKEN_PRINCIPAL_ID_FK` FOREIGN KEY (`PRINCIPAL_ID`) REFERENCES `JDOUSERGROUP` (`ID`) ON DELETE CASCADE
)