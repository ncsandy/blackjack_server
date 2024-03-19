package org.blackjackclient;
public class Formatter {
    String getCardInfo(Card card) {

        String cardInfo = "";

        if (card.isAce()) {
            cardInfo += "Ace";
        } else if (card.getFace() != null) {
            cardInfo += card.getFace().getFaceName();
        } else {
            cardInfo += card.getValue();
        }

        cardInfo += " of " + card.getSuite().getSuiteName();
        return cardInfo;
    }
}
