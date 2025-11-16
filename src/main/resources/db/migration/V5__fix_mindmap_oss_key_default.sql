-- Fix mindmap oss_key default value (idempotent MySQL script)

SET @db := DATABASE();

-- Modify mindmaps.oss_key to have a default value
SET @exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='mindmaps' AND COLUMN_NAME='oss_key' AND COLUMN_DEFAULT IS NULL);
SET @sql := IF(@exists>0, 'ALTER TABLE mindmaps MODIFY COLUMN oss_key VARCHAR(255) NOT NULL DEFAULT "" COMMENT ''OSS文件Key''' , 'SELECT 1');
PREPARE s1 FROM @sql; EXECUTE s1; DEALLOCATE PREPARE s1;