# Intershop
Витрина интернет-магазина с веб-интерфейсом и REST API

### Структура проекта
- `main-app` - основное приложение интернет-магазина
- `payment-service` - микросервис для обработки платежей

### Локальный запуск

### Запуск main-app
1. Соберите проект с помощью Maven:
   ```bash
   mvn clean install
   ```

2. Запустите главный класс приложения:  
   `ru.yandex.practicum.IntershopApplication` (через IDE)

   Или через Maven:
   ```bash
   cd main-app
   mvn spring-boot:run
   ```

### Запуск payment-service
1. В отдельном терминале запустите сервис платежей:
   ```bash
   cd payment-service
   mvn spring-boot:run
   ```

### Запуск в Docker

1. Убедитесь, что Docker запущен
2. Выполните команду:
   ```bash
   docker compose up
   ```

После запуска приложение будет доступно по адресу:
http://localhost:8080/main/items

### Важные URLs
- Интернет-магазин: http://localhost:8080/main/items
- Корзина: http://localhost:8080/cart/items
- Заказы: http://localhost:8080/orders
- API платежного сервиса: http://localhost:8081/swagger-ui-custom.html

Студент Шестаков А.В., 4-ая когорта, курс "Мидл Java-разработчик".