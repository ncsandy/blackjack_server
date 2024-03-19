package org.blackjackserver;

import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

abstract class CardHolder implements Player {
    protected List<Card> hand;
    protected Formatter formatter;

    @Setter
    protected int score;
    public CardHolder() {
        this.hand = new ArrayList<>();
        this.formatter = new Formatter();
    }

    @Override
    public void addCard(Card card) {
        this.hand.add(card);
    }

    @Override
    public void clearHand() {
        this.hand.clear();
    }

    @Override
    public String readHand() {
        StringBuilder builder = new StringBuilder();
        for (Card card: this.hand) {
            builder.append(formatter.getCardInfo(card)).append(" ");
        }
        return builder.toString();
    }

    @Override
    public int getScore() {
        int score = 0;
        int numOfAces = 0;
        for (Card card : this.hand) {
            score += card.getValue();
            if (card.isAce()) {
                numOfAces++;
            }
            if (score > 21 && numOfAces > 0) {
                numOfAces -= 1;
                score -= 10;
            }
        }
        this.score = score;
        return this.score;
    }

    @Override
    public void reset() {
        score = 0;
        hand.clear();
    }

}
