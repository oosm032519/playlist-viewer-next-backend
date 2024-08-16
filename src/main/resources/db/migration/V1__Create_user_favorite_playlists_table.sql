CREATE TABLE user_favorite_playlists
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             VARCHAR(255) NOT NULL,
    playlist_id         VARCHAR(255) NOT NULL,
    playlist_name       VARCHAR(255) NOT NULL,
    total_tracks        INT          NOT NULL,
    added_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    playlist_owner_name VARCHAR(255) NOT NULL
);
