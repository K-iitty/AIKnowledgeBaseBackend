-- Enhanced knowledge base categorization system
-- Add cover image and description fields to categories

-- Add cover_key and description fields to note_categories
ALTER TABLE note_categories 
ADD COLUMN cover_key VARCHAR(255) DEFAULT NULL COMMENT '封面图片OSS Key',
ADD COLUMN description TEXT DEFAULT NULL COMMENT '分类描述',
ADD COLUMN visibility VARCHAR(20) DEFAULT 'private' COMMENT '可见性: private/public/enterprise',
ADD COLUMN item_count INT DEFAULT 0 COMMENT '分类下项目数量';

-- Add cover_key and description fields to mindmap_categories  
ALTER TABLE mindmap_categories 
ADD COLUMN cover_key VARCHAR(255) DEFAULT NULL COMMENT '封面图片OSS Key',
ADD COLUMN description TEXT DEFAULT NULL COMMENT '分类描述',
ADD COLUMN visibility VARCHAR(20) DEFAULT 'private' COMMENT '可见性: private/public/enterprise',
ADD COLUMN item_count INT DEFAULT 0 COMMENT '分类下项目数量';

-- Add background_style field for card styling
ALTER TABLE note_categories 
ADD COLUMN background_style VARCHAR(100) DEFAULT NULL COMMENT '背景样式: gradient color',
ADD COLUMN badge_text VARCHAR(50) DEFAULT NULL COMMENT '徽章文字';

ALTER TABLE mindmap_categories 
ADD COLUMN background_style VARCHAR(100) DEFAULT NULL COMMENT '背景样式: gradient color',
ADD COLUMN badge_text VARCHAR(50) DEFAULT NULL COMMENT '徽章文字';

-- Create indexes for better performance
CREATE INDEX idx_note_categories_visibility ON note_categories(visibility);
CREATE INDEX idx_note_categories_user_visibility ON note_categories(user_id, visibility);
CREATE INDEX idx_mindmap_categories_visibility ON mindmap_categories(visibility);
CREATE INDEX idx_mindmap_categories_user_visibility ON mindmap_categories(user_id, visibility);

-- Update item count triggers (optional, can also be managed in application)
-- These triggers will automatically update item_count when items are added/removed

-- Note: Since we're using logical foreign keys instead of physical ones,
-- we'll manage item counts in the application layer for better performance