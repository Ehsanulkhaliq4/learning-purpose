CREATE TABLE IF NOT EXISTS book_catalog (
                                            id BIGSERIAL PRIMARY KEY,
                                            book_title VARCHAR(255) NOT NULL,
    book_author_name VARCHAR(150) NOT NULL,
    posted_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              content_type VARCHAR(50),
    book_description TEXT,
    cover_image_key VARCHAR(500),
    pdf_storage_key VARCHAR(500)
    );

CREATE INDEX IF NOT EXISTS idx_books_title ON book_catalog(book_title);
CREATE INDEX IF NOT EXISTS idx_books_author ON book_catalog(book_author_name);

-- =========================================================
-- Debezium logical replication publication
-- =========================================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_publication
        WHERE pubname = 'book_publication'
    ) THEN
        CREATE PUBLICATION book_publication
        FOR TABLE public.book_catalog;
END IF;
END
$$;