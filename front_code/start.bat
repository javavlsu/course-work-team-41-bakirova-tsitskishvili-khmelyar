@echo off

chcp 65001

echo Проверка наличия browser-sync...
where browser-sync >nul 2>&1

if %errorlevel% neq 0 (
    echo browser-sync не найден в PATH. Проверка глобальной установки...
    npm list -g browser-sync >nul 2>&1

    if %errorlevel% neq 0 (
        echo browser-sync не установлен. Установка...
        npm install -g browser-sync
    )

    else (
        echo browser-sync установлен глобально, но не найден в PATH.
        echo Пожалуйста, проверьте вашу переменную окружения PATH.
        pause
        exit /b
    )

)

else (
    echo browser-sync уже установлен и доступен в PATH.
)

echo Запуск...
browser-sync --server --files "**/*"