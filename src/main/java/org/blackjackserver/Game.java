package org.blackjackserver;

import java.io.IOException;
import java.util.List;


public class Game {

    private List<BlackJackPlayer> blackJackPlayers;
    private Deck deck;
    private final Dealer dealer;


    public Game(List<BlackJackPlayer> blackJackPlayers) {
        this.blackJackPlayers = blackJackPlayers;
        deck = new Deck();
        dealer = new Dealer();
    }


    public void start() {
        while (true) {
            try {
                sendMessageToAll("Game started! Dealing cards...");

                deck.dealHand(dealer);
                sendMessageToAll(dealer.firstDealerRead());

                for (BlackJackPlayer player : blackJackPlayers) {
                    deck.dealHand(player);
                    player.sendMessage("You have: " + player.readHand() + '\n' + "Score: " + player.getScore());
                    processPlayerTurn(player);
                }

                sendMessageToAll("Dealer has: " + dealer.readHand() + "\nScore is:" + dealer.getScore());

                dealer.dealerHit(deck);

                sendMessageToAll("Dealer has: " + dealer.readHand() + "\nScore is:" + dealer.getScore());


                reset(blackJackPlayers);

                sendMessageToAll("Game over!");

            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    private void processPlayerTurn(BlackJackPlayer player) throws IOException {
        while (true) {
            String input = player.readMessage();
            if (input.equals("hit")) {
                System.out.println(player.getName() + " chose to hit");
                player.hand.add(deck.hit());
                player.sendMessage("Your cards: " + player.readHand());
                player.sendMessage("Your score is: " + player.getScore());
            } else if (input.equals("stand")) {
                System.out.println(player.getName() + " chose to stand");
                break;
            }
        }
    }

    private void reset(List<BlackJackPlayer> players) {
        for (BlackJackPlayer player: players){
            player.hand.clear();
            player.score = 0;
        }
        dealer.clearHand();
        dealer.score = 0;
    }
    private void sendMessageToAll(String message) {
        for (BlackJackPlayer player : blackJackPlayers) {
            player.sendMessage(message);
        }
    }

}
