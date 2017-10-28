package com.company;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    private Socket socket;
    private PrintWriter out;
    private FunctionsContainer container;
    private String functionToExecute;
    private int x;

    Client(String functionToExecute, int x) {
        container = new FunctionsContainer();
        this.functionToExecute = functionToExecute;
        this.x = x;
    }

    public void calculateAndSendToServer() throws InterruptedException {
        int answer = container.getAvailableFunctions().get(functionToExecute).calculate(x);

        try {
            socket = new Socket("localhost", 5555);
            out = new PrintWriter(socket.getOutputStream(), true);
        }
        catch(IOException e) {
            System.out.println("Can't connect to the server.");
            System.exit(-1);
        }
        out.println(functionToExecute);
        out.println(answer);
        out.close();
        try {
            socket.close();
        }
        catch (IOException e) {
            System.out.println("Can't close client socket.");
            System.exit(-1);
        }
    }
}
