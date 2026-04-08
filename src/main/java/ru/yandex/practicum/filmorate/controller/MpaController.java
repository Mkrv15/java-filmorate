package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.enums.MPA;
import ru.yandex.practicum.filmorate.storage.mpa.MpaRatingDbStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final MpaRatingDbStorage storage;

    @GetMapping
    public List<MPA> getAllMpa(){
        return storage.getAllMpa();
    }

    @GetMapping("/{id}")
    public MPA getMpaById(@PathVariable int id){
        return storage.getMpaById(id);
    }
}
