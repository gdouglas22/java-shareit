package ru.practicum.shareit.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// добавил для констант заголовковы
// используется в контроллерах для получения id пользователя из заголовков HTTP запросов
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeaderConstants {
    public static final String USER_ID = "X-Sharer-User-Id";
}
