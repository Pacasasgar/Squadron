package com.game.squadrontd.services;

import com.game.squadrontd.models.DefensePlacement;
import com.game.squadrontd.models.Game;
import com.game.squadrontd.models.WaveInfo;
import org.springframework.stereotype.Service;

/**
 * CombatEngine: Lógica pura de cálculo (Algoritmo de Batalla).
 * Extraemos esto fuera del GameService para mantener la "Single Responsibility".
 * Aquí simulamos la batalla entre las unidades defensivas del jugador y los enemigos de la oleada.
 */
@Service
public class CombatEngine {

    /**
     * Resuelve el combate y devuelve un resumen de texto de lo ocurrido.
     * Altere el estado del juego (vida de la base y recompensas) basado en el resultado.
     */
    public String resolveWave(Game game, WaveInfo wave) {
        int baseDefenseHp = game.getDefenses().stream().mapToInt(d -> d.getType().getHp()).sum();
        int baseDefenseAttack = game.getDefenses().stream().mapToInt(d -> d.getType().getAttack()).sum();

        int baseEnemyHp = wave.getEnemyCount() * wave.getEnemyHp();
        int baseEnemyAttack = wave.getEnemyCount() * wave.getEnemyAttack();

        int currentDefenseHp = baseDefenseHp;
        int currentEnemyHp = baseEnemyHp;

        StringBuilder log = new StringBuilder();
        log.append("--- INICIO DE OLEADA ").append(wave.getWaveNumber()).append(" ---\n");
        log.append("Defensas (Poder: ").append(baseDefenseAttack).append(", Vida: ").append(baseDefenseHp).append(")\n");
        log.append("Enemigos: ").append(wave.getEnemyCount()).append("x ").append(wave.getEnemyName())
           .append(" (Poder: ").append(baseEnemyAttack).append(", Vida: ").append(baseEnemyHp).append(")\n");

        if (baseDefenseAttack == 0) {
            log.append("No tienes defensas. Los enemigos avanzan directamente hacia la base.\n");
            currentDefenseHp = 0;
        }

        int round = 1;
        while (currentDefenseHp > 0 && currentEnemyHp > 0) {
            // Calcular daño actual basado en porcentaje de vida (para que a menos vida, menos peguen)
            int currentDefenseAttack = (int) Math.ceil(baseDefenseAttack * ((double) currentDefenseHp / baseDefenseHp));
            
            // Defensas atacan primero
            currentEnemyHp -= currentDefenseAttack;
            log.append("Ronda ").append(round).append(": Defensas hacen ").append(currentDefenseAttack).append(" daño. ");
            
            if (currentEnemyHp <= 0) {
                log.append("Enemigos aniquilados.\n");
                break;
            }
            
            // Enemigos supervivientes atacan
            int currentEnemyAttack = (int) Math.ceil(baseEnemyAttack * ((double) currentEnemyHp / baseEnemyHp));
            currentDefenseHp -= currentEnemyAttack;
            log.append("Enemigos responden con ").append(currentEnemyAttack).append(" daño.\n");
            
            round++;
        }

        if (currentEnemyHp <= 0) {
            log.append("¡Tus defensas han defendido la base con éxito!\n");
            int reward = wave.getEnemyCount() * wave.getGoldRewardPerEnemy();
            game.setGold(game.getGold() + reward);
            log.append("Recompensa: +").append(reward).append(" oro.\n");
        } else {
            // Las defensas murieron, el daño de los enemigos restantes va a la base
            double survivalRatio = (double) currentEnemyHp / baseEnemyHp;
            int remainingDamage = (int) Math.ceil(baseEnemyAttack * survivalRatio);
            
            game.setBaseHealth(Math.max(0, game.getBaseHealth() - remainingDamage));
            log.append("¡Tus defensas fueron destruidas! Tu base sufre ").append(remainingDamage).append(" de daño.\n");

            // Recompensa parcial
            int killedEnemies = (int)((1.0 - survivalRatio) * wave.getEnemyCount());
            int reward = killedEnemies * wave.getGoldRewardPerEnemy();
            game.setGold(game.getGold() + reward);
            log.append("Recompensa parcial: +").append(reward).append(" oro.\n");
        }
        
        return log.toString();
    }
}
