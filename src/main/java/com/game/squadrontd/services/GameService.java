package com.game.squadrontd.services;

import com.game.squadrontd.models.*;
import com.game.squadrontd.repositories.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * GameService: Maneja las transacciones y las reglas de negocio principales.
 * La anotación @Service indica que es un componente de Spring.
 * La anotación @Transactional asegura que si algo falla a medias, la base de datos no se actualiza parcialmente.
 */
@Service
public class GameService {

    private final GameRepository gameRepository;
    private final CombatEngine combatEngine;

    // Inyección de dependencias por constructor. Spring se encarga de pasar las instancias.
    public GameService(GameRepository gameRepository, CombatEngine combatEngine) {
        this.gameRepository = gameRepository;
        this.combatEngine = combatEngine;
    }

    public Game createGame() {
        Game newGame = new Game();
        return gameRepository.save(newGame);
    }

    public Optional<Game> getGame(Long id) {
        return gameRepository.findById(id);
    }

    @Transactional
    public Game buyDefense(Long gameId, DefenseType type) {
        Game game = getGame(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada"));

        if (game.getStatus() != GameStatus.PREPARATION) {
            throw new IllegalStateException("Solo puedes comprar en la fase de preparación");
        }

        if (game.getGold() >= type.getCost()) {
            game.setGold(game.getGold() - type.getCost());
            DefensePlacement placement = new DefensePlacement(type, game);
            game.addDefense(placement);
            return gameRepository.save(game);
        } else {
            throw new IllegalArgumentException("Oro insuficiente");
        }
    }

    @Transactional
    public Game investEconomy(Long gameId) {
        Game game = getGame(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada"));

        int cost = 50; // Costo fijo por ahora para mejorar income
        if (game.getGold() >= cost) {
            game.setGold(game.getGold() - cost);
            game.setIncome(game.getIncome() + 5); // Sube 5 el ingreso garantizado
            return gameRepository.save(game);
        } else {
            throw new IllegalArgumentException("Oro insuficiente para mejorar economía");
        }
    }

    @Transactional
    public String startWave(Long gameId) {
        Game game = getGame(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada"));

        // Definimos la oleada (idealmente vendría de una base de datos o configuración YAML)
        WaveInfo currentWave = getWaveConfig(game.getCurrentWave());
        
        // Ejecutamos algoritmo de batalla
        String resultLog = combatEngine.resolveWave(game, currentWave);

        // Aplicamos el "Income" (Ingreso de la ronda)
        game.setGold(game.getGold() + game.getIncome());
        resultLog += "Income recibido: +" + game.getIncome() + " oro.\n";

        // Revisamos condición de victoria o derrota
        if (game.getBaseHealth() <= 0) {
            game.setStatus(GameStatus.GAME_OVER);
            resultLog += "¡GAME OVER! Has perdido toda la vida.\n";
        } else {
            game.setCurrentWave(game.getCurrentWave() + 1);
            if (game.getCurrentWave() > 5) {
                game.setStatus(GameStatus.VICTORY);
                resultLog += "¡VICTORIA! Has sobrevivido a todas las oleadas.\n";
            }
        }

        gameRepository.save(game);
        return resultLog;
    }

    private WaveInfo getWaveConfig(int waveNumber) {
        // Configuraciones hardcodeadas para MVP
        return switch (waveNumber) {
            case 1 -> new WaveInfo(1, "Duendes", 5, 20, 5, 2);
            case 2 -> new WaveInfo(2, "Orcos", 8, 40, 10, 3);
            case 3 -> new WaveInfo(3, "Trolls", 3, 100, 30, 10);
            case 4 -> new WaveInfo(4, "Gólems", 5, 200, 50, 20);
            default -> new WaveInfo(5, "Dragón Jefe", 1, 1000, 200, 100);
        };
    }
}
