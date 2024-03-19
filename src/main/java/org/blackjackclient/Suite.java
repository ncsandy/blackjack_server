package org.blackjackclient;

import lombok.Getter;

@Getter
public enum Suite {
    HEARTS("Hearts"),
    CLUBS("Clubs"),
    DIAMONDS("Diamonds"),
    SPADES("Spades");

    private final String suiteName;

    Suite(String suiteName) {
        this.suiteName = suiteName;
    }

}
