CREATE OR REPLACE VIEW AnalyticsForCategory AS
SELECT
    a.user_id AS user_id,
    c.name_category,
    COALESCE(SUM(t.amount), 0) AS total_amount,
    MIN(t.transactions_date) AS start_date,
    MAX(t.transactions_date) AS end_date
FROM Accounts a
LEFT JOIN Transactions t ON t.account_id = a.uuid
LEFT JOIN Categories c ON t.category_id = c.uuid
GROUP BY a.user_id, c.name_category;