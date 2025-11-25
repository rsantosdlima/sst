package com.example.esocialapp.controller;

import com.example.esocialapp.model.Evento_S2230;
import com.example.esocialapp.repository.Evento_S2230Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos-s2230")
public class Evento_S2230Controller {

    @Autowired
    private Evento_S2230Repository repository;

    @GetMapping
    public List<Evento_S2230> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Evento_S2230 create(@RequestBody Evento_S2230 evento) {
        return repository.save(evento);
    }
}
