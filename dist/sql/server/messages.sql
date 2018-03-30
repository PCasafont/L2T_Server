CREATE TABLE IF NOT EXISTS `messages` (
  `messageId`           INT                                       NOT NULL DEFAULT 0,
  `senderId`            INT                                       NOT NULL DEFAULT 0,
  `receiverId`          INT                                       NOT NULL DEFAULT 0,
  `subject`             TINYTEXT,
  `content`             TEXT,
  `expiration`          BIGINT(13) UNSIGNED                       NOT NULL DEFAULT '0',
  `reqAdena`            BIGINT                                    NOT NULL DEFAULT 0,
  `hasAttachments`      ENUM ('true', 'false') DEFAULT 'false'    NOT NULL,
  `isUnread`            ENUM ('true', 'false') DEFAULT 'true'     NOT NULL,
  `isDeletedBySender`   ENUM ('true', 'false') DEFAULT 'false'    NOT NULL,
  `isDeletedByReceiver` ENUM ('true', 'false') DEFAULT 'false'    NOT NULL,
  `isLocked`            ENUM ('true', 'false') DEFAULT 'false'    NOT NULL,
  `sendBySystem`        INT(11)                                   NOT NULL DEFAULT '0',
  `isReturned`          ENUM ('true', 'false') CHARACTER SET utf8 NOT NULL DEFAULT 'false',
  `systemMessage1`      INT(11)                                   NOT NULL DEFAULT '0',
  `systemMessage2`      INT(11)                                   NOT NULL DEFAULT '0',
  PRIMARY KEY (`messageId`)
);
