ALTER TABLE blog
ADD COLUMN status varchar(20) NOT NULL DEFAULT 'FINISHED' COMMENT '文章编辑状态：DRAFT草稿，FINISHED已完成';

UPDATE blog
SET status = 'FINISHED'
WHERE status IS NULL OR status = '';
