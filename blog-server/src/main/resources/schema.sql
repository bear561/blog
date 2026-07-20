CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(64),
    avatar VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    slug VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    slug VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    summary VARCHAR(512),
    content CLOB,
    content_html CLOB,
    cover_image VARCHAR(512),
    category_id BIGINT,
    is_published INT DEFAULT 0,
    is_top INT DEFAULT 0,
    view_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES t_category(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS t_article_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    UNIQUE (article_id, tag_id),
    FOREIGN KEY (article_id) REFERENCES t_article(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES t_tag(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS t_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    parent_id BIGINT,
    reply_to_id BIGINT,
    nickname VARCHAR(64) NOT NULL,
    email VARCHAR(128),
    website VARCHAR(256),
    content CLOB NOT NULL,
    is_reviewed INT DEFAULT 0,
    ip VARCHAR(64),
    user_agent VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES t_article(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS t_friend_link (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    url VARCHAR(512) NOT NULL,
    avatar VARCHAR(512),
    description VARCHAR(255),
    sort_order INT DEFAULT 0,
    is_visible INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_site_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(128) NOT NULL UNIQUE,
    config_value CLOB,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS t_ai_conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64),
    question CLOB,
    answer CLOB,
    tokens_used INT DEFAULT 0,
    model VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Default data
MERGE INTO t_user (id, username, password, nickname) KEY(id) VALUES
(1, 'admin', '$2b$10$tzHHAX3yqd01FPWp/0VgfuMh0aUwL.qAHyYEbgIPdDUj6yg09RZvG', '站长');

MERGE INTO t_category (id, name, slug, description, sort_order) KEY(id) VALUES
(1, '技术', 'tech', '技术相关文章', 1),
(2, '生活', 'life', '生活随笔', 2);

MERGE INTO t_site_config (id, config_key, config_value, description) KEY(id) VALUES
(1, 'site_name', 'My Blog', '站点名称'),
(2, 'site_description', '一个技术博客', '站点描述'),
(3, 'site_keywords', 'blog,技术,博客', 'SEO关键词'),
(4, 'about_content', '# 关于我\n\n这是我的博客。', '关于页内容'),
(5, 'footer_info', '© 2024 My Blog. Powered by Vue + Spring Boot.', '页脚信息'),
(6, 'ai_welcome_message', '你好！我是本站的AI助手，有什么可以帮你的？', 'AI欢迎语'),
(7, 'ai_suggestions', '["最近发布了哪些文章？","介绍一下这个博客","有哪些技术分类的文章？","如何联系站长？"]', 'AI推荐问题');
