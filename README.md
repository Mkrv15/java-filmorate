# java-filmorate
Template repository for Filmorate project.
Ссылка на диаграмму: https://dbdiagram.io/d/69c02b4afb2db18e3bd854e2

# Запросы для основных операций
## Запросы для users
- findAll
```SQL
SELECT *
FROM users;
```
- getUser
```SQL
SELECT *
FROM users
WHERE id = {id};
```
## Запросы для friends
- findFriends
```SQL
SELECT *
FROM users
WHERE {id} IN (
    SELECT from_user_id
    FROM friend_request
    WHERE status_id = 1
);
```
- findMutualFriends
```SQL
SELECT fr.to_user_id
FROM friend_request fr
WHERE fr.from_user_id = 1 
  AND fr.status_id = 1
  AND fr.to_user_id IN (
    SELECT to_user_id 
    FROM friend_request 
    WHERE from_user_id = 2 AND status_id = 1
  );

```
## Запросы для films
- findAll
```SQL
SELECT *
FROM films AS f
LEFT JOIN genres_film AS gf ON f.id = gf.id_film
LEFT JOIN genre AS g ON gf.id_genre = genre.id
```
- findPopularFilm
```SQL
SELECT f.name,
        COUNT(l.id_film) AS likes_count
FROM films AS f
LEFT JOIN likes AS l ON f.id = l.id_film
GROUP BY f.name, releaseDate
ORDER BY likes_count DESC
LIMIT 10;
```