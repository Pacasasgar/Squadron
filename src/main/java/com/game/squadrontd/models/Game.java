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
    private int gold = 50;
    private int income = 10;
    private int baseHealth = 100;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DefensePlacement> defenses = new ArrayList<>();

    public void addDefense(DefensePlacement defense) {
        defenses.add(defense);
        defense.setGame(this);
    }
}
