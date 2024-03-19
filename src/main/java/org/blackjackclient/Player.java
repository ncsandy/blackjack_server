package org.blackjackclient;

import java.io.IOException;

public interface Player {
    void addCard(Card card);
    void clearHand();
    int getScore();
    String readHand();
    void reset();

}
