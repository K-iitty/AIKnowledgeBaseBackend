-- Add chat session and message tables for AI chat memory/history

CREATE TABLE IF NOT EXISTS chat_sessions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  mode VARCHAR(32) DEFAULT 'default',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天会话';

-- idx_chat_sessions_user
SET @db := DATABASE();
SET @exists := (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='chat_sessions' AND INDEX_NAME='idx_chat_sessions_user');
SET @sql := IF(@exists=0, 'ALTER TABLE chat_sessions ADD INDEX idx_chat_sessions_user (user_id)', 'SELECT 1');
PREPARE s1 FROM @sql; EXECUTE s1; DEALLOCATE PREPARE s1;

CREATE TABLE IF NOT EXISTS chat_messages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  role VARCHAR(32) NOT NULL,
  content TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天消息';

-- idx_chat_messages_session
SET @exists := (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='chat_messages' AND INDEX_NAME='idx_chat_messages_session');
SET @sql := IF(@exists=0, 'ALTER TABLE chat_messages ADD INDEX idx_chat_messages_session (session_id)', 'SELECT 1');
PREPARE s2 FROM @sql; EXECUTE s2; DEALLOCATE PREPARE s2;
