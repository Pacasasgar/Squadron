package com.game.squadrontd.repositories;

import com.game.squadrontd.models.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Los repositorios de Spring Data JPA son interfaces mágicas. 
 * Al extender JpaRepository, le decimos a Spring que maneje la entidad Game.
 * Spring crea automáticamente la implementación de esta interfaz en tiempo de ejecución,
 * proporcionando métodos como save(), findById(), findAll(), delete(), etc., sin
 * que tengamos que escribir una sola línea de código SQL.
 */
@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
}
