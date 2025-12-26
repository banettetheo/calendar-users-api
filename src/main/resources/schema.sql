-- Fichier : src/main/resources/schema.sql

-- 1. Nettoyage initial
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO CURRENT_USER;
GRANT ALL ON SCHEMA public TO PUBLIC;

-- 2. Création de la table
CREATE TABLE IF NOT EXISTS app_user (
                                        id BIGSERIAL PRIMARY KEY,
                                        keycloak_id TEXT UNIQUE NOT NULL,
                                        user_name TEXT NOT NULL,
                                        hashtag INTEGER NOT NULL, -- Stockage du nombre seul (ex: 1234)
                                        first_name TEXT,
                                        last_name TEXT,
                                        profile_pic_url TEXT,
                                        joined_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    -- CONTRAINTE D'UNICITÉ : Garantit que le couple Nom + Hashtag est unique
                                        CONSTRAINT unique_user_identity UNIQUE (user_name, hashtag)
);