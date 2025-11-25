package com.example.esocialapp.repository;

import com.example.esocialapp.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
}
