package org.blackjackserver;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;

@NoArgsConstructor
public class BlackJackPlayer extends CardHolder {
    Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    @Getter
    private UUID uuid;

    @Getter @Setter
    private String name;

    @Getter
    private boolean disconnected;

    @Getter @Setter
    int money;

    @Getter @Setter
    int bet;


    public BlackJackPlayer(Socket socket) throws IOException {
        this.socket = socket;
        this.uuid = UUID.randomUUID();

        this.money = 100;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Failed to create input or output stream");
            e.printStackTrace();
        }
    }

    // Network methods

    public void sendMessage(String message) {
        out.println(message);
    }

    public String readMessage() throws IOException {
        try {
            return in.readLine();
        } catch (SocketException e) {
            System.out.println(this.name + " disconnected. Closing socket: " + socket);
            disconnected = true;
            close();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            disconnected = true;
            close();
            return null;
        }
    }
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

}
