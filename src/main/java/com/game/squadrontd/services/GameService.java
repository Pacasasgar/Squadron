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
    public Game upgradeExtraction(Long gameId) {
        Game game = getGame(gameId).orElseThrow(() -> new IllegalArgumentException("Partida no encontrada"));
        if (game.getStatus() != GameStatus.PREPARATION) {
            throw new IllegalStateException("Solo puedes mejorar la extracción en la fase de preparación");
        }
        int cost = 50;
        if (game.getGold() >= cost) {
            game.setGold(game.getGold() - cost);
            game.setExtractionLevel(game.getExtractionLevel() + 1);
            return gameRepository.save(game);
        } else {
            throw new IllegalArgumentException("Oro insuficiente para mejorar extracción");
        }
    }

    @Transactional
    public Game investEconomy(Long gameId) {
        Game game = getGame(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada"));

        if (game.getStatus() == GameStatus.GAME_OVER || game.getStatus() == GameStatus.VICTORY) {
            throw new IllegalStateException("La partida ya ha terminado");
        }

        int cost = 1; // 1 Arcanium = 1 Income
        if (game.getArcanium() >= cost) {
            game.setArcanium(game.getArcanium() - cost);
            game.setIncome(game.getIncome() + 1);
            return gameRepository.save(game);
        } else {
            throw new IllegalArgumentException("Arcanium insuficiente para invertir en economía");
        }
    }

    @Transactional
    public BattleReplay startWave(Long gameId) {
        Game game = getGame(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada"));

        if (game.getStatus() == GameStatus.GAME_OVER || game.getStatus() == GameStatus.VICTORY) {
            throw new IllegalStateException("La partida ya ha terminado");
        }

        WaveInfo currentWave = getWaveConfig(game.getCurrentWave());
        
        int initialBaseHealth = game.getBaseHealth();
        
        BattleReplay replay = combatEngine.resolveWave(game, currentWave);

        // Recolectar Arcanium
        int earnedArcanium = game.getExtractionLevel() * 1;
        game.setArcanium(game.getArcanium() + earnedArcanium);
        replay.setEarnedArcanium(earnedArcanium);
        
        if (earnedArcanium > 0) {
            replay.setTextLog(replay.getTextLog() + "El Monolito ha extraído: +" + earnedArcanium + " Arcanium.\n");
        }

        // Flawless Bonus Logic
        if (game.getBaseHealth() == initialBaseHealth && game.getBaseHealth() > 0) {
            game.setGold(game.getGold() + currentWave.getFlawlessBonus());
            replay.setTextLog(replay.getTextLog() + "<span class=\"reward\">¡BONUS DE OLEADA PERFECTA! Tus tropas saquean el campo de batalla: +" + currentWave.getFlawlessBonus() + " oro.</span>\n");
        }

        game.setGold(game.getGold() + game.getIncome());
        replay.setTextLog(replay.getTextLog() + "Income de Oro: +" + game.getIncome() + ".\n");

        if (game.getBaseHealth() <= 0) {
            game.setStatus(GameStatus.GAME_OVER);
            replay.setGameOver(true);
            replay.setTextLog(replay.getTextLog() + "¡GAME OVER! El Monolito ha colapsado.\n");
        } else {
            game.setCurrentWave(game.getCurrentWave() + 1);
            if (game.getCurrentWave() > 5) {
                game.setStatus(GameStatus.VICTORY);
                replay.setVictory(true);
                replay.setTextLog(replay.getTextLog() + "¡VICTORIA! Has sobrevivido a todas las oleadas.\n");
            }
        }

        gameRepository.save(game);
        return replay;
    }

    public WaveInfo getWaveConfig(int waveNumber) {
        // Configuraciones hardcodeadas para MVP (añadido FlawlessBonus)
        return switch (waveNumber) {
            case 1 -> new WaveInfo(1, "Duendes", 5, 20, 5, DamageType.PHYSICAL, ArmorType.LIGHT, 3, 10);
            case 2 -> new WaveInfo(2, "Orcos", 8, 40, 10, DamageType.PHYSICAL, ArmorType.LIGHT, 4, 20);
            case 3 -> new WaveInfo(3, "Trolls", 3, 100, 30, DamageType.PHYSICAL, ArmorType.HEAVY, 15, 30);
            case 4 -> new WaveInfo(4, "Espectros", 5, 80, 50, DamageType.MAGIC, ArmorType.LIGHT, 25, 40);
            default -> new WaveInfo(5, "Dragón Jefe", 1, 1000, 200, DamageType.MAGIC, ArmorType.HEAVY, 100, 50);
        };
    }
}
