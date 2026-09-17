CREATE TABLE videos (
                        id BIGSERIAL PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        description TEXT,
                        object_name VARCHAR(500) NOT NULL UNIQUE,
                        content_type VARCHAR(100) NOT NULL,
                        file_size BIGINT NOT NULL,
                        view_count BIGINT NOT NULL DEFAULT 0,
                        like_count BIGINT NOT NULL DEFAULT 0,
                        uploaded_by VARCHAR(150) NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE video_likes (
                             id BIGSERIAL PRIMARY KEY,
                             video_id BIGINT NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
                             username VARCHAR(150) NOT NULL,
                             created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT uq_video_user_like UNIQUE (video_id, username)
);

CREATE INDEX idx_videos_created_at ON videos(created_at DESC);
CREATE INDEX idx_video_likes_video_id ON video_likes(video_id);
CREATE INDEX idx_video_likes_lookup ON video_likes(video_id, username);