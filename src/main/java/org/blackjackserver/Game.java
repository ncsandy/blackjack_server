package org.blackjackserver;

import java.io.IOException;
import java.net.SocketException;
import java.util.ConcurrentModificationException;
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
        synchronized (players) {
            try {
                while (blackJackPlayers.size() == 2) {
                    try {

                        reloadDeck();

                        sendMessageToAll("Game started! Dealing cards...");
                        deck.dealHand(dealer);

                        Iterator<BlackJackPlayer> iterator = blackJackPlayers.iterator();
                        while (iterator.hasNext()) {
                            BlackJackPlayer player = iterator.next();
                            getPlayerBet(player);
                            player.sendMessage(dealer.firstDealerRead());
                            deck.dealHand(player);
                            sendPlayerHandAndScore(player);
                            processPlayerTurn(player);
                        }
                    } catch (ConcurrentModificationException e) {
                        resetPlayers();
                        System.out.println("Player disconnected during turn..");
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
    }

    private void processPlayerTurn(BlackJackPlayer player) throws IOException {
        while (true) {

            if (player.isDisconnected()) {
                break;
            }

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
        synchronized (players) {
            players.removeIf(player -> player.socket.isClosed());
        }
    }

    private void resetPlayers() {
        synchronized (players) {
            removeDisconnectedPlayers();
            for (BlackJackPlayer player : blackJackPlayers) {
                player.hand.clear();
                player.score = 0;
            }
        }
        dealer.clearHand();
        dealer.score = 0;
    }

    private void sendMessageToAll(String message) {
        synchronized (players) {
            for (BlackJackPlayer player : blackJackPlayers) {
                player.sendMessage(message);
            }
        }
    }

    private void sendPlayerHandAndScore(BlackJackPlayer player) throws IOException {
        player.sendMessage("You have: " + player.readHand() + '\n' + "Score: " + player.getScore() + '\n' + "Hit or Stand?");
    }

    private void sendDealerHandAndScore() {
        sendMessageToAll("\nDealer has: " + dealer.readHand() + "\nScore is:" + dealer.getScore());
    }

    private void determineWinner() {
        synchronized (players) {
            int dealerScore = dealer.getScore();
            for (BlackJackPlayer player : blackJackPlayers) {
                int playerScore = player.getScore();
                if (playerScore == 21){
                    player.sendMessage("\nCongratulations! You got Blackjack!");
                    calculator("blackjack", player);
                    System.out.println(player.getMoney());
                }
                else if (playerScore > 21) {
                    player.sendMessage("\nBust! You lose.");
                    calculator("lose", player);
                    System.out.println(player.getMoney());
                } else if (dealerScore > 21 || playerScore > dealerScore) {
                    player.sendMessage("\nCongratulations! You win.");
                    calculator("win", player);
                    System.out.println(player.getMoney());
                } else if (playerScore == dealerScore) {
                    player.sendMessage("\nPush!");
                    System.out.println(player.getMoney());
                } else {
                    player.sendMessage("\nDealer wins. Better luck next time.");
                    calculator("lose", player);
                    System.out.println(player.getMoney());
                }
            }
        }
    }

    private void getPlayerBet(BlackJackPlayer player) throws IOException {
        int bet = 0;
        while (true) {
            player.sendMessage("How much would you like to bet? You have " + player.getMoney());
            String input = player.readMessage();
            if (input == null) {
                handlePlayerDisconnection();
                break;
            }

            try {
                bet = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                player.sendMessage("Please but a correct number..");
                continue;
            }
            if (bet <= 0) {
                player.sendMessage("Please enter a positive bet amount.");
                continue;
            }

            if (bet > player.getMoney()) {
                player.sendMessage("You dont have enough money to bet that much..");
            }

            if (bet <= player.getMoney()) {
                player.sendMessage("You are now betting: " + bet);
                player.setBet(bet);
                player.setMoney(player.getMoney() - bet);
                break;
            }
        }
    }

    public void calculator(String outcome, BlackJackPlayer player) {
        int money = 0;
        switch (outcome) {
            case "win":
                money = player.money += player.bet * 2;
                player.setMoney(money);
                player.sendMessage("You now have:" + player.getMoney());
                break;
            case "lose":
                money = player.money - player.bet;
                player.setMoney(money);
                player.sendMessage("You now have:" + player.getMoney());
                break;
            case "blackjack":
                money = player.money += (int) (player.bet + player.bet * 1.5);
                player.setMoney(money);
                player.sendMessage("You now have:" + player.getMoney());
                System.out.println(player.getMoney());
                break;
            default:
                throw new IllegalArgumentException("Invalid outcome: " + outcome);
        }
    }

    private void reloadDeck() {
        if (deck.getCards().size() <= 10) {
            Deck newDeck = new Deck();
            deck.getCards().addAll(newDeck.getCards());
        }
    }
}
