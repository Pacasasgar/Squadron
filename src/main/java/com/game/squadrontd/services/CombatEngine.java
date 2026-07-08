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
        // 1. Calcular el poder total de nuestras defensas
        int totalDefenseHp = game.getDefenses().stream().mapToInt(d -> d.getType().getHp()).sum();
        int totalDefenseAttack = game.getDefenses().stream().mapToInt(d -> d.getType().getAttack()).sum();

        // 2. Calcular el poder total de la oleada
        int totalEnemyHp = wave.getEnemyCount() * wave.getEnemyHp();
        int totalEnemyAttack = wave.getEnemyCount() * wave.getEnemyAttack();

        StringBuilder log = new StringBuilder();
        log.append("--- INICIO DE OLEADA ").append(wave.getWaveNumber()).append(" ---\n");
        log.append("Defensas (Poder: ").append(totalDefenseAttack).append(", Vida: ").append(totalDefenseHp).append(")\n");
        log.append("Enemigos: ").append(wave.getEnemyCount()).append("x ").append(wave.getEnemyName())
           .append(" (Poder: ").append(totalEnemyAttack).append(", Vida: ").append(totalEnemyHp).append(")\n");

        // Regla súper simplificada: 
        // Si nuestro ataque es mayor a la vida del enemigo, mueren sin tocarnos.
        // Si no, sufriremos algo de daño que absorberán nuestras defensas. Si nuestras defensas mueren, sufrimos en la base.
        
        // Fase 1: Defensas atacan primero (ventaja defensora)
        int remainingEnemyHp = totalEnemyHp - totalDefenseAttack;
        
        if (remainingEnemyHp <= 0) {
            log.append("¡Tus defensas han aniquilado la oleada sin recibir un rasguño!\n");
            // Dar oro de recompensa completo
            int reward = wave.getEnemyCount() * wave.getGoldRewardPerEnemy();
            game.setGold(game.getGold() + reward);
            log.append("Recompensa: +").append(reward).append(" oro.\n");
        } else {
            // Fase 2: Los enemigos sobrevivientes atacan
            // (El daño del enemigo es proporcional a la vida que les quedó)
            double survivalRatio = (double) remainingEnemyHp / totalEnemyHp;
            int actualEnemyDamage = (int) (totalEnemyAttack * survivalRatio);
            
            log.append("Sobrevivieron algunos enemigos. Te atacan con poder de: ").append(actualEnemyDamage).append("\n");
            
            int remainingDefenseHp = totalDefenseHp - actualEnemyDamage;
            
            if (remainingDefenseHp >= 0) {
                log.append("Tus defensas aguantaron el golpe. ¡Sobrevives a la oleada!\n");
            } else {
                // Las defensas cayeron, el daño remanente va a la base
                int damageToBase = Math.abs(remainingDefenseHp);
                game.setBaseHealth(Math.max(0, game.getBaseHealth() - damageToBase));
                log.append("¡Tus defensas fueron destruidas! Tu base ha recibido ").append(damageToBase).append(" de daño.\n");
            }

            // Damos recompensa parcial basada en daño hecho
            int killedEnemies = (int)((1.0 - survivalRatio) * wave.getEnemyCount());
            int reward = killedEnemies * wave.getGoldRewardPerEnemy();
            game.setGold(game.getGold() + reward);
            log.append("Recompensa parcial: +").append(reward).append(" oro.\n");
        }
        
        return log.toString();
    }
}
