package com.game.squadrontd.models;

import lombok.Getter;

@Getter
public enum DefenseType {
    INFANTRY(10, 60, 10, DamageType.PHYSICAL, ArmorType.LIGHT),  // cost, hp, attack
    ARCHER(15, 30, 35, DamageType.PHYSICAL, ArmorType.LIGHT),
    KNIGHT(25, 120, 20, DamageType.PHYSICAL, ArmorType.HEAVY),
    MAGE(20, 40, 40, DamageType.MAGIC, ArmorType.LIGHT);

    private final int cost;
    private final int hp;
    private final int attack;
    private final DamageType damageType;
    private final ArmorType armorType;

    DefenseType(int cost, int hp, int attack, DamageType damageType, ArmorType armorType) {
        this.cost = cost;
        this.hp = hp;
        this.attack = attack;
        this.damageType = damageType;
        this.armorType = armorType;
    }
}
