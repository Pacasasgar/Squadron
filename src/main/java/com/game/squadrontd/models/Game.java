package com.game.squadrontd.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.PREPARATION;

    private int currentWave = 1;
    
    // Player State simplified within Game
    private int gold = 100;
    private int income = 0;
    private int baseHealth = 100; // Monolith HP
    private int arcanium = 0;
    private int extractionLevel = 0;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DefensePlacement> defenses = new ArrayList<>();

    public void addDefense(DefensePlacement defense) {
        defenses.add(defense);
        defense.setGame(this);
    }

    @Transient
    public int getArmyValue() {
        return defenses.stream().mapToInt(d -> d.getType().getCost()).sum();
    }
}
