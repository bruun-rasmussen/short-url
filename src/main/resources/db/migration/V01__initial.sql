--
-- Idempotent SQL script for creating the initial database schema to match
-- what was running in production at the time of Flyway introduction.
--

CREATE TABLE IF NOT EXISTS short_scheme (
  id              bigint NOT NULL AUTO_INCREMENT,
  name            varchar(255),
  description     varchar(255),
  target_pattern  varchar(255),
  replacement     varchar(255),
  shortcut_prefix varchar(255),
  tag_alphabet    varchar(255),
  tag_length      int,

  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS short_url_tag (
  tag        varchar(255) NOT NULL,
  scheme_id  bigint,
  target     varchar(255),

  PRIMARY KEY (tag),
  UNIQUE KEY uq_short_url_tag_1 (scheme_id, target),
  KEY ix_short_url_tag_scheme_1 (scheme_id)
);
