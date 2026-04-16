-- Удаляем constraint NOT NULL с email
ALTER TABLE users ALTER COLUMN email DROP NOT NULL;

-- Проверяем что изменилось
\d users
