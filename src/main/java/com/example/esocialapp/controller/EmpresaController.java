package com.example.esocialapp.controller;

import com.example.esocialapp.model.Empresa;
import com.example.esocialapp.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    @Autowired
    private EmpresaRepository repository;

    @GetMapping
    public List<Empresa> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Empresa create(@RequestBody Empresa empresa) {
        return repository.save(empresa);
    }
}
