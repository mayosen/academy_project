# Веб-сервис для хранения файлов
Тестовое задание в Школу бэкенд-разработки Академии Яндекса (осень 2022).

# Задание
В данном задании предлагается реализовать бэкенд для веб-сервиса хранения файлов, аналогичный сервису Яндекс Диск. 
Обычно взаимодействие с такими сервисами происходит следующим образом:
1. Пользователь загружает и структурирует файлы в предложенном ему облачном пространстве. 
2. Пользователь может скачивать файлы и фиксировать историю их изменений.
Ваша задача - разработать REST API сервис, который позволяет пользователям загружать и обновлять информацию о файлах и папках.

[Cпецификация OpenAPI](/src/main/resources/openapi.yaml), которую нужно реализовать.  
Открыть можно в онлайн-редакторе [Swagger Editor](https://editor.swagger.io/).

# Технологии
- Java 17
- Gradle 7.5
- Spring Boot 2.7
- PostgreSQL 14.2
- Docker 20.10

# Запуск
## Локально
1. Установить переменные окружения.
```bash
$ export \
  DB_URL=postgresql://host:port/database \
  DB_USERNAME=username \
  DB_PASSWORD=password \
  SPRING_PROFILES_ACTIVE=dev # При разработке
```

2. Запустить проект.
```bash
$ gradle clean bootRun
```

## Docker Compose
1. Установить переменные окружения или использовать `.env` файл ([пример](/.env.example)).

2. Запустить контейнер.
```bash
$ docker-compose up -d
```
