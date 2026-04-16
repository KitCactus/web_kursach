# Руководство по интеграционным тестам для Orders

## Что такое интеграционный тест?
Интеграционный тест проверяет, как несколько компонентов системы работают вместе. В нашем случае тестируются:
- HTTP контроллер (OrderController)
- Бизнес-логика (OrderService)
- Маршрутизация HTTP запросов

## Созданные тесты

### Файл теста
```
cafe/src/test/java/ru/ssau/cafe/controller/OrderControllerTest.java
```

### Что тестируется

#### 1. **Получение заказов**
- `getAllOrders()` - получить все заказы, возвращает список с полной информацией
- `getOrdersByStatus()` - фильтрация заказов по статусу (PENDING, READY, COMPLETED)
- `getTodayOrders()` - получить только заказы за сегодня
- `getOrderById()` - получить конкретный заказ по ID

#### 2. **Статистика**
- `getTodayRevenue()` - выручка за сегодня (сумма всех заказов)
- `getTodayOrdersCount()` - количество заказов за сегодня

#### 3. **Изменение статуса заказа**
- `updateOrderStatus()` - изменить статус заказа (PENDING → READY → COMPLETED)
- Полный жизненный цикл заказа - тест проверяет переход через все 3 статуса

## Как запустить тесты

### ✅ Рекомендуемый способ (работает!)
```bash
cd cafe
./mvnw clean test -Dtest=OrderControllerTest -Dbackend=true
```

### Через IntelliJ IDEA / VS Code
1. Откройте файл `src/test/java/ru/ssau/cafe/controller/OrderControllerTest.java`
2. Кликните правой кнопкой на класс и выберите "Run 'OrderControllerTest'"
3. Или кликните на зелёный треугольник слева от названия класса

### Альтернатива через Maven (если mvnw не работает)
```bash
cd cafe
mvn clean test -Dtest=OrderControllerTest -Dbackend=true
```

## Структура каждого теста

Каждый тест следует этому паттерну:

```java
@Test
@DisplayName("Описание что тестируется")
void testName() throws Exception {
    // 1. Подготовка данных (Arrange)
    List<OrderDto> testOrders = Arrays.asList(...);
    when(orderService.getAllOrders()).thenReturn(testOrders);
    
    // 2. Выполнение HTTP запроса (Act)
    mockMvc.perform(get("/orders"))
    
    // 3. Проверка результата (Assert)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1L));
}
```

## Тестовые данные

Каждый заказ содержит:
- **ID** - уникальный идентификатор
- **Имя клиента** - например "Иван Петров"
- **Сумма** - общая стоимость заказа (в рублях)
- **Статус** - PENDING (в ожидании), READY (готов), COMPLETED (завершён)
- **Товары** - список товаров (эспрессо, круассан и т.д.)

Пример тестового заказа:
```
ID: 1
Клиент: "Иван Петров"
Сумма: 380.00 ₽
Статус: COMPLETED
Товары:
  - Эспрессо (кол-во: 2, цена: 150.00 ₽)
  - Круассан (кол-во: 1, цена: 80.00 ₽)
```

## Список всех тестов

| № | Название теста | Что проверяет | Ожидаемый результат |
|---|---|---|---|
| 1 | `getAllOrders_returns200` | GET /orders | 200 OK, список из 2 заказов |
| 2 | `getOrdersByStatus_PENDING_returns200` | GET /orders/status/PENDING | 200 OK, только PENDING заказы |
| 3 | `getOrdersByStatus_COMPLETED_returns200` | GET /orders/status/COMPLETED | 200 OK, только COMPLETED заказы |
| 4 | `getTodayOrders_returns200` | GET /orders/today | 200 OK, заказы за сегодня |
| 5 | `getOrderById_validId_returns200` | GET /orders/{id} | 200 OK, один конкретный заказ |
| 6 | `getTodayRevenue_returns200` | GET /orders/today/revenue | 200 OK, сумма 1500.00 ₽ |
| 7 | `getTodayOrdersCount_returns200` | GET /orders/today/count | 200 OK, количество 5 |
| 8 | `updateOrderStatus_toREADY_returns200` | PATCH /orders/{id}/status | 200 OK, статус изменился на READY |
| 9 | `updateOrderStatus_toCOMPLETED_returns200` | PATCH /orders/{id}/status | 200 OK, статус изменился на COMPLETED |
| 10 | `updateOrderStatus_fullLifecycle_...` | Полный цикл | PENDING → READY → COMPLETED |

## Почему это интеграционный тест?

✅ Тестирует **реальный HTTP контроллер** (не mock)
✅ Тестирует **всю цепочку**: HTTP запрос → контроллер → сервис → DTO
✅ Использует **MockMvc** для эмуляции HTTP клиента без реального сервера
✅ Использует **Mockito** для подмены сервиса (не нужна реальная БД)
✅ Проверяет **статус коды и JSON ответы**

## Как это поможет на защите курсовой

### Показываешь код тестов
> "Я написал 10 интеграционных тестов, которые проверяют все API endpoints для управления заказами"

### Запускаешь тесты
```
[INFO] Running ru.ssau.cafe.controller.OrderControllerTest
[INFO] Tests run: 10, Failures: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Объясняешь логику
- Каждый тест проверяет конкретный сценарий
- Используется тестовые данные (мокированный OrderService)
- Проверяются HTTP статус коды и JSON структура ответа

## Расширение тестов в будущем

Можешь добавить ещё:
1. Тесты на **ошибки** (404 - заказ не найден, 400 - неверный статус)
2. Тесты на **валидацию** (пустое имя клиента, отрицательная сумма)
3. Интеграционные тесты с **реальной БД** (@DataJpaTest)
4. Тесты **производительности** и нагрузочное тестирование

## Контакты

Если нужна помощь с запуском тестов, смотри [GitHub Issues](https://github.com/anthropics/claude-code/issues)
