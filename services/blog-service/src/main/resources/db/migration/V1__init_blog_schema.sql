CREATE TABLE IF NOT EXISTS posts (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    posted_by VARCHAR(100) NOT NULL,
    image_storage_key VARCHAR(500),
    view_count INTEGER NOT NULL DEFAULT 0,
    like_count INTEGER NOT NULL DEFAULT 0,
    tags JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                             );

CREATE TABLE IF NOT EXISTS comments (
                                        id BIGSERIAL PRIMARY KEY,
                                        post_id BIGINT NOT NULL,
                                        content TEXT NOT NULL,
                                        posted_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_posts_posted_by ON posts(posted_by);
CREATE INDEX IF NOT EXISTS idx_posts_tags_jsonb ON posts USING gin (tags);
CREATE INDEX IF NOT EXISTS idx_comments_post ON comments(post_id);