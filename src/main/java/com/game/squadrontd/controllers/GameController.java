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

    // GET /api/games/{id}/next-wave -> Devuelve la info de la siguiente oleada
    @GetMapping("/{id}/next-wave")
    public com.game.squadrontd.models.WaveInfo getNextWave(@PathVariable Long id) {
        Game game = getGame(id);
        return gameService.getWaveConfig(game.getCurrentWave());
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

    // POST /api/games/{id}/extraction -> Mejora la extraccion del monolito
    @PostMapping("/{id}/extraction")
    public Game upgradeExtraction(@PathVariable Long id) {
        return gameService.upgradeExtraction(id);
    }

    @PostMapping("/{id}/start-wave")
    public com.game.squadrontd.models.BattleReplay startWave(@PathVariable Long id) {
        return gameService.startWave(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public org.springframework.http.ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public org.springframework.http.ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
        return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
    }
}
