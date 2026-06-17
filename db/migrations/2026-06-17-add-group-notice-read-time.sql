USE `linkx_im`;

ALTER TABLE `im_group_member`
    ADD COLUMN IF NOT EXISTS `notice_read_time` DATETIME NULL COMMENT 'group notice read time' AFTER `mute_time`;
