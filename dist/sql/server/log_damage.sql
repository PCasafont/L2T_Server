CREATE TABLE IF NOT EXISTS `log_damage` (
  attacker varchar(16) DEFAULT NULL,
  target varchar(16) DEFAULT NULL,
  attackerClass smallint(6) DEFAULT NULL,
  targetClass smallint(6) DEFAULT NULL,
  damageType varchar(10) DEFAULT NULL,
  attack float NOT NULL DEFAULT '1',
  defense float NOT NULL DEFAULT '1',
  levelMod float NOT NULL DEFAULT '1',
  power float NOT NULL DEFAULT '0',
  powerBonus float NOT NULL DEFAULT '1',
  critBonus float NOT NULL DEFAULT '1',
  critStaticBonus float NOT NULL DEFAULT '0',
  positionBonus float NOT NULL DEFAULT '0',
  ssBonus float NOT NULL DEFAULT '1',
  finalBonus float NOT NULL DEFAULT '1',
  damage float DEFAULT NULL,
  PRIMARY KEY (`attacker`, 0)
);