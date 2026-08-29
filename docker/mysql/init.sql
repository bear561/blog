CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE blog;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(64),
    avatar VARCHAR(512),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 分类表
CREATE TABLE IF NOT EXISTS t_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    slug VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255),
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 标签表
CREATE TABLE IF NOT EXISTS t_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    slug VARCHAR(64) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文章表
CREATE TABLE IF NOT EXISTS t_article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    summary VARCHAR(512),
    content TEXT,
    content_html TEXT,
    cover_image VARCHAR(512),
    category_id BIGINT,
    is_published TINYINT(1) DEFAULT 0,
    is_top TINYINT(1) DEFAULT 0,
    view_count BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_published_created (is_published, created_at),
    INDEX idx_category (category_id),
    FULLTEXT INDEX ft_content (title, summary, content),
    FOREIGN KEY (category_id) REFERENCES t_category(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文章-标签关联表
CREATE TABLE IF NOT EXISTS t_article_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    UNIQUE KEY uk_article_tag (article_id, tag_id),
    FOREIGN KEY (article_id) REFERENCES t_article(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES t_tag(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评论表
CREATE TABLE IF NOT EXISTS t_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    parent_id BIGINT,
    reply_to_id BIGINT,
    nickname VARCHAR(64) NOT NULL,
    email VARCHAR(128),
    content TEXT NOT NULL,
    is_reviewed TINYINT(1) DEFAULT 1,
    is_anonymous TINYINT(1) DEFAULT 0,
    ip VARCHAR(64),
    user_agent VARCHAR(512),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_article_reviewed (article_id, is_reviewed, created_at),
    FOREIGN KEY (article_id) REFERENCES t_article(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 友链表
CREATE TABLE IF NOT EXISTS t_friend_link (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    url VARCHAR(512) NOT NULL,
    avatar VARCHAR(512),
    description VARCHAR(255),
    sort_order INT DEFAULT 0,
    is_visible TINYINT(1) DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 站点配置表
CREATE TABLE IF NOT EXISTS t_site_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(128) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- AI 对话记录表
CREATE TABLE IF NOT EXISTS t_ai_conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64),
    question TEXT,
    answer TEXT,
    tokens_used INT DEFAULT 0,
    model VARCHAR(64),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入默认管理员 (密码: admin123, BCrypt加密)
INSERT INTO t_user (username, password, nickname) VALUES
('admin', '$2b$10$3X1b9btJpSpU5f4raczhsuDFP525yz.e30ND0jXDwO59bM2KYxwTS', '站长');

-- 插入默认分类
INSERT INTO t_category (name, slug, description, sort_order) VALUES
('技术', 'tech', '技术相关文章', 1),
('生活', 'life', '生活随笔', 2);

-- 插入默认站点配置
INSERT INTO t_site_config (config_key, config_value, description) VALUES
('site_name', 'My Blog', '站点名称'),
('site_description', '一个技术博客', '站点描述'),
('site_keywords', 'blog,技术,博客', 'SEO关键词'),
('about_content', '# 关于我\n\n这是我的博客。', '关于页内容'),
('footer_info', '© 2024 My Blog. Powered by Vue + Spring Boot.', '页脚信息'),
('icp_number', '赣ICP备2026019609号', 'ICP备案号'),
('ai_welcome_message', '你好！我是本站的AI助手，我可以帮你：\n- 了解博客的内容\n- 查找特定主题的文章\n- 解答技术问题\n有什么可以帮你的？', 'AI欢迎语'),
('ai_suggestions', '["最近发布了哪些文章？","介绍一下这个博客","有哪些技术分类的文章？","如何联系站长？"]', 'AI推荐问题JSON数组');
