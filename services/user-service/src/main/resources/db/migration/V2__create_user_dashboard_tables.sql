-- 1. Weekly study rhythm and logged hours per user
CREATE TABLE IF NOT EXISTS user_daily_study_rhythm (
                                                       id BIGSERIAL PRIMARY KEY,
                                                       user_id BIGINT NOT NULL,
                                                       study_date DATE NOT NULL,
                                                       hours_spent NUMERIC(4,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT fk_study_rhythm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_study_date UNIQUE (user_id, study_date)
    );

-- 2. Aggregated user learning metrics (streak, lessons, goals)
CREATE TABLE IF NOT EXISTS user_learning_metrics (
                                                     id BIGSERIAL PRIMARY KEY,
                                                     user_id BIGINT NOT NULL UNIQUE,
                                                     current_streak_days INT NOT NULL DEFAULT 0,
                                                     streak_diff_last_week INT NOT NULL DEFAULT 0,
                                                     completed_lessons INT NOT NULL DEFAULT 0,
                                                     lessons_completed_this_week INT NOT NULL DEFAULT 0,
                                                     assessment_score NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    is_personal_best BOOLEAN NOT NULL DEFAULT FALSE,
    study_time_hours NUMERIC(4,2) NOT NULL DEFAULT 0.00,
    weekly_goal_hours NUMERIC(4,2) NOT NULL DEFAULT 6.00,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT fk_metrics_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- 3. User activity timeline (completed quizzes, video watches, assessments)
CREATE TABLE IF NOT EXISTS user_activity_log (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 user_id BIGINT NOT NULL,
                                                 title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(255) NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    status_color VARCHAR(30) NOT NULL DEFAULT 'blue',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- 4. In-progress learning items (resumed quizzes, modules)
CREATE TABLE IF NOT EXISTS user_enrolled_items (
                                                   id BIGSERIAL PRIMARY KEY,
                                                   user_id BIGINT NOT NULL,
                                                   item_type VARCHAR(50) NOT NULL,
    reference_id BIGINT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    progress_percentage INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'IN PROGRESS',
    action_text VARCHAR(50) NOT NULL DEFAULT 'Continue ->',
    action_url VARCHAR(500) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT fk_enrolled_items_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Performance Indexes
CREATE INDEX IF NOT EXISTS idx_study_rhythm_user_date ON user_daily_study_rhythm(user_id, study_date DESC);
CREATE INDEX IF NOT EXISTS idx_activity_log_user_time ON user_activity_log(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_enrolled_items_user ON user_enrolled_items(user_id, updated_at DESC);