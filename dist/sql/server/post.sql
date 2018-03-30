CREATE TABLE IF NOT EXISTS `post` (
  `postId`       INT    NOT NULL DEFAULT '0',
  `senderId`     INT    NOT NULL DEFAULT '0',
  `receiverId`   INT    NOT NULL DEFAULT '0',
  `senderName`   VARCHAR(35),
  `receiverName` VARCHAR(35),
  `subject`      TINYTEXT,
  `content`      TEXT,
  `locked`       INT(1) NOT NULL DEFAULT '0',
  `expiration`   INT    NOT NULL DEFAULT '0',
  `unread`       INT(1) NOT NULL DEFAULT '0',
  `fourStars`    INT(1) NOT NULL DEFAULT '0',
  `news`         INT(1) NOT NULL DEFAULT '0',
  `reqAdena`     BIGINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`postId`)
);
