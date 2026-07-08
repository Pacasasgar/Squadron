package com.game.squadrontd.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WaveInfo {
    private int waveNumber;
    private String enemyName;
    private int enemyCount;
    private int enemyHp;
    private int enemyAttack;
    private int goldRewardPerEnemy;
}
