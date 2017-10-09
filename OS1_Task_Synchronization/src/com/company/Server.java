package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server extends Thread {

    private final int numberOfFunctions;
    private BufferedReader in;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BlockingQueue<ResultContainer> results;

    Server(int numberOfFunctions) {
        super();

        this.numberOfFunctions = numberOfFunctions;
        results = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        try {
            try {
                serverSocket = new ServerSocket(5555);
                serverSocket.setSoTimeout(1000);
            } catch (IOException e) {
                System.out.println("Can't listen port 5555.");
                System.exit(-1);
            }
            for (int i = 0; i < numberOfFunctions; i++) {
                try {
                    clientSocket = serverSocket.accept();
                } catch (SocketTimeoutException e) {
                    i--;
                    continue;
                } catch (IOException e) {
                    System.out.println("Can't accept client.");
                    System.exit(-1);
                }
                in = new BufferedReader(new
                        InputStreamReader(clientSocket.getInputStream()));
                try {
                    results.put(new ResultContainer(in.readLine(), Integer.parseInt(in.readLine())));
                } catch (InterruptedException e) {
                    System.out.println("Can't read data from request");
                    System.exit(-1);
                }

                in.close();
                clientSocket.close();

            }
        }
        catch(IOException e) {

        }
    }

    public ResultContainer resultPoll() {
        return results.poll();
    }

}
