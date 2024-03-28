package org.blackjackserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlackJackServer {
    private static final int PORT = 8888;
    private ServerSocket serverSocket;
    static List<BlackJackPlayer> players;
    private Game game;
    private ExecutorService executor;

    public BlackJackServer() {
        players = new ArrayList<>();
        executor = Executors.newFixedThreadPool(2);
    }

    public void start(){
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started. Waiting for players...");

            while (true) {

                executor.execute(() -> {
                    try {
                        removeDisconnectedPlayers();
                        acceptPlayerConnections();
                    } catch (IOException e) {
                        System.out.println("Error while accepting connections");
                        e.printStackTrace();
                    }
                });

                if (isReady()) {
                    startGame();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            shutdown();
        }
    }

    private void acceptPlayerConnections() throws IOException {
        while (players.size() < 2 && !serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Player connected: " + socket);
                BlackJackPlayer player = new BlackJackPlayer(socket);
                assignName(player);
                players.add(player);
                if (players.size() != 2) {
                    player.sendMessage("Waiting for players to join..");
                }
                removeDisconnectedPlayers();
                if (isReady()) {
                    break;
                }
            }
    }
    public void removeDisconnectedPlayers() {
        synchronized (players) {
            players.removeIf(player -> player.socket.isClosed());
        }
    }

    private void assignName(BlackJackPlayer player) throws IOException {
        player.sendMessage("What is your name?");
        String input = player.readMessage();
        if (input != null) {
            player.setName(input);
            System.out.println("New player created " + player.getName() + " UUID " + player.getUuid());
            player.sendMessage("Your name is now: " + player.getName());
        }
    }

    private boolean isReady() {
        return players.size() == 2;
    }

    private void startGame() {
        removeDisconnectedPlayers();
        if (isReady()) {
            game = new Game();
            game.start();
        }
    }

    private void shutdown() {
        try {
            System.out.println("Server encountered a critical error, shutting down.");
            serverSocket.close();
            executor.shutdown();
        } catch (IOException e) {
            System.out.println("Failed to shutdown...trying again");
            shutdown();
        }
    }
    public static void main(String[] args) {
        BlackJackServer server = new BlackJackServer();
        server.start();
    }
}


