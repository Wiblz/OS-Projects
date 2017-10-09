package com.company;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;

public class Controller extends Thread {

    private final int x;
    //map, containing processes and corresponding function names
    private Map<String, Process> processes;
    private Map<String, Integer> result;
    private FunctionsContainer container;
    private Server server;
    private Scanner inputReader;
    private boolean isPromptNeeded = true;
    private boolean isCancelled = false;
    private boolean isResultComputed = false;
    private long lastContinue;

    Controller() {
        //Getting argument from user
        System.out.println("Please, enter x: ");
        inputReader = new Scanner(System.in);
        x = inputReader.nextInt();

        processes = new HashMap<>();
        result = new HashMap<>();
        container = new FunctionsContainer();
        server = new Server(container.numberOfFunctions);
        server.start();
    }

    private void shutProcesses() {
        for(Map.Entry<String, Process> entry : processes.entrySet()) {
            if(entry.getValue().isAlive()) {
                entry.getValue().destroyForcibly();
            }
        }
    }

    private void initProcesses() throws IOException{
        for(Map.Entry<String, FunctionInterface> entry : container.getAvailableFunctionsEntrySet()) {
            String[] command = {"java", "com.company.Main", entry.getKey(), Integer.toString(x)};
            ProcessBuilder probuilder = new ProcessBuilder(command);
            probuilder.directory(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getFile().substring(1).replace("%20"," " )));
            processes.put(entry.getKey(), probuilder.start());
        }
    }

    private int ask() {
        System.out.println("1. Сontinue");
        System.out.println("2. Continue without prompt");
        System.out.println("3. Cancel");

        return inputReader.nextInt();
    }

    private void serverInterrupt() {
        if(!server.isInterrupted()) {
            server.interrupt();
        }
    }

    private void startUserPrompt() {
        int userChoise;
        ResultContainer lastResult = new ResultContainer();
        lastContinue = System.currentTimeMillis();

        while(!isCancelled && !isResultComputed) {
            if(isPromptNeeded && System.currentTimeMillis() - lastContinue > 5000) {
                userChoise = ask();
                switch(userChoise) {
                    case 1:
                        lastContinue = System.currentTimeMillis();
                        break;
                    case 2:
                        isPromptNeeded = false;
                        break;
                    case 3:
                        shutProcesses();
                        isCancelled = true;
                        break;
                    default:
                        System.out.println("Response isn't recognized. Continuing computation.");
                        lastContinue = System.currentTimeMillis();
                        break;
                }
            }

            while((lastResult = server.resultPoll()) != null) {
                System.out.println(lastResult.getFormattedResult());
                result.put(lastResult.getFunctionName(), lastResult.getFunctionResult());
                if(result.size() == FunctionsContainer.numberOfFunctions || lastResult.getFunctionResult() == 0) {
                    isResultComputed = true;
                    if(lastResult.getFunctionResult() == 0) {
                        System.out.println(lastResult.getFunctionName() + " is 0, shutting down other processes");
                        shutProcesses();
                        System.out.println("Final result is 0.");
                        break;
                    }
                    else {
                        System.out.println("All functions returned result. Final result is " + operation());
                        break;
                    }
                }
            }
        }
        serverInterrupt();
        if(isCancelled) {
            System.out.println("Cancelled by user.");
        }
    }

    private int operation() {
        return result.get("f") + result.get("g") - result.get("h");
    }

    @Override
    public void run() {
        try {
            initProcesses();
        }
        catch (IOException e) {
            System.out.println("Can't create process.");
            System.exit(-1);
        }
        startUserPrompt();
    }
}