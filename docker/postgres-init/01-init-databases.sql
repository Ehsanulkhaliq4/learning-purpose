-- Create isolated service databases
CREATE DATABASE user_db;
CREATE DATABASE exam_db;
CREATE DATABASE bookstore_db;
CREATE DATABASE blog_db;
CREATE DATABASE ai_chat_db;

-- Enable pgvector extension for RAG vector searches
\connect ai_chat_db;
CREATE EXTENSION IF NOT EXISTS vector;

GRANT ALL PRIVILEGES ON DATABASE user_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE exam_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE bookstore_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE blog_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE ai_chat_db TO postgres;