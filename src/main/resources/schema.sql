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

-- Index pour la recherche par clé externe (déjà vu)
CREATE UNIQUE INDEX idx_app_user_keycloak_id ON app_user (keycloak_id);


-- =============================================
-- TABLE 2 : user_friends (Amitiés ACCEPTÉES)
-- =============================================
CREATE TABLE IF NOT EXISTS user_friends (
                                            id BIGSERIAL PRIMARY KEY,
                                            user_id BIGINT NOT NULL, -- L'utilisateur qui a validé la relation (selon votre modèle)
                                            friend_id BIGINT NOT NULL, -- L'ami

                                            UNIQUE (user_id, friend_id),
                                            CHECK (user_id != friend_id),

                                            CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
                                            CONSTRAINT fk_friend FOREIGN KEY (friend_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- Index pour accélérer la jointure principale sur les amitiés
CREATE INDEX idx_friends_by_user ON user_friends (user_id);


-- =============================================
-- TABLE 3 : friend_requests (Demandes en ATTENTE)
-- =============================================
CREATE TABLE IF NOT EXISTS friend_requests (
                                               id BIGSERIAL PRIMARY KEY,
                                               sender_id BIGINT NOT NULL,   -- L'utilisateur qui a envoyé la demande
                                               receiver_id BIGINT NOT NULL,  -- L'utilisateur qui doit accepter
                                               requested_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Empêche qu'une demande soit envoyée deux fois
                                               UNIQUE (sender_id, receiver_id),

    -- Contrainte pour gérer les cas d'erreur de logique
                                               CHECK (sender_id != receiver_id),

                                               CONSTRAINT fk_sender FOREIGN KEY (sender_id) REFERENCES app_user(id) ON DELETE CASCADE,
                                               CONSTRAINT fk_receiver FOREIGN KEY (receiver_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- Index pour accélérer la vérification des demandes reçues et envoyées
-- (Essentiel pour la procédure stockée et la requête de statut)
CREATE INDEX idx_fr_receiver ON friend_requests (receiver_id);
CREATE INDEX idx_fr_sender ON friend_requests (sender_id);