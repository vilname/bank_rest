# Bank REST

Backend-сервис на Spring Boot для управления банковскими картами:
- аутентификация по JWT;
- роли `ADMIN` и `USER`;
- просмотр карт и баланса;
- переводы между своими картами;
- заявка на блокировку карты пользователем и обработка заявки администратором;
- миграции БД через Liquibase.

## Технологии

- Java 17
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Liquibase
- OpenAPI (Swagger)
- Docker Compose

## Подготовка

1. Убедиться, что установлены Docker и Docker Compose.
2. Скопировать `.env.example` в `.env` и заполнить значения.

## Переменные окружения

Минимально необходимые:
- `APP_JWT_SECRET` — секрет для подписи JWT (обязателен)
- `APP_PAN_SECRET` — 64 hex-символа для шифрования PAN
- `APP_CORS_ALLOWED_ORIGINS` — разрешенные origin (через запятую)

Часто используемые:
- `APP_PORT` (по умолчанию `8080`)
- `DB_PORT` (по умолчанию `5435`)
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`

Пример находится в `.env.example`.

## Запуск в Docker

Запуск приложения и базы данных:

```bash
docker compose up --build
```

Приложение будет доступно на `http://localhost:8080`.

## Тесты

Запуск тестов в контейнере:

```bash
docker compose run --rm test
```

## API и документация

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI YAML: `docs/openapi.yaml`

### Основные пользовательские endpoints

- `POST /api/auth/register` — регистрация
- `POST /api/auth/login` — вход
- `GET /api/cards` — список карт пользователя
- `GET /api/cards/{cardId}/balance` — баланс карты
- `POST /api/cards/block` — создать заявку на блокировку
- `POST /api/transfers` — перевод между своими картами
- `GET /api/transfers` — история переводов

### Основные админские endpoints

- `GET /admin/cards` — список карт
- `POST /admin/cards` — создание карты
- `POST /admin/cards/{cardId}/block` — блокировка карты
- `POST /admin/cards/{cardId}/activate` — активация карты
- `GET /admin/cards/block-requests` — список заявок на блокировку
- `POST /admin/cards/block-requests/{requestId}/approve` — одобрить заявку
- `POST /admin/cards/block-requests/{requestId}/reject` — отклонить заявку

## Примечания по безопасности

- Полный номер карты в БД не хранится в открытом виде.
- Используются `pan_cipher`, `pan_hmac`, `pan_last_four`.
- Маскирование номера в ответах API: `**** **** **** 1234`.

## Примечание

- логику метода `CardHelper.markExpiredIfNeeded(card)` конечно лучше вынести в фоновую задачу, но пока так
