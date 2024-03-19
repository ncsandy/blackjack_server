package org.blackjackserver;

public class Dealer extends CardHolder {
    public Dealer() {

    }

    @Override
    public int getScore() {
        int score = 0;
        for (Card card : this.hand) {
            score += card.getValue();
        }
        this.score = score;
        return this.score;
    }

    public String firstDealerRead() {
        return "Dealer is showing " + formatter.getCardInfo(this.hand.get(0));
    }

    public void dealerHit(Deck deck) {
        score = getScore();

        if (score == 21) {
            return;
        }

        if (score == 17){
            return;
        }

        while (score < 17) {
            addCard(deck.hit());
            getScore();
        }
    }

}
