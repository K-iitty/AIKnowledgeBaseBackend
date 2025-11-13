-- Enhance notes and mindmaps tables (idempotent MySQL script)

SET @db := DATABASE();

-- notes.tags
SET @exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='notes' AND COLUMN_NAME='tags');
SET @sql := IF(@exists=0, 'ALTER TABLE notes ADD COLUMN tags VARCHAR(255) DEFAULT NULL COMMENT ''标签''' , 'SELECT 1');
PREPARE s1 FROM @sql; EXECUTE s1; DEALLOCATE PREPARE s1;

-- notes.cover_key
SET @exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='notes' AND COLUMN_NAME='cover_key');
SET @sql := IF(@exists=0, 'ALTER TABLE notes ADD COLUMN cover_key VARCHAR(255) DEFAULT NULL COMMENT ''封面图片OSS Key''' , 'SELECT 1');
PREPARE s2 FROM @sql; EXECUTE s2; DEALLOCATE PREPARE s2;

-- notes.word_count
SET @exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='notes' AND COLUMN_NAME='word_count');
SET @sql := IF(@exists=0, 'ALTER TABLE notes ADD COLUMN word_count INT DEFAULT 0 COMMENT ''字数''' , 'SELECT 1');
PREPARE s3 FROM @sql; EXECUTE s3; DEALLOCATE PREPARE s3;

-- mindmaps.cover_key
SET @exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='mindmaps' AND COLUMN_NAME='cover_key');
SET @sql := IF(@exists=0, 'ALTER TABLE mindmaps ADD COLUMN cover_key VARCHAR(255) DEFAULT NULL COMMENT ''封面图片OSS Key''' , 'SELECT 1');
PREPARE s4 FROM @sql; EXECUTE s4; DEALLOCATE PREPARE s4;

-- mindmaps.node_count
SET @exists := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='mindmaps' AND COLUMN_NAME='node_count');
SET @sql := IF(@exists=0, 'ALTER TABLE mindmaps ADD COLUMN node_count INT DEFAULT 0 COMMENT ''节点数量''' , 'SELECT 1');
PREPARE s5 FROM @sql; EXECUTE s5; DEALLOCATE PREPARE s5;

-- index: notes(category_id)
SET @exists := (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='notes' AND INDEX_NAME='idx_notes_category');
SET @sql := IF(@exists=0, 'ALTER TABLE notes ADD INDEX idx_notes_category (category_id)' , 'SELECT 1');
PREPARE s6 FROM @sql; EXECUTE s6; DEALLOCATE PREPARE s6;

-- index: mindmaps(category_id)
SET @exists := (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='mindmaps' AND INDEX_NAME='idx_mindmaps_category');
SET @sql := IF(@exists=0, 'ALTER TABLE mindmaps ADD INDEX idx_mindmaps_category (category_id)' , 'SELECT 1');
PREPARE s7 FROM @sql; EXECUTE s7; DEALLOCATE PREPARE s7;
