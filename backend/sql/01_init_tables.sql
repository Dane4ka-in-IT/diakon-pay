CREATE TABLE IF NOT EXISTS Users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(254) UNIQUE NOT NULL,
    password TEXT NOT NULL,
    avatar_url TEXT
);
COMMENT ON TABLE Users IS 'Пользователи';
COMMENT ON COLUMN Users.id IS 'уникальный ключ';
COMMENT ON COLUMN Users.name IS 'имя';
COMMENT ON COLUMN Users.email IS 'эл.почта';
COMMENT ON COLUMN Users.password IS 'пароль';
COMMENT ON COLUMN Users.avatar_url IS 'картинка аватарки';


CREATE TABLE IF NOT EXISTS Accounts (
    uuid UUID UNIQUE NOT NULL PRIMARY KEY,
    user_id INT NULL CHECK (user_id > 1),
    bank_name VARCHAR(50) NOT NULL,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    balance NUMERIC(20, 4) NOT NULL CHECK (balance >= 0),
    currency_code CHAR(3) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE SET NULL
);
COMMENT ON TABLE Accounts IS 'Аккаунты';
COMMENT ON COLUMN Accounts.uuid IS 'уникальный ключ';
COMMENT ON COLUMN Accounts.user_id IS 'владелец';
COMMENT ON COLUMN Accounts.bank_name IS 'название банка';
COMMENT ON COLUMN Accounts.account_number IS 'лицевой счёт';
COMMENT ON COLUMN Accounts.balance IS 'баланс';
COMMENT ON COLUMN Accounts.currency_code IS 'буквенный код валюты';
COMMENT ON COLUMN Accounts.is_deleted IS 'удалён ли аккаунт';


CREATE TABLE IF NOT EXISTS Categories (
    uuid UUID UNIQUE NOT NULL PRIMARY KEY,
    user_id INT NULL CHECK (user_id > 0),
    name_category VARCHAR(50) NOT NULL,
    category_description TEXT,
    type VARCHAR(7) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),

    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE SET NULL
);
COMMENT ON TABLE Categories IS 'Категории';
COMMENT ON COLUMN Categories.uuid IS 'уникальный ключ';
COMMENT ON COLUMN Categories.name_category IS 'название';
COMMENT ON COLUMN Categories.category_description IS 'описание';
COMMENT ON COLUMN Categories.type IS 'тип';


CREATE TABLE IF NOT EXISTS Transactions (
    uuid UUID UNIQUE NOT NULL PRIMARY KEY,
    account_id UUID,
    category_id UUID, 
    transactions_description TEXT,
    amount NUMERIC(20, 4) NOT NULL CHECK (amount >= 0),
    exchange_rate NUMERIC(20, 4) CHECK (exchange_rate >= 0),
    transactions_date TIMESTAMP WITH TIME ZONE DEFAULT now(),

    FOREIGN KEY (account_id) REFERENCES Accounts(uuid) ON DELETE SET NULL,
    FOREIGN KEY (category_id) REFERENCES Categories(uuid) ON DELETE SET NULL
);
COMMENT ON TABLE Transactions IS 'Транзакции';
COMMENT ON COLUMN Transactions.uuid IS 'уникальный ключ';
COMMENT ON COLUMN Transactions.account_id IS 'аккаунт-счёт';
COMMENT ON COLUMN Transactions.category_id IS 'тип транзакции';
COMMENT ON COLUMN Transactions.transactions_description IS 'описание транзакции';
COMMENT ON COLUMN Transactions.amount IS 'сумма';
COMMENT ON COLUMN Transactions.exchange_rate IS 'курс валюты';
COMMENT ON COLUMN Transactions.transactions_date IS 'дата транзакции';


CREATE TABLE IF NOT EXISTS AuditLogs (
    id SERIAL PRIMARY KEY,
    entity TEXT NOT NULL,
    entity_id INT NULL CHECK (entity_id > 0),
    entity_uuid UUID,
    action TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
COMMENT ON TABLE AuditLogs IS 'Аудит логов';
COMMENT ON COLUMN AuditLogs.id IS 'уникальный ключ';
COMMENT ON COLUMN AuditLogs.entity IS 'объект (таблица)';
COMMENT ON COLUMN AuditLogs.entity_id IS 'ключ строки (int)';
COMMENT ON COLUMN AuditLogs.entity_uuid IS 'ключ строки (uuid)';
COMMENT ON COLUMN AuditLogs.action IS 'действие';
COMMENT ON COLUMN AuditLogs.created_at IS 'дата создания';