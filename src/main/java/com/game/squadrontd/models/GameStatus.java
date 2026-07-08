package com.game.squadrontd.models;

public enum GameStatus {
    PREPARATION, // Player is buying things
    RESOLVING,   // Wave is being resolved
    GAME_OVER,   // Player lost all health
    VICTORY      // Player beat all waves
}
