-- Модуль «Авторизация и Безопасность»
-- 1.1. Аутентификация клиента (Возвращает ID или -1)
SELECT check_credentials($1, $2) as client_id;

-- 1.2. Регистрация нового туриста
-- Параметры: Имя, Фамилия, Телефон, Email, Хеш пароля, Паспорт
CALL register_client($1, $2, $3, $4, $5, $6);

-- 1.3. Получение данных текущего сотрудника (по логину БД)
SELECT ManagerID, FirstName, LastName, Position, HireDate 
FROM Managers 
WHERE DB_Login = current_user;

-- 1.4. Загрузка профиля клиента (для Личного кабинета)
SELECT FirstName, LastName, Phone, PassportData, RegistrationDate 
FROM Clients 
WHERE ClientID = $1;



-- Модуль «Управление Турами»
-- 2.1. Поиск туров с фильтрацией
-- Параметры: Часть названия страны, Макс. цена, Дата начала
SELECT t.TourID, t.Destination, t.StartDate, t.EndDate, t.BaseCost, 
       tt.TypeName, p.Name AS PartnerName 
FROM Tours t 
JOIN TourTypes tt ON t.TourTypeID = tt.TourTypeID 
JOIN Partners p ON t.PartnerID = p.PartnerID 
WHERE t.IsActive = TRUE 
  AND t.Destination ILIKE '%' || $1 || '%' 
  AND t.BaseCost <= $2 
  AND t.StartDate >= $3 
ORDER BY t.StartDate;

-- 2.2. Получение детальной информации о туре
SELECT t.*, tt.TypeName, tt.Description as TypeDesc, 
       p.Name as PartnerName, p.Contacts as PartnerContacts 
FROM Tours t 
JOIN TourTypes tt ON t.TourTypeID = tt.TourTypeID 
JOIN Partners p ON t.PartnerID = p.PartnerID 
WHERE t.TourID = $1;

-- 2.3. Загрузка списка отелей для тура
SELECT AccommodationID, Type, Description, Cost 
FROM Accommodations 
WHERE TourID = $1 
ORDER BY Cost;

-- 2.4. Добавление нового тура (через функцию)
SELECT create_tour($1, $2, $3, $4, $5, $6, $7, $8);

-- 2.5. Архивация (скрытие) тура
UPDATE Tours SET IsActive = FALSE WHERE TourID = $1;

-- 2.6. Актуализация базовой цены тура
UPDATE Tours SET BaseCost = $2 WHERE TourID = $1;



-- «Работа с Клиентами»
-- 3.1. Быстрый поиск клиента (по имени или телефону)
SELECT ClientID, FirstName, LastName, Phone, Email 
FROM Clients 
WHERE (FirstName || ' ' || LastName) ILIKE '%' || $1 || '%' 
   OR Phone LIKE '%' || $1 || '%' 
LIMIT 50;

-- 3.2. Получение полной анкеты клиента
SELECT * FROM Clients WHERE ClientID = $1;

-- 3.3. Обновление профиля клиента
UPDATE Clients 
SET FirstName = $2, LastName = $3, Phone = $4, PassportData = $5 
WHERE ClientID = $1;

-- 3.4. История путешествий клиента
SELECT b.BookingID, t.Destination, t.StartDate, b.Status, b.FinalPrice, b.PaymentStatus 
FROM Bookings b 
JOIN Tours t ON b.TourID = t.TourID 
WHERE b.ClientID = $1 
ORDER BY b.BookingDate DESC;



-- Модуль «Бронирование»
-- 4.1. Создание новой заявки (Возвращает ID брони)
SELECT create_booking($1, $2, $3);

-- 4.2. Рабочий стол менеджера (Активные заявки)
-- Параметр: ID менеджера
SELECT b.BookingID, c.FirstName || ' ' || c.LastName AS ClientName, 
       t.Destination, b.BookingDate, b.Status, b.PaymentStatus, b.FinalPrice 
FROM Bookings b 
JOIN Clients c ON b.ClientID = c.ClientID 
JOIN Tours t ON b.TourID = t.TourID 
WHERE b.ManagerID = $1 
  AND b.Status IN ('В обработке', 'Подтверждено') 
