package com.game.squadrontd.models;

import lombok.Getter;

@Getter
public enum DefenseType {
    INFANTRY(10, 50, 10, DamageType.PHYSICAL, ArmorType.LIGHT),  // cost, hp, attack
    ARCHER(15, 20, 25, DamageType.PHYSICAL, ArmorType.LIGHT),
    KNIGHT(25, 150, 35, DamageType.PHYSICAL, ArmorType.HEAVY),
    MAGE(20, 40, 30, DamageType.MAGIC, ArmorType.LIGHT);

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
