package org.blackjackclient;

import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class BlackJackPlayer extends CardHolder {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    @Getter @Setter
    private String name;

    public BlackJackPlayer(Socket socket)  throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    // Network methods

    public void sendMessage(String message) {
        out.println(message);
    }

    public String readMessage() throws IOException {
        return in.readLine();
    }
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

}
