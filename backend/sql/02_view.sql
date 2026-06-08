CREATE OR REPLACE VIEW AnalyticsForCategory AS
SELECT
    a.user_id AS user_id,
    c.name_category,
    COALESCE(SUM(t.amount), 0) AS total_amount,
    DATE_TRUNC('month', t.transactions_date) AS t_month
FROM Accounts a
LEFT JOIN Transactions t ON t.account_id = a.uuid
LEFT JOIN Categories c ON t.category_id = c.uuid
GROUP BY a.user_id, t_month, c.name_category
ORDER BY a.user_id, t_month;