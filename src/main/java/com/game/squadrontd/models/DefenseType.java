package com.game.squadrontd.models;

import lombok.Getter;

@Getter
public enum DefenseType {
    INFANTRY(20, 50, 10),  // cost, hp, attack
    ARCHER(25, 20, 25),
    KNIGHT(50, 150, 35);

    private final int cost;
    private final int hp;
    private final int attack;

    DefenseType(int cost, int hp, int attack) {
        this.cost = cost;
        this.hp = hp;
        this.attack = attack;
    }
}
