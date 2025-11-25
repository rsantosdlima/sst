package com.example.esocialapp.repository;

import com.example.esocialapp.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
}
