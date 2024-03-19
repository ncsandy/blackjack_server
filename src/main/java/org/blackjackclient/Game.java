package org.blackjackclient;

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
        try {
            sendMessageToAll("Game started! Dealing cards...");

            deck.dealHand(dealer);
            sendMessageToAll(dealer.firstDealerRead());

            for (BlackJackPlayer player : blackJackPlayers) {
                    deck.dealHand(player);
                    player.sendMessage("You have: " +player.readHand() +'\n' + "Score: " +player.getScore());
                    processPlayerTurn(player);
                }

            sendMessageToAll("Game over!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processPlayerTurn(BlackJackPlayer player) throws IOException {
        while (true) {
            String input = player.readMessage();
            if (input.equals("hit")) {
                System.out.println(player.getName() + "C");
                player.hand.add(deck.hit());
                player.sendMessage("Your cards: " + player.readHand());
                player.sendMessage("Your score is: " + player.getScore());
            } else if (input.equals("stand")) {
                break;
            }
        }
    }
    private void sendMessageToAll(String message) {
        for (BlackJackPlayer player : blackJackPlayers) {
            player.sendMessage(message);
        }
    }

}
