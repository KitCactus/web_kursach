#!/bin/bash

# Скрипт для запуска интеграционных тестов без сборки фронтенда

cd "$(dirname "$0")/cafe"

echo "🧪 Запуск интеграционных тестов для Orders..."
echo ""

# Способ 1: Через Maven, но скипим npm (если это возможно)
if command -v mvn &> /dev/null; then
    mvn clean test -Dtest=OrderControllerTest -Dmaven.skip.npm=true
else
    # Способ 2: Через mvnw
    ./mvnw clean test -Dtest=OrderControllerTest
fi

echo ""
echo "✅ Тесты завершены!"
