-- Fichier : src/main/resources/schema.sql

-- 1. Nettoyage initial
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO CURRENT_USER;
GRANT ALL ON SCHEMA public TO PUBLIC;

-- =============================================
-- TABLE 1 : app_user (Entité principale)
-- =============================================
CREATE TABLE IF NOT EXISTS app_user (
                                        id BIGSERIAL PRIMARY KEY,
                                        keycloak_id TEXT UNIQUE NOT NULL,
                                        profile_pic_url TEXT,
                                        joined_date TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX idx_app_user_keycloak_id ON app_user (keycloak_id);