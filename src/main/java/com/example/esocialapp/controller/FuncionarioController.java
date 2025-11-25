package com.example.esocialapp.controller;

import com.example.esocialapp.model.Funcionario;
import com.example.esocialapp.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    @Autowired
    private FuncionarioRepository repository;

    @GetMapping
    public List<Funcionario> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Funcionario create(@RequestBody Funcionario funcionario) {
        return repository.save(funcionario);
    }
}
