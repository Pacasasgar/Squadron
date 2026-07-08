package com.game.squadrontd.services;

import com.game.squadrontd.models.DefensePlacement;
import com.game.squadrontd.models.Game;
import com.game.squadrontd.models.WaveInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * CombatEngine: Lógica pura de cálculo (Algoritmo de Batalla).
 * Extraemos esto fuera del GameService para mantener la "Single Responsibility".
 * Aquí simulamos la batalla entre las unidades defensivas del jugador y los enemigos de la oleada.
 */
@Service
public class CombatEngine {

    // Clase interna temporal para simular combate táctico
    private static class CombatUnit {
        String name;
        int hp;
        int attack;

        CombatUnit(String name, int hp, int attack) {
            this.name = name;
            this.hp = hp;
            this.attack = attack;
        }
    }

    public String resolveWave(Game game, WaveInfo wave) {
        StringBuilder log = new StringBuilder();
        log.append("--- INICIO DE OLEADA ").append(wave.getWaveNumber()).append(" ---\n");

        // 1. Fase de Setup: Instanciar unidades
        List<CombatUnit> defenses = new ArrayList<>();
        for (DefensePlacement dp : game.getDefenses()) {
            defenses.add(new CombatUnit(dp.getType().name(), dp.getType().getHp(), dp.getType().getAttack()));
        }

        List<CombatUnit> enemies = new ArrayList<>();
        for (int i = 0; i < wave.getEnemyCount(); i++) {
            enemies.add(new CombatUnit(wave.getEnemyName(), wave.getEnemyHp(), wave.getEnemyAttack()));
        }

        log.append("Fuerzas aliadas: ").append(defenses.size()).append(" unidades.\n");
        log.append("Fuerzas enemigas: ").append(enemies.size()).append(" ").append(wave.getEnemyName()).append(".\n");

        if (defenses.isEmpty()) {
            log.append("No tienes defensas. Los enemigos avanzan directamente hacia la base.\n");
        }

        // 2. Bucle de Combate (por rondas)
        int round = 1;
        while (!defenses.isEmpty() && !enemies.isEmpty()) {
            int enemiesKilledThisRound = 0;
            int defensesKilledThisRound = 0;
            int enemyDamageTaken = 0;
            int defenseDamageTaken = 0;

            // Turno Aliado: Defensas atacan a los enemigos (Focus fire frontal)
            for (CombatUnit defense : defenses) {
                if (enemies.isEmpty()) break; // No quedan enemigos
                CombatUnit targetEnemy = enemies.get(0);
                
                targetEnemy.hp -= defense.attack;
                enemyDamageTaken += defense.attack;

                if (targetEnemy.hp <= 0) {
                    enemies.remove(0);
                    enemiesKilledThisRound++;
                }
            }

            // Turno Enemigo: Enemigos vivos atacan a las defensas (Focus fire frontal)
            for (CombatUnit enemy : enemies) {
                if (defenses.isEmpty()) break; // No quedan defensas
                CombatUnit targetDefense = defenses.get(0);

                targetDefense.hp -= enemy.attack;
                defenseDamageTaken += enemy.attack;

                if (targetDefense.hp <= 0) {
                    defenses.remove(0);
                    defensesKilledThisRound++;
                }
            }

            // Log de la ronda
            log.append("> Ronda ").append(round).append(" | ")
               .append("Aliados infligen ").append(enemyDamageTaken).append(" daño (").append(enemiesKilledThisRound).append(" bajas). ");
            
            if (!enemies.isEmpty()) {
                log.append("Enemigos infligen ").append(defenseDamageTaken).append(" daño (").append(defensesKilledThisRound).append(" bajas).\n");
            } else {
                log.append("\n");
            }

            round++;
        }

        // 3. Resolución final
        if (enemies.isEmpty()) {
            log.append("¡Victoria! Tus defensas han aniquilado la oleada.\n");
            int reward = wave.getEnemyCount() * wave.getGoldRewardPerEnemy();
            game.setGold(game.getGold() + reward);
            log.append("Recompensa: +").append(reward).append(" oro.\n");
        } else {
            // Defensas aniquiladas, enemigos sobrantes atacan la base 1 sola vez
            int remainingEnemyDamage = enemies.stream().mapToInt(e -> e.attack).sum();
            game.setBaseHealth(Math.max(0, game.getBaseHealth() - remainingEnemyDamage));
            log.append("¡Tus defensas fueron destruidas! ").append(enemies.size()).append(" enemigos sobrevivientes golpean tu base por ").append(remainingEnemyDamage).append(" de daño.\n");

            // Recompensa parcial
            int killedEnemies = wave.getEnemyCount() - enemies.size();
            int reward = killedEnemies * wave.getGoldRewardPerEnemy();
            game.setGold(game.getGold() + reward);
            log.append("Recompensa parcial: +").append(reward).append(" oro.\n");
        }

        return log.toString();
    }
}
