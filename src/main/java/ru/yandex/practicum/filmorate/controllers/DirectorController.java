package ru.yandex.practicum.filmorate.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.director.DirectorService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/directors")
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    public List<Director> getDirectors() {
        log.info("Получен GET-запрос к эндпоинту: '/directors' на получение всех режиссеров");
        return directorService.getDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable Long id) {
        log.info("Получен GET-запрос к эндпоинту: '/directors' на получение режиссера с ID={}", id);
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public Director create(@Valid @RequestBody Director director) {
        log.info("Получен POST-запрос к эндпоинту: '/directors' на добавление режиссера");
        return directorService.create(director);
    }

    @PutMapping
    public Director update(@RequestBody Director director) {
        log.info("Получен PUT-запрос к эндпоинту: '/directors' на изменение данных о режиссере");
        return directorService.update(director);
    }

    @DeleteMapping("/{id}")
    public Director delete(@PathVariable Long id) {
        log.info("Получен DELETE-запрос к эндпоинту: '/directors' на удаление режиссера с ID={}", id);
        return directorService.delete(id);
    }
}
