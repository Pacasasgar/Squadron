package com.game.squadrontd.controllers;

import com.game.squadrontd.models.DefenseType;
import com.game.squadrontd.models.Game;
import com.game.squadrontd.services.GameService;
import org.springframework.web.bind.annotation.*;

/**
 * GameController: Nuestra API REST.
 * La anotación @RestController le dice a Spring que esta clase manejará peticiones web (HTTP)
 * y que automáticamente convertirá las respuestas en formato JSON.
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    // POST /api/games -> Crea una partida nueva
    @PostMapping
    public Game createGame() {
        return gameService.createGame();
    }

    // GET /api/games/{id} -> Devuelve el estado de la partida
    @GetMapping("/{id}")
    public Game getGame(@PathVariable Long id) {
        return gameService.getGame(id)
                .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada"));
    }

    // POST /api/games/{id}/defenses?type=ARCHER -> Compra una defensa
    @PostMapping("/{id}/defenses")
    public Game buyDefense(@PathVariable Long id, @RequestParam DefenseType type) {
        return gameService.buyDefense(id, type);
    }

    // POST /api/games/{id}/economy -> Mejora el income
    @PostMapping("/{id}/economy")
    public Game investEconomy(@PathVariable Long id) {
        return gameService.investEconomy(id);
    }

    @PostMapping("/{id}/start-wave")
    public com.game.squadrontd.models.BattleReplay startWave(@PathVariable Long id) {
        return gameService.startWave(id);
    }
}
