package org.blackjackserver;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Deck {

    @Getter
    private List<Card> cards;
    private Random random;

    public Deck() {
        cards = new ArrayList<>();
        random = new Random();
        initDeck();
        shuffle();
    }

    private void initDeck() {
        for (Suite suite : Suite.values()) {
            for (int i = 2; i <= 14; i++) {
                Card card = new Card();
                card.setSuite(suite);
                card.setValue(i);
                card.setAce(i == 11);

                if (i >= 12) {
                    creatSuite(suite, cards);
                    break;
                }

                cards.add(card);
            }
        }
    }
    public void creatSuite(Suite suite, List<Card> deck) {
        for (Face face : Face.values()) {
            Card card = new Card();
            card.setSuite(suite);
            card.setFace(face);
            card.setValue(10);
            deck.add(card);
        }
    }
    private void shuffle() {
        Collections.shuffle(cards);
    }

    public void dealHand(Player player) {
        for (int i = 0; i < 2; i++) {
            int index = random.nextInt(cards.size());
            player.addCard(cards.remove(index));
        }
    }
    public Card hit() {
        int index = random.nextInt(cards.size());
        return cards.remove(index);
    }
}
