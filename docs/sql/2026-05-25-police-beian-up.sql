INSERT INTO site_setting (name_en, name_zh, value, type)
SELECT 'policeBeian', '联网备案号', '津公网安备12011102001866号', 1
WHERE NOT EXISTS (
    SELECT 1 FROM site_setting WHERE name_en = 'policeBeian'
);
