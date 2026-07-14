# GPTsound

Open-source Android-приложение для музыкального discovery через официальный SoundCloud Public API. Интерфейс построен на Jetpack Compose: стеклянные панели, живой градиент, «Волна» похожих треков, локальные избранное и история, а также импортируемые JSON-темы.

> GPTsound — независимый проект и не является официальным приложением SoundCloud. Названия, авторы и ссылки на треки остаются привязаны к источнику. Воспроизведение работает через официальный SoundCloud Widget.

## Что уже работает

- Android 8.0+ (`minSdk 26`), Material 3 и Jetpack Compose.
- Поиск треков через `GET /tracks` с OAuth 2.1.
- «Волна» на базе `GET /tracks/{track_urn}/related` с URN, связанным paging и дополнительным разнообразием исполнителей.
- OAuth Authorization Code + PKCE и проверка `state`.
- Шифрование access token через Android Keystore; секреты не попадают в backup.
- Официальный SoundCloud Widget для воспроизведения и атрибуции.
- Демо-режим без ключа, чтобы сразу оценить интерфейс.
- Избранное, история и «чистая лента» для материалов с явными метками `#ad`/`sponsored`.
- Редактор дизайна, три пресета, экспорт и импорт темы JSON.
- Unit tests, Android Lint, CI-сборка и автоматический prerelease APK.

## Почему здесь нет обхода рекламы

Правила SoundCloud API запрещают удалять или изменять рекламу, доставляемую платформой. Поэтому GPTsound не содержит собственной рекламы и может фильтровать только треки с явными рекламными метками в открытых metadata. Он не вмешивается в аудиопоток или официальный плеер.

## Подключение SoundCloud API

Без API credentials приложение запускается в демонстрационном режиме.

1. Получите Client ID в [SoundCloud Apps](https://soundcloud.com/you/apps/).
2. Зарегистрируйте Redirect URI: `gptsound://oauth/callback`.
3. В GPTsound откройте **Дизайн → SoundCloud API**.
4. Введите Client ID и Redirect URI, сохраните и нажмите **Подключить SoundCloud**.
5. Завершите вход на официальной странице SoundCloud.

Client secret в Android-приложение добавлять нельзя. Для общедоступных production-сборок рекомендуется собственный backend/token broker и отдельная проверка условий SoundCloud API.

Актуальные источники: [API Guide](https://developers.soundcloud.com/docs/api/guide), [API Reference](https://developers.soundcloud.com/docs/api/reference), [API Terms](https://developers.soundcloud.com/docs/api/terms-of-use).

## Сборка

Нужны JDK 17, Android SDK 35 и Gradle 8.10.2.

```bash
gradle testDebugUnitTest lintDebug assembleDebug
```

APK появится в `app/build/outputs/apk/debug/app-debug.apk`. Проект также открывается напрямую в актуальной Android Studio.

## Свой дизайн

Тема — обычный JSON. Пример находится в [`themes/aurora.json`](themes/aurora.json), схема — в [`themes/theme.schema.json`](themes/theme.schema.json). JSON можно скопировать в буфер и импортировать прямо на экране «Дизайн».

## Архитектура

- `auth/` — OAuth 2.1 PKCE и обмен code на token.
- `data/` — API, локальные настройки, Keystore и repository.
- `domain/` — модели, theme codec, фильтр и ранжирование Волны.
- `ui/` — Compose-экраны, glass-компоненты и официальный widget player.

## Лицензия

MIT. Правила вклада — в [CONTRIBUTING.md](CONTRIBUTING.md).
