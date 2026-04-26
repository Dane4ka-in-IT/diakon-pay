CREATE OR REPLACE FUNCTION update_balance_fn()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    v_type VARCHAR(7);
    v_old_type VARCHAR(7);
BEGIN
    IF (TG_OP = 'INSERT') THEN
        SELECT type INTO v_type
        FROM Categories
        WHERE uuid = NEW.category_id;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'Category not found: %', NEW.category_id;
        END IF;

        IF v_type = 'EXPENSE' THEN
            UPDATE Accounts
            SET balance = balance - NEW.amount
            WHERE uuid = NEW.account_id;
        ELSE
            UPDATE Accounts
            SET balance = balance + NEW.amount
            WHERE uuid = NEW.account_id;
        END IF;
        
    ELSIF (TG_OP = 'DELETE') THEN
        SELECT type INTO v_type
        FROM Categories
        WHERE uuid = OLD.category_id;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'Category not found: %', OLD.category_id;
        END IF;

        IF v_type = 'EXPENSE' THEN
            UPDATE Accounts
            SET balance = balance + OLD.amount
            WHERE uuid = OLD.account_id;
        ELSE
            UPDATE Accounts
            SET balance = balance - OLD.amount
            WHERE uuid = OLD.account_id;
        END IF;
        
    ELSIF (TG_OP = 'UPDATE') THEN
        SELECT type INTO v_old_type
        FROM Categories
        WHERE uuid = OLD.category_id;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'Category not found: %', NEW.category_id;
        END IF;

        IF v_old_type = 'EXPENSE' THEN
            UPDATE Accounts
            SET balance = balance + OLD.amount
            WHERE uuid = OLD.account_id;
        ELSE
            UPDATE Accounts
            SET balance = balance - OLD.amount
            WHERE uuid = OLD.account_id;
        END IF;

        SELECT type INTO v_type
        FROM Categories
        WHERE uuid = NEW.category_id;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'Category not found: %', NEW.category_id;
        END IF;

        IF v_type = 'EXPENSE' THEN
            UPDATE Accounts
            SET balance = balance - NEW.amount
            WHERE uuid = NEW.account_id;
        ELSE
            UPDATE Accounts
            SET balance = balance + NEW.amount
            WHERE uuid = NEW.account_id;
        END IF;
    END IF;
    
    IF (TG_OP = 'DELETE') THEN
        RETURN OLD;
    END IF;
    RETURN NEW;

EXCEPTION
    WHEN others THEN
        RAISE EXCEPTION 'update_balance_fn failed: %, SQLSTATE: %', SQLERRM, SQLSTATE;
END;
$$;

CREATE TRIGGER trg_update_balance AFTER INSERT OR UPDATE OR DELETE ON Transactions FOR EACH ROW EXECUTE FUNCTION update_balance_fn();


CREATE OR REPLACE FUNCTION audit_log_fn()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    v_entity_id INT := NULL;
    v_entity_uuid UUID := NULL;
BEGIN
    IF (TG_TABLE_NAME = 'Users') THEN
        v_entity_id := (CASE WHEN TG_OP = 'DELETE' THEN OLD.id ELSE NEW.id END);
    ELSE
        v_entity_uuid := (CASE WHEN TG_OP = 'DELETE' THEN OLD.uuid ELSE NEW.uuid END);
    END IF;

    INSERT INTO AuditLogs(entity, entity_id, entity_uuid, action)
    VALUES (TG_TABLE_NAME, v_entity_id, v_entity_uuid, TG_OP);

    IF (TG_OP = 'DELETE') THEN
        RETURN OLD;
    END IF;
    RETURN NEW;

EXCEPTION
    WHEN others THEN
        RAISE EXCEPTION 'audit_log_fn failed: %, SQLSTATE: %', SQLERRM, SQLSTATE;
END;
$$;

CREATE TRIGGER trg_audit_users AFTER INSERT OR UPDATE OR DELETE ON Users FOR EACH ROW EXECUTE FUNCTION audit_log_fn();
CREATE TRIGGER trg_audit_accounts AFTER INSERT OR UPDATE OR DELETE ON Accounts FOR EACH ROW EXECUTE FUNCTION audit_log_fn();
CREATE TRIGGER trg_audit_categories AFTER INSERT OR UPDATE OR DELETE ON Categories FOR EACH ROW EXECUTE FUNCTION audit_log_fn();
CREATE TRIGGER trg_audit_transactions AFTER INSERT OR UPDATE OR DELETE ON Transactions FOR EACH ROW EXECUTE FUNCTION audit_log_fn();
