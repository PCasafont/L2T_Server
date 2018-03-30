CREATE TABLE IF NOT EXISTS `log_damage` (
  attacker        VARCHAR(16)    DEFAULT NULL,
  target          VARCHAR(16)    DEFAULT NULL,
  attackerClass   SMALLINT(6)    DEFAULT NULL,
  targetClass     SMALLINT(6)    DEFAULT NULL,
  damageType      VARCHAR(10)    DEFAULT NULL,
  attack          FLOAT NOT NULL DEFAULT '1',
  defense         FLOAT NOT NULL DEFAULT '1',
  levelMod        FLOAT NOT NULL DEFAULT '1',
  power           FLOAT NOT NULL DEFAULT '0',
  powerBonus      FLOAT NOT NULL DEFAULT '1',
  critBonus       FLOAT NOT NULL DEFAULT '1',
  critStaticBonus FLOAT NOT NULL DEFAULT '0',
  positionBonus   FLOAT NOT NULL DEFAULT '0',
  ssBonus         FLOAT NOT NULL DEFAULT '1',
  finalBonus      FLOAT NOT NULL DEFAULT '1',
  damage          FLOAT          DEFAULT NULL,
  PRIMARY KEY (`attacker`, `target`, `attack`, `defense`, `damage`)
);
