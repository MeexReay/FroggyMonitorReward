# FroggyMonitorReward
Плагин для награды игроков за голоса и отзывы на FroggyMonitor

Плагин запускает сайт,
на который будут приходить запросы от
FroggyMonitor на поощрение игрока,
плагин их читает и выполняет действие из конфига

[Скачать](https://github.com/MeexReay/FroggyMonitorReward/releases/latest)

## Конфиг
```yml
site_host: localhost   # IP адрес для сайта
site_port: 8080        # Порт для сайта
site_backlog: 5        # Максимальное кол-во подключений одновременно

secret_token: "ваш_секретный_токен" # Секретный токен с FroggyMonitor

comment_page: "/api/comment" # Страница для награды за отзыв
vote_page: "/api/vote"       # Страница для награды за голос

# Что указать в FroggyMonitor?
# В URL для поощрения за отзыв: 
#         http://{ip_сервера}:{site_port}{comment_page}  
#     ->  http://example.com:8080/api/comment
# В URL для поощрения за голос: 
#         http://{ip_сервера}:{site_port}{vote_page}     
#     ->  http://example.com:8080/api/vote
# Также возможно понадобится открыть порт на хосте

vote:                   # Награда за голос
  vault: 10                     # Выдать валюту (необяз.)
  item: "diamond 10"            # Выдать предмет (необяз.) (забрать предмет нельзя)
  message: "Спасибо за голос!"  # Отправить сообщение (необяз.)
  commands:                     # Исполнить команды (необяз.)
    - "/title {player_name} subtitle на FroggyMonitor"
    - "/title {player_name} title Спасибо за отзыв!"

add_comment:        # Награда за добавление отзыва
  vault: 10
  message: "Спасибо за отзыв!"

del_comment:        # Награда за удаление отзыва
  vault: -10                    # Снять валюту
```
