CREATE TABLE IF NOT EXISTS `pets` (
  `item_obj_id` INT(11) NOT NULL DEFAULT 0,
  `name`        VARCHAR(16),
  `level`       DECIMAL(11),
  `curHp`       DECIMAL(18, 0),
  `curMp`       DECIMAL(18, 0),
  `exp`         DECIMAL(20, 0),
  `sp`          DECIMAL(11),
  `fed`         DECIMAL(11),
  PRIMARY KEY (`item_obj_id`)
);
