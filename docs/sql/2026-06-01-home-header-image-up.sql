INSERT INTO site_setting (name_en, name_zh, value, type)
SELECT 'homeHeaderImage', '首页首屏背景图', '/img/header/hitokoto-bg.jpg.jpeg', 1
WHERE NOT EXISTS (
    SELECT 1 FROM site_setting WHERE name_en = 'homeHeaderImage'
);
