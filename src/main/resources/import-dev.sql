
--
-- Struktur-dump for tabellen `short_scheme`
--
CREATE TABLE short_scheme (
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


--
-- Struktur-dump for tabellen `short_url_tag`
--
CREATE TABLE short_url_tag (
  tag        varchar(255) NOT NULL,
  scheme_id  bigint,
  target     varchar(255),

  PRIMARY KEY (tag),
  UNIQUE KEY uq_short_url_tag_1 (scheme_id, target),
  KEY ix_short_url_tag_scheme_1 (scheme_id)
);


--
-- Data dump for tabellen `short_scheme`
--
INSERT INTO short_scheme (id, name, description, target_pattern, replacement, shortcut_prefix, tag_alphabet, tag_length) VALUES
(4, 'lot-id', 'QR-kode til emneetiket, LIVE', '([0-9A-F]+)', 'https://bruun-rasmussen.dk/m/lots/$1?utm_source=lot-label&utm_medium=qr-code&utm_campaign=none', 'http://bra.dk/', 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789._-@', 5),
(3, 'modern-news', 'QR-kode til nyhedsartikel', '(.+)', 'http://bruun-rasmussen.dk/m/news/$1?utm_source=banner&utm_medium=qr-code&utm_campaign=none', 'http://bra.dk/', 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789', 5);


--
-- Data dump for tabellen `short_url_tag`
--
INSERT INTO short_url_tag (tag, scheme_id, target) VALUES
('QNr0H', 4, 'F6B2A079A3AE'),
('cVsWU', 4, '4D48C8650442'),
('kIxYZ', 4, '21B6E90B3228'),
('Iawjq', 4, '2D0605A02F5E'),
('d7kGr', 4, '8A3A45780D7D'),
('mEd4E', 4, '4443D2A89701'),
('yigT_', 4, '0EF1B09F4CD9'),
('2loTv', 4, '1217BDA3345C'),
('CdaXm', 4, '5A8EBF0DFEFA'),
('RSd_P', 4, '63FD7D440F07'),
('p10HV', 4, '41A0A3CA4F42'),
('d7BKv', 4, 'BE2CE7ACC07B'),
('MJiY5', 4, 'FD43C474DE0E'),
('lOqfW', 4, '7AB552ECD1CF'),
('66lbY', 4, 'A591002FF9C0');
