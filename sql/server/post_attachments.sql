CREATE TABLE IF NOT EXISTS `post_attachments` (
  `postId` INT NOT NULL DEFAULT '0',
  `objectId` INT NOT NULL DEFAULT '0',
  PRIMARY KEY (`postId`,`objectId`)
);
