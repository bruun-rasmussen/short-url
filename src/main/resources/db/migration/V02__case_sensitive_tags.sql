--
-- Tags are case-sensitive: 'uCHwn' and 'UCHwn' are distinct tags. The legacy
-- case-insensitive collation made them clash and shrank the tag namespace.
--

ALTER TABLE short_url_tag
  MODIFY tag varchar(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL;
