package com.game.squadrontd.services;

import com.game.squadrontd.models.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CombatEngine {

    // Clase interna temporal para simular combate táctico
    private static class CombatUnit {
        String id;
        String name;
        int hp;
        int maxHp;
        int attack;
        DamageType damageType;
        ArmorType armorType;

        CombatUnit(String id, String name, int hp, int attack, DamageType damageType, ArmorType armorType) {
            this.id = id;
            this.name = name;
            this.hp = hp;
            this.maxHp = hp;
            this.attack = attack;
            this.damageType = damageType;
            this.armorType = armorType;
        }
    }

    private double calculateDamageMultiplier(DamageType attackerDamage, ArmorType defenderArmor) {
        if (attackerDamage == DamageType.PHYSICAL) {
            return defenderArmor == ArmorType.LIGHT ? 1.5 : 0.5;
        } else if (attackerDamage == DamageType.MAGIC) {
            return defenderArmor == ArmorType.HEAVY ? 1.5 : 0.5;
        }
        return 1.0;
    }

    public BattleReplay resolveWave(Game game, WaveInfo wave) {
        BattleReplay replay = new BattleReplay();
        StringBuilder log = new StringBuilder();
        log.append("--- INICIO DE OLEADA ").append(wave.getWaveNumber()).append(" ---\n");

        // 1. Fase de Setup: Instanciar unidades
        List<CombatUnit> defenses = new ArrayList<>();
        List<BattleReplay.CombatUnitState> initialDefensesState = new ArrayList<>();
        int defIndex = 0;
        for (DefensePlacement dp : game.getDefenses()) {
            String id = "DEF_" + defIndex++;
            DefenseType type = dp.getType();
            defenses.add(new CombatUnit(id, type.name(), type.getHp(), type.getAttack(), type.getDamageType(), type.getArmorType()));
            initialDefensesState.add(new BattleReplay.CombatUnitState(id, type.name(), type.getHp(), type.getHp(), type.getDamageType(), type.getArmorType()));
        }

        List<CombatUnit> enemies = new ArrayList<>();
        List<BattleReplay.CombatUnitState> initialEnemiesState = new ArrayList<>();
        for (int i = 0; i < wave.getEnemyCount(); i++) {
            String id = "ENE_" + i;
            enemies.add(new CombatUnit(id, wave.getEnemyName(), wave.getEnemyHp(), wave.getEnemyAttack(), wave.getEnemyDamageType(), wave.getEnemyArmorType()));
            initialEnemiesState.add(new BattleReplay.CombatUnitState(id, wave.getEnemyName(), wave.getEnemyHp(), wave.getEnemyHp(), wave.getEnemyDamageType(), wave.getEnemyArmorType()));
        }

        replay.setInitialDefenses(initialDefensesState);
        replay.setInitialEnemies(initialEnemiesState);
        List<BattleReplay.RoundSnapshot> rounds = new ArrayList<>();

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

            // Turno Aliado
            for (CombatUnit defense : defenses) {
                if (enemies.isEmpty()) break;
                CombatUnit targetEnemy = enemies.get(0);
                
                int actualDamage = (int) Math.ceil(defense.attack * calculateDamageMultiplier(defense.damageType, targetEnemy.armorType));
                targetEnemy.hp -= actualDamage;
                enemyDamageTaken += actualDamage;

                if (targetEnemy.hp <= 0) {
                    enemies.remove(0);
                    enemiesKilledThisRound++;
                }
            }

            // Turno Enemigo
            for (CombatUnit enemy : enemies) {
                if (defenses.isEmpty()) break;
                CombatUnit targetDefense = defenses.get(0);

                int actualDamage = (int) Math.ceil(enemy.attack * calculateDamageMultiplier(enemy.damageType, targetDefense.armorType));
                targetDefense.hp -= actualDamage;
                defenseDamageTaken += actualDamage;

                if (targetDefense.hp <= 0) {
                    defenses.remove(0);
                    defensesKilledThisRound++;
                }
            }

            // Guardar Snapshot para el Frontend
            List<Integer> defHps = new ArrayList<>();
            for (BattleReplay.CombatUnitState initDef : initialDefensesState) {
                CombatUnit alive = defenses.stream().filter(u -> u.id.equals(initDef.getId())).findFirst().orElse(null);
                defHps.add(alive != null ? Math.max(0, alive.hp) : 0);
            }

            List<Integer> eneHps = new ArrayList<>();
            for (BattleReplay.CombatUnitState initEne : initialEnemiesState) {
                CombatUnit alive = enemies.stream().filter(u -> u.id.equals(initEne.getId())).findFirst().orElse(null);
                eneHps.add(alive != null ? Math.max(0, alive.hp) : 0);
            }

            rounds.add(new BattleReplay.RoundSnapshot(round, defHps, eneHps));

            log.append("> Ronda ").append(round).append(" | ")
               .append("Aliados infligen ").append(enemyDamageTaken).append(" daño (").append(enemiesKilledThisRound).append(" bajas). ");
            
            if (!enemies.isEmpty()) {
                log.append("Enemigos infligen ").append(defenseDamageTaken).append(" daño (").append(defensesKilledThisRound).append(" bajas).\n");
            } else {
                log.append("\n");
            }
            round++;
        }
        
        replay.setRounds(rounds);

        // 3. Resolución final
        if (enemies.isEmpty()) {
            log.append("¡Victoria! Tus defensas han aniquilado la oleada.\n");
            int reward = wave.getEnemyCount() * wave.getGoldRewardPerEnemy();
            game.setGold(game.getGold() + reward);
            log.append("Recompensa: +").append(reward).append(" oro.\n");
            replay.setTotalGoldReward(reward);
            replay.setRemainingBaseHealth(game.getBaseHealth());
            replay.setVictory(false); // Solo de ronda, la victoria total se evalua en el service
        } else {
            int remainingEnemyDamage = enemies.stream().mapToInt(e -> e.attack).sum();
            game.setBaseHealth(Math.max(0, game.getBaseHealth() - remainingEnemyDamage));
            log.append("¡Tus defensas fueron destruidas! ").append(enemies.size()).append(" enemigos sobrevivientes golpean tu base por ").append(remainingEnemyDamage).append(" de daño.\n");

            int killedEnemies = wave.getEnemyCount() - enemies.size();
            int reward = killedEnemies * wave.getGoldRewardPerEnemy();
            game.setGold(game.getGold() + reward);
            log.append("Recompensa parcial: +").append(reward).append(" oro.\n");
            
            replay.setTotalGoldReward(reward);
            replay.setRemainingBaseHealth(game.getBaseHealth());
        }

        replay.setTextLog(log.toString());
        return replay;
    }
}