ORDER BY b.BookingDate DESC;

-- 4.3. Изменение статуса бронирования (Подтверждение/Отмена)
UPDATE Bookings SET Status = $2 WHERE BookingID = $1;

-- 4.4. Получение полных данных для Договора
SELECT b.*, c.PassportData, c.FirstName, c.LastName, 
       t.Destination, t.StartDate, t.EndDate, m.LastName as ManagerName 
FROM Bookings b 
JOIN Clients c ON b.ClientID = c.ClientID 
JOIN Tours t ON b.TourID = t.TourID 
LEFT JOIN Managers m ON b.ManagerID = m.ManagerID 
WHERE b.BookingID = $1;

-- 4.5. Просмотр "Мои туры" для клиента (через функцию)
SELECT * FROM get_my_tours($1);


-- 5. Модуль «Финансы»
-- 5.1. Реестр должников (Неоплаченные брони)
SELECT b.BookingID, c.FirstName || ' ' || c.LastName AS ClientName, 
       t.Destination, b.FinalPrice, 
       (SELECT COALESCE(SUM(Amount), 0) FROM Payments p WHERE p.BookingID = b.BookingID) AS PaidAmount, 
       b.FinalPrice - (SELECT COALESCE(SUM(Amount), 0) FROM Payments p WHERE p.BookingID = b.BookingID) AS Debt 
FROM Bookings b 
JOIN Clients c ON b.ClientID = c.ClientID 
JOIN Tours t ON b.TourID = t.TourID 
WHERE b.PaymentStatus IN ('Не оплачено', 'Частично') 
  AND b.Status != 'Аннулировано' 
ORDER BY b.BookingDate;

-- 5.2. Регистрация платежа (через процедуру)
-- Автоматически обновляет статус оплаты брони
CALL add_payment($1, $2, $3, $4);

-- 5.3. История платежей по конкретной заявке
SELECT PaymentID, PaymentDate, Amount, PaymentMethod, TransactionInfo 
FROM Payments 
WHERE BookingID = $1 
ORDER BY PaymentDate DESC;



-- Модуль «Администрирование и Справочники»
-- 6.1. Получение списка типов туров
SELECT TourTypeID, TypeName FROM TourTypes ORDER BY TypeName;

-- 6.2. Получение списка партнеров (фильтр по типу)
SELECT PartnerID, Name FROM Partners WHERE PartnerType = $1 ORDER BY Name;

-- 6.3. Регистрация нового сотрудника
INSERT INTO Managers (FirstName, LastName, Phone, Email, Position, HireDate, DB_Login) 
VALUES ($1, $2, $3, $4, $5, $6, $7);

-- 6.4. Просмотр журнала аудита (Логов)
-- Параметры: Дата начала, Дата конца
SELECT LogID, EventDate, DBUser, OperationType, TableName, ChangeDetails 
FROM AuditLog 
WHERE EventDate BETWEEN $1 AND $2 
ORDER BY EventDate DESC 
LIMIT 100;



-- Модуль «Аналитика»
-- 7.1. Отчет KPI менеджеров (через функцию)
-- Параметры: Дата начала периода, Дата конца
SELECT * FROM report_manager_performance($1, $2);

-- 7.2. Динамика выручки по месяцам (за текущий год)
SELECT TO_CHAR(PaymentDate, 'YYYY-MM') AS Month, 
       SUM(Amount) AS TotalRevenue, 
       COUNT(PaymentID) AS TransactionsCount 
FROM Payments 
WHERE PaymentDate >= DATE_TRUNC('year', CURRENT_DATE) 
GROUP BY TO_CHAR(PaymentDate, 'YYYY-MM') 
ORDER BY Month;

-- 7.3. ТОП-5 популярных направлений
SELECT t.Destination, COUNT(b.BookingID) as SalesCount 
FROM Bookings b 
JOIN Tours t ON b.TourID = t.TourID 
WHERE b.Status = 'Завершено' 
GROUP BY t.Destination 
ORDER BY SalesCount DESC 
LIMIT 5;
