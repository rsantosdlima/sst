package com.example.esocialapp.controller;

import com.example.esocialapp.model.Evento_S2240;
import com.example.esocialapp.repository.Evento_S2240Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos-s2240")
public class Evento_S2240Controller {

    @Autowired
    private Evento_S2240Repository repository;

    @GetMapping
    public List<Evento_S2240> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Evento_S2240 create(@RequestBody Evento_S2240 evento) {
        return repository.save(evento);
    }
}
