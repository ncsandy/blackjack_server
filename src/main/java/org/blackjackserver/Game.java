package org.blackjackserver;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.blackjackserver.BlackJackServer.players;

public class Game {
    private final List<BlackJackPlayer> blackJackPlayers;
    private final Deck deck;
    private final Dealer dealer;

    public Game() {
        this.blackJackPlayers = players;
        deck = new Deck();
        dealer = new Dealer();
    }

    public void start() {
        try {
            while (blackJackPlayers.size() == 2) {
                sendMessageToAll("Game started! Dealing cards...");

                deck.dealHand(dealer);
                sendMessageToAll(dealer.firstDealerRead());


                Iterator<BlackJackPlayer> iterator = blackJackPlayers.iterator();
                while (iterator.hasNext()) {
                    BlackJackPlayer player = iterator.next();
                    deck.dealHand(player);
                    sendPlayerHandAndScore(player);
                    processPlayerTurn(player);
                }

                if (blackJackPlayers.size() <= 1) {
                    System.out.println("Returning to main lobby");
                    sendMessageToAll("Returning to main lobby to wait for players.  Player size is: " + players.size());
                    break;
                }

                sendDealerHandAndScore();

                dealer.dealerHit(deck);

                sendDealerHandAndScore();

                determineWinner();

                resetPlayers();

                removeDisconnectedPlayers();

                sendMessageToAll("Game over!");
            }
        } catch (SocketException e) {
            System.out.println("Player disconnected. Exiting game.");
            removeDisconnectedPlayers();
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
                handlePlayerDisconnection();
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

    private void handlePlayerDisconnection() {
        removeDisconnectedPlayers();
    }

    private void removeDisconnectedPlayers() {
        List<BlackJackPlayer> disconnectedPlayers = new ArrayList<>();
        for (BlackJackPlayer player : blackJackPlayers) {
            if (player.socket.isClosed()) {
                System.out.println("Player disconnected: " + player.getName() + " UUID: " + player.getUuid());
                sendMessageToAll("Player disconnected: " + player.getName());
                disconnectedPlayers.add(player);
            }
        }
        blackJackPlayers.removeAll(disconnectedPlayers);
    }


    private void resetPlayers() {
        removeDisconnectedPlayers();
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
        player.sendMessage("You have: " + player.readHand() + '\n' + "Score: " + player.getScore() + '\n' + "Hit or Stand?");
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