package com.company;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        //First launch of the program. We should init the controller, which then launches server and new processes
        if(args.length == 0) {
            Controller controller = new Controller();
            controller.start();
        }

        //We're in secondary process, which will soon calculate result of one of the functions
        else {
            Client client = new Client(args[0], Integer.parseInt(args[1]));
            client.calculateAndSendToServer();
        }
    }
}