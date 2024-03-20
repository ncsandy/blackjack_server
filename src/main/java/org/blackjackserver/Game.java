package org.blackjackserver;

import java.io.IOException;
import java.net.SocketException;
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
            while (blackJackPlayers.size() == 2) {
                sendMessageToAll("Game started! Dealing cards...");

                deck.dealHand(dealer);
                sendMessageToAll(dealer.firstDealerRead());

                if (blackJackPlayers.isEmpty()) {
                    break;
                }

                for (BlackJackPlayer player : blackJackPlayers) {
                    deck.dealHand(player);
                    player.sendMessage("You have: " + player.readHand() + '\n' + "Score: " + player.getScore());
                    processPlayerTurn(player);
                }

                sendMessageToAll("Dealer has: " + dealer.readHand() + "\nScore is:" + dealer.getScore());

                dealer.dealerHit(deck);

                sendMessageToAll("Dealer has: " + dealer.readHand() + "\nScore is:" + dealer.getScore());

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

                reset(blackJackPlayers);

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

                System.out.println(player.getName() + " disconnected.");
                removeDisconnectedPlayer();
                break;
            }

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

    private void removeDisconnectedPlayer() {
        blackJackPlayers.removeIf(player -> {
            if (player.socket.isClosed()) {
                System.out.println("Player disconnected: " + player.getName());
                sendMessageToAll("Player disconnected: " + player.getName());
                return true;
            }
            return false;
        });
    }

    private void reset(List<BlackJackPlayer> players) {
        for (BlackJackPlayer player : players) {
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
