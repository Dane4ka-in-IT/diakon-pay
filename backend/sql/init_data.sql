INSERT INTO Users(name, email, password) VALUES ('general', 'email', 'general');

INSERT INTO categories (user_id, name_category, type) VALUES
    (1, 'Пополнение счета', 'INCOME'),
    (1, 'Зарплата', 'INCOME'),
    (1, 'Еда и рестораны', 'EXPENSE'),
    (1, 'Транспорт', 'EXPENSE'),
    (1, 'ЖКХ и связь', 'EXPENSE'),
    (1, 'Развлечения', 'EXPENSE'),
    (1, 'Перевод', 'EXPENSE');