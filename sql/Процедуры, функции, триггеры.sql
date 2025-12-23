--- 1. Группа «Авторизация и управление пользователями» ---
-- 1.1. Процедура регистрации клиента --
CREATE OR REPLACE PROCEDURE register_client(
    p_firstname VARCHAR,
    p_lastname VARCHAR,
    p_phone VARCHAR,
    p_email VARCHAR,
    p_password_hash VARCHAR, -- Хеш пароля (SHA-256), генерируется приложением
    p_passport_data VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    -- 1. Валидация: Проверка на существование пользователя с таким Email
    IF EXISTS (SELECT 1 FROM Clients WHERE Email = p_email) THEN
        RAISE EXCEPTION 'Пользователь с адресом % уже зарегистрирован', p_email;
    END IF;

    -- 2. Создание записи
    INSERT INTO Clients (
        FirstName, LastName, Phone, Email, PasswordHash, PassportData, RegistrationDate
    )
    VALUES (
        p_firstname, p_lastname, p_phone, p_email, p_password_hash, p_passport_data, CURRENT_TIMESTAMP
    );
    
    -- Логирование сработает автоматически благодаря триггеру
END;
$$;


-- 1.2. Функция проверки учетных данных --
CREATE OR REPLACE FUNCTION check_credentials(
    p_email VARCHAR,
    p_password_hash VARCHAR
) RETURNS INT 
LANGUAGE plpgsql
AS $$
DECLARE
    v_client_id INT;
BEGIN
    -- Поиск клиента с совпадением и Email, и Хеша пароля
    SELECT ClientID INTO v_client_id
    FROM Clients
    WHERE Email = p_email AND PasswordHash = p_password_hash;

    -- Если клиент найден, возвращаем его ID
    IF v_client_id IS NOT NULL THEN
        RETURN v_client_id;
    ELSE
        -- Возвращаем -1 как код ошибки "Неверный логин или пароль"
        RETURN -1;
    END IF;
END;
$$;



--- 2. Группа «Операционная деятельность» ---
-- 2.1. Функция создания нового тура --
CREATE OR REPLACE FUNCTION create_tour(
    p_tour_type_id INT,
    p_manager_id INT,
    p_partner_id INT,
    p_destination VARCHAR,
    p_start_date DATE,
    p_end_date DATE,
    p_base_cost DECIMAL,
    p_description TEXT
) RETURNS INT 
LANGUAGE plpgsql
AS $$
DECLARE
    new_tour_id INT;
BEGIN
    INSERT INTO Tours (
        TourTypeID, ManagerID, PartnerID, Destination, 
        StartDate, EndDate, BaseCost, Description, IsActive
    )
    VALUES (
        p_tour_type_id, p_manager_id, p_partner_id, p_destination, 
        p_start_date, p_end_date, p_base_cost, p_description, TRUE
    )
    RETURNING TourID INTO new_tour_id;
    
    RETURN new_tour_id; -- Возвращаем ID созданного тура для интерфейса
END;
$$;


-- 2.2. Функция оформления бронирования (Smart Booking) --
CREATE OR REPLACE FUNCTION create_booking(
    p_tour_id INT,
    p_client_id INT,
    p_manager_id INT -- Может быть NULL, если клиент бронирует самостоятельно
) RETURNS INT 
LANGUAGE plpgsql
AS $$
DECLARE
    new_booking_id INT;
    v_tour_cost DECIMAL(10,2);
    v_is_active BOOLEAN;
BEGIN
    -- 1. Проверка актуальности тура и получение цены
    SELECT BaseCost, IsActive INTO v_tour_cost, v_is_active
    FROM Tours 
    WHERE TourID = p_tour_id;
    
    -- Обработка ошибок валидации
    IF v_tour_cost IS NULL THEN
        RAISE EXCEPTION 'Тур с ID % не найден', p_tour_id;
    END IF;

    IF v_is_active = FALSE THEN
        RAISE EXCEPTION 'Тур с ID % находится в архиве и недоступен для бронирования', p_tour_id;
    END IF;

    -- 2. Создание брони с фиксацией цены (Snapshot Price)
    -- Мы записываем v_tour_cost в поле FinalPrice, чтобы история продаж не менялась
    -- при изменении цены в каталоге
    INSERT INTO Bookings (
        TourID, ClientID, ManagerID, 
        BookingDate, Status, PaymentStatus, FinalPrice
    )
    VALUES (
        p_tour_id, p_client_id, p_manager_id, 
        CURRENT_TIMESTAMP, 'В обработке', 'Не оплачено', v_tour_cost
    )
    RETURNING BookingID INTO new_booking_id;

    RETURN new_booking_id;
END;
$$;


-- 2.3 Функция для личного кабинета--
CREATE OR REPLACE FUNCTION get_my_tours(
    p_client_id INT
) RETURNS TABLE (
    BookingID INT,
    BookingDate DATE,
    Destination VARCHAR,
    Dates TEXT,         -- В старой версии, скорее всего, были отдельные даты, а тут текст
    Cost DECIMAL,
    Status VARCHAR,
    PaymentStatus VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        b.BookingID,
        b.BookingDate::DATE,
        t.Destination,
        -- Форматирование дат в строку
        (TO_CHAR(t.StartDate, 'DD.MM.YYYY') || ' - ' || TO_CHAR(t.EndDate, 'DD.MM.YYYY')) AS Dates,
        b.FinalPrice,
        b.Status,
        b.PaymentStatus
    FROM Bookings b
    JOIN Tours t ON b.TourID = t.TourID
    WHERE b.ClientID = p_client_id
    ORDER BY b.BookingDate DESC;
END;
$$;


--- 3. Группа «Финансы» ---
-- 3.1. Процедура регистрации платежа --
CREATE OR REPLACE PROCEDURE add_payment(
    p_booking_id INT,
    p_amount DECIMAL,
    p_method VARCHAR,
    p_transaction_info VARCHAR
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_total_paid DECIMAL(10,2);
    v_final_price DECIMAL(10,2);
BEGIN
    -- 1. Регистрация транзакции
    INSERT INTO Payments (BookingID, Amount, PaymentMethod, TransactionInfo, PaymentDate)
    VALUES (p_booking_id, p_amount, p_method, p_transaction_info, CURRENT_TIMESTAMP);

    -- 2. Расчет общей суммы всех платежей по этой заявке
    SELECT COALESCE(SUM(Amount), 0) INTO v_total_paid
    FROM Payments
    WHERE BookingID = p_booking_id;

    -- 3. Получение итоговой стоимости тура
    SELECT FinalPrice INTO v_final_price
    FROM Bookings
    WHERE BookingID = p_booking_id;

    -- 4. Автоматическое обновление статуса оплаты
    IF v_total_paid >= v_final_price THEN
        -- Полная оплата
        UPDATE Bookings SET PaymentStatus = 'Оплачено' WHERE BookingID = p_booking_id;
    ELSIF v_total_paid > 0 THEN
        -- Частичная оплата (аванс)
        UPDATE Bookings SET PaymentStatus = 'Частично' WHERE BookingID = p_booking_id;
    ELSE
        -- На случай возвратов (отмены платежей)
        UPDATE Bookings SET PaymentStatus = 'Не оплачено' WHERE BookingID = p_booking_id;
    END IF;
    
    -- Примечание: Логирование в AuditLog произойдет автоматически через триггер
END;
$$;

-- 3.2. Функция финансового отчета --
CREATE OR REPLACE FUNCTION report_payments_by_period(
    p_start_date DATE,
    p_end_date DATE
) RETURNS TABLE (
    PaymentID INT,
    BookingID INT,
    ClientName TEXT,
    PaymentDate TIMESTAMP,
    Amount DECIMAL,
    Method VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        p.PaymentID,
        p.BookingID,
        (c.FirstName || ' ' || c.LastName) AS ClientName,
        p.PaymentDate,
        p.Amount,
        p.PaymentMethod
    FROM Payments p
    JOIN Bookings b ON p.BookingID = b.BookingID
    JOIN Clients c ON b.ClientID = c.ClientID
    WHERE p.PaymentDate BETWEEN p_start_date AND p_end_date
    ORDER BY p.PaymentDate DESC;
END;
$$;



--- 4. Группа «Аналитика и Отчетность» ---
-- 4.1. Отчет по эффективности менеджеров --
CREATE OR REPLACE FUNCTION report_manager_performance(
    p_start_date DATE,
    p_end_date DATE
) RETURNS TABLE (
    ManagerName TEXT,
    ManagerPosition VARCHAR, -- ИЗМЕНЕНО: Было Position, стало ManagerPosition
    ToursSold INT,
    TotalRevenue DECIMAL
) 
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        (m.FirstName || ' ' || m.LastName) AS ManagerName,
        m.Position AS ManagerPosition, -- Явное сопоставление с выходным параметром
        COUNT(b.BookingID)::INT AS ToursSold,
        COALESCE(SUM(b.FinalPrice), 0) AS TotalRevenue
    FROM Managers m
    LEFT JOIN Bookings b ON m.ManagerID = b.ManagerID
    -- Учитываем только продажи в заданном периоде и исключаем отмены
    WHERE b.BookingDate BETWEEN p_start_date AND p_end_date
      AND b.Status != 'Аннулировано'
    GROUP BY m.ManagerID, m.FirstName, m.LastName, m.Position
    ORDER BY TotalRevenue DESC;
END;
$$;

--- 5. Группа «Системные триггеры» ---
-- 5.1. Универсальная триггерная функция аудита --
CREATE OR REPLACE FUNCTION system_audit_trigger()
RETURNS TRIGGER 
LANGUAGE plpgsql
AS $$
DECLARE
    v_record_id TEXT;
    v_old_data JSONB := NULL;
    v_new_data JSONB := NULL;
    v_changed_fields TEXT := '';
BEGIN
    -- Определение типа операции и данных
    IF (TG_OP = 'DELETE') THEN
        v_old_data := to_jsonb(OLD);
        -- Пытаемся найти ID удаленной записи в JSON-структуре
        v_record_id := COALESCE(v_old_data->>'bookingid', v_old_data->>'clientid', v_old_data->>'paymentid', 'N/A');
    ELSIF (TG_OP = 'INSERT') THEN
        v_new_data := to_jsonb(NEW);
        v_record_id := COALESCE(v_new_data->>'bookingid', v_new_data->>'clientid', v_new_data->>'paymentid', 'N/A');
    ELSE -- UPDATE
        v_old_data := to_jsonb(OLD);
        v_new_data := to_jsonb(NEW);
        v_record_id := COALESCE(v_new_data->>'bookingid', 'N/A');
        
        -- Вычисляем только те поля, которые реально изменились
        SELECT string_agg(key, ', ') INTO v_changed_fields
        FROM jsonb_each_text(v_old_data) AS o(key, value)
        JOIN jsonb_each_text(v_new_data) AS n(key, value) USING (key)
        WHERE o.value IS DISTINCT FROM n.value;
    END IF;

    -- Запись события в журнал
    INSERT INTO AuditLog (
        DBUser, OperationType, TableName, RecordID, 
        OldData, NewData, ChangedFields
    )
    VALUES (
        session_user,   -- Имя пользователя СУБД, выполнившего действие
        TG_OP,          -- INSERT / UPDATE / DELETE
        TG_TABLE_NAME,  -- Имя таблицы
        v_record_id,
        v_old_data,
        v_new_data,
        NULLIF(v_changed_fields, '')
    );

    IF (TG_OP = 'DELETE') THEN RETURN OLD; ELSE RETURN NEW; END IF;
END;
$$;

-- 5.2. Назначение триггеров--
-- Привязываем функцию аудита к ключевым таблицам
CREATE TRIGGER audit_clients_trigger
AFTER INSERT OR UPDATE OR DELETE ON Clients
FOR EACH ROW EXECUTE FUNCTION system_audit_trigger();

CREATE TRIGGER audit_bookings_trigger
AFTER INSERT OR UPDATE OR DELETE ON Bookings
FOR EACH ROW EXECUTE FUNCTION system_audit_trigger();

CREATE TRIGGER audit_payments_trigger
AFTER INSERT OR UPDATE OR DELETE ON Payments
FOR EACH ROW EXECUTE FUNCTION system_audit_trigger();