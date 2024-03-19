package org.blackjackclient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class BlackJackServer {
    private static final int PORT = 8888;
    private ServerSocket serverSocket;
    private List<BlackJackPlayer> players;

    private Game game;

    public BlackJackServer() {
        players = new ArrayList<>();

    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started. Waiting for players...");

            while (true) {
                acceptPlayerConnections();
                if (isReady()) {
                    game = new Game(players);
                    game.start();
                    players.clear();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    private void acceptPlayerConnections() throws IOException {
        while (players.size() < 2) {
            Socket socket = serverSocket.accept();
            System.out.println("Player connected: " + socket);
            BlackJackPlayer player = new BlackJackPlayer(socket);
            assignName(player);
            players.add(player);

            if (players.size() == 2) {
                break;  // Exit the loop once two players have joined
            }
        }
    }

    private void assignName(BlackJackPlayer player) throws IOException {
        while (true) {
            player.sendMessage("What is your name?");
            String input = player.readMessage();
            if (input != null) {
                player.setName(input);
                player.sendMessage("Your name is now: " + player.getName());
                break;
            }
        }
    }
    private boolean isReady() {
        return players.size() == 2;
    }

    private void shutdown() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        BlackJackServer server = new BlackJackServer();
        server.start();
    }
}