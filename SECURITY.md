# Security policy

Сообщайте об уязвимостях через приватный Security Advisory репозитория.

Никогда не публикуйте SoundCloud `client_secret`, access token, refresh token, signing keystore или пользовательские cookie в issue, commit или pull request. GPTsound хранит access token в Android Keystore и исключает его из резервной копии.

Поддерживаемая версия: `0.1.x`.
