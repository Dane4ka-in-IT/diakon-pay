INSERT INTO Users (name, email, password) 
VALUES ('System_General', 'admin@diakonpay.dev', 'root_pass');

INSERT INTO Users (name, email, password, avatar_url) 
VALUES ('IT_Diakon', 'diakon@test.ru', 'secure_pass', 'https://api.dicebear.com/7.x/avataaars/svg?seed=Lucky');

INSERT INTO Categories (uuid, user_id, name_category, type, category_description) VALUES
    (gen_random_uuid(), 1, 'Зарплата', 'INCOME', 'Основной доход'),
    (gen_random_uuid(), 1, 'Переводы', 'INCOME', 'Входящие переводы'),
    (gen_random_uuid(), 1, 'Продукты', 'EXPENSE', 'Еда и супермаркеты'),
    (gen_random_uuid(), 1, 'Транспорт', 'EXPENSE', 'Такси, метро, бензин'),
    (gen_random_uuid(), 1, 'Развлечения', 'EXPENSE', 'Кино, игры, хобби'),
    (gen_random_uuid(), 1, 'ЖКХ', 'EXPENSE', 'Коммунальные платежи');

INSERT INTO Accounts (uuid, user_id, bank_name, account_number, balance, currency_code) VALUES
    ('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 2, 'Т-Банк', '*4455', 120000.00, 'RUB'),
    ('b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 2, 'Сбербанк', '*1234', 5000.50, 'RUB');

INSERT INTO Transactions (uuid, account_id, category_id, transactions_description, amount, exchange_rate) VALUES
    (gen_random_uuid(), 
     'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 
     (SELECT uuid FROM Categories WHERE name_category = 'Продукты' LIMIT 1), 
     'Шаурма в 2 ночи', 350.00, 1.0),
    
    (gen_random_uuid(), 
     'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 
     (SELECT uuid FROM Categories WHERE name_category = 'Зарплата' LIMIT 1), 
     'Аванс за MVP', 45000.00, 1.0);