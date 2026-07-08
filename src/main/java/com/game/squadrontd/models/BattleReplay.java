package com.game.squadrontd.models;

import lombok.Data;

import java.util.List;

@Data
public class BattleReplay {
    private List<CombatUnitState> initialDefenses;
    private List<CombatUnitState> initialEnemies;
    private List<RoundSnapshot> rounds;
    private String textLog;
    private int remainingBaseHealth;
    private int totalGoldReward;
    private boolean isGameOver;
    private boolean isVictory;

    @Data
    public static class CombatUnitState {
        private String id; // e.g. "DEF_0", "ENE_1"
        private String name;
        private int hp;
        private int maxHp;
        private DamageType damageType;
        private ArmorType armorType;

        public CombatUnitState(String id, String name, int hp, int maxHp, DamageType damageType, ArmorType armorType) {
            this.id = id;
            this.name = name;
            this.hp = hp;
            this.maxHp = maxHp;
            this.damageType = damageType;
            this.armorType = armorType;
        }
    }

    @Data
    public static class RoundSnapshot {
        private int roundNumber;
        private List<Integer> defenseHps; // Matches order of initialDefenses
        private List<Integer> enemyHps; // Matches order of initialEnemies

        public RoundSnapshot(int roundNumber, List<Integer> defenseHps, List<Integer> enemyHps) {
            this.roundNumber = roundNumber;
            this.defenseHps = defenseHps;
            this.enemyHps = enemyHps;
        }
    }
}
