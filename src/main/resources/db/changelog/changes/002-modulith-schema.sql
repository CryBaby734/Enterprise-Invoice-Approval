--liquibase formatted sql

--changeset yourname:004-create-event-publication-table
CREATE TABLE event_publication (
                                   id UUID PRIMARY KEY,
                                   listener_id VARCHAR(512) NOT NULL,
                                   event_type VARCHAR(512) NOT NULL,
                                   serialized_event TEXT NOT NULL,
                                   publication_date TIMESTAMP NOT NULL,
                                   completion_date TIMESTAMP
);

CREATE INDEX idx_event_publication_completion_date ON event_publication(completion_date);

-- 👇 ДОБАВЛЯЕМ ВОТ ЭТОТ БЛОК НИЖЕ 👇

--changeset yourname:005-create-event-publication-archive-table
CREATE TABLE event_publication_archive (
                                           id UUID PRIMARY KEY,
                                           listener_id VARCHAR(512) NOT NULL,
                                           event_type VARCHAR(512) NOT NULL,
                                           serialized_event TEXT NOT NULL,
                                           publication_date TIMESTAMP NOT NULL,
                                           completion_date TIMESTAMP NOT NULL
);

CREATE INDEX idx_event_publication_archive_completion_date ON event_publication_archive(completion_date);