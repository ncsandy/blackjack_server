package org.blackjackserver;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Game {
    private final List<BlackJackPlayer> blackJackPlayers;
    private final Deck deck;
    private final Dealer dealer;

    public Game(List<BlackJackPlayer> blackJackPlayers) {
        this.blackJackPlayers = blackJackPlayers;
        deck = new Deck();
        dealer = new Dealer();
    }

    public void start() {
        try {
            while (blackJackPlayers.size() == 2) {
                sendMessageToAll("Game started! Dealing cards...");

                deck.dealHand(dealer);
                sendMessageToAll(dealer.firstDealerRead());

                if (blackJackPlayers.isEmpty()) {
                    break;
                }

                for (BlackJackPlayer player : blackJackPlayers) {
                    deck.dealHand(player);
                    sendPlayerHandAndScore(player);
                    processPlayerTurn(player);
                }

                sendDealerHandAndScore();

                dealer.dealerHit(deck);

                sendDealerHandAndScore();

                determineWinner();

                resetPlayers();

                removeDisconnectedPlayer();

                sendMessageToAll("Game over!");
            }
        } catch (SocketException e) {
            System.out.println("Player disconnected. Exiting game.");
            removeDisconnectedPlayer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processPlayerTurn(BlackJackPlayer player) throws IOException {
        while (true) {
            if (player.getScore() == 21) {
                player.sendMessage("Blackjack!");
                break;
            }

            String input = player.readMessage();
            if (input == null) {
                handlePlayerDisconnection(player);
                break;
            }

            if (input.equals("hit")) {
                handlePlayerHit(player);
            } else if (input.equals("stand")) {
                handlePlayerStand(player);
                break;
            }
        }
    }

    private void handlePlayerHit(BlackJackPlayer player) throws IOException {
        System.out.println(player.getName() + " chose to hit");
        player.hand.add(deck.hit());
        sendPlayerHandAndScore(player);
    }

    private void handlePlayerStand(BlackJackPlayer player) {
        System.out.println(player.getName() + " chose to stand");
    }

    private void handlePlayerDisconnection(BlackJackPlayer player) {
        System.out.println(player.getName() + " disconnected.");
        removeDisconnectedPlayer();
    }

    private void removeDisconnectedPlayer() {
        List<BlackJackPlayer> disconnectedPlayers = new ArrayList<>();
        for (BlackJackPlayer player : blackJackPlayers) {
            if (player.socket.isClosed()) {
                System.out.println("Player disconnected: " + player.getName());
                sendMessageToAll("Player disconnected: " + player.getName());
                disconnectedPlayers.add(player);
            }
        }
        blackJackPlayers.removeAll(disconnectedPlayers);
    }


    private void resetPlayers() {
        for (BlackJackPlayer player : blackJackPlayers) {
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

    private void sendPlayerHandAndScore(BlackJackPlayer player) throws IOException {
        player.sendMessage("You have: " + player.readHand() + '\n' + "Score: " + player.getScore());
    }

    private void sendDealerHandAndScore() {
        sendMessageToAll("Dealer has: " + dealer.readHand() + "\nScore is:" + dealer.getScore());
    }

    private void determineWinner() {
        int dealerScore = dealer.getScore();
        for (BlackJackPlayer player : blackJackPlayers) {
            int playerScore = player.getScore();
            if (playerScore > 21) {
                player.sendMessage("Bust! You lose.");
            } else if (dealerScore > 21 || playerScore > dealerScore) {
                player.sendMessage("Congratulations! You win.");
            } else if (playerScore == dealerScore) {
                player.sendMessage("Push!");
            } else {
                player.sendMessage("Dealer wins. Better luck next time.");
            }
        }
    }
}
