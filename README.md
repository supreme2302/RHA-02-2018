![travis](https://travis-ci.org/HimmelSpark/RHA-02-2018.svg?branch=master)
# Бэкэнд сервер для браузерной игры-стратегии "Bruteforce Balance"
## Деплой API <https://rha-backend.herokuapp.com>
## Команда RHA: 
- Адамян Петрос
- Бритиков Константин 
- Чувашов Егор
### Все данные в теле запроса должны быть а формате application/json
### Пользовательские данные пересылаются по следующему шаблону
```json
{
  "name": "vasya",
  "password": "qwertyui",
  "email": "pupkin@mail.ru"
}
```
#### Список команд, предоставляемых API:    
1. /users/create – регистрация пользователя. В теле POST запроса должны быть данные нового пользователя
2. /users/auth – авторизация пользователя. В теле POST запроса должны быть данные пользователя
    Причем ключ email обязателен, хоть и его значение не играет роли
3. /users/logout – деавторизация пользователя. POST запрос
4. /users/info – информация о текущем пользователе. GET запрос
5. /users/change – изменение информации о пользователе. В теле POST запроса должны быть новые данные пользователя

#### Коды состояний и причины их возникновения:
1. **SUCCESSFULLY_REGISTERED**
    + Возвращается при успешной регистрации нового пользователя
2. **SUCCESSFULLY_AUTHED**
    + Возвращается при успешной авторизации пользователя
3. **SUCCESSFULLY_LOGGED_OUT**
    + Возвращается при успешной деавторизации пользователя
4. **SUCCESSFULLY_CHANGED**
    + Возвращается при успешном изменении данных пользователя
5. **ACCESS_ERROR**
    + Возвращается при:
        + Запросе на деавторизацию неавторизоавнного пользователя
        + Запросе информации о пользователе текущей сессии от неавторизованного пользователя
        + Запросе на изменение пользовательских данных от неавторизованного пользователя
6. **WRONG_CREDENTIALS**
    + Возвращается при попытке авторизоваться с неправильными учетными данными (username либо password)
7. **NOT_UNIQUE_USERNAME**
    + Возвращается при попытке зарегистрировать пользователя с неуникальным никнеймом
8. **ALREADY_AUTHENTICATED**
    + Возвращается при:
        + Запросе на авторизацию от авторизованного пользователя
        + Запросе на регистрацию от авторизованного пользователя
9. **UNEXPECTED_ERROR**
    + Возвращается при возникновении непредвиденной ошибки
