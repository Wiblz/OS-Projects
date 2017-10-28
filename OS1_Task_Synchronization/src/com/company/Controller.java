package com.company;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.util.logging.*;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.keyboard.NativeKeyEvent;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static org.jnativehook.GlobalScreen.unregisterNativeHook;

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
    private boolean hangExit = false;
    private long lastContinue;

    Controller() {
        //Getting argument from user
        System.out.println("Please, enter x: ");
        inputReader = new Scanner(System.in);
        x = inputReader.nextInt();

        initListener();

        processes = new HashMap<>();
        result = new HashMap<>();
        container = new FunctionsContainer();
        server = new Server(FunctionsContainer.numberOfFunctions);
        server.start();
    }

    private void initListener() {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            System.out.println("Can't register nativehook.");
        }
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        GlobalScreen.addNativeKeyListener(new EscapeListener());
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
        ResultContainer lastResult;
        lastContinue = System.currentTimeMillis();

        while(!isCancelled && !isResultComputed && !hangExit) {
            try{
                Thread.sleep(10);
            } catch(InterruptedException e) {}


            if(isPromptNeeded && System.currentTimeMillis() - lastContinue > 5000) {
                userChoise = ask();
                switch(userChoise) {
                    case 1:
                        lastContinue = System.currentTimeMillis();
                        break;
                    case 2:
                        isPromptNeeded = false;
                        lastContinue = System.currentTimeMillis();
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
                System.out.println(lastResult.getFunctionName());
                if(!lastResult.isHanging()) {
                    System.out.println(lastResult.getFormattedResult());
                    result.put(lastResult.getFunctionName(), lastResult.getFunctionResult());
                    if (result.size() == FunctionsContainer.numberOfFunctions || lastResult.getFunctionResult() == 0) {
                        isResultComputed = true;
                        if (lastResult.getFunctionResult() == 0) {
                            System.out.println(lastResult.getFunctionName() + " is 0, shutting down other processes");
                            shutProcesses();
                            System.out.println("Final result is 0.");
                            break;
                        } else {
                            System.out.println("All functions returned result. Final result is " + operation());
                            break;
                        }
                    }
                }
                else {
                    hangExit = true;
                    System.out.println("Function " + lastResult.getFunctionName() + " hangs. Shutting down processes.");
                    shutProcesses();
                }
            }
        }
        serverInterrupt();
        if(isCancelled) {
            System.out.println("Cancelled by user.");
        }
        if(hangExit) {
            System.out.println("Cancelled due function hanging.");
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

        try {
            unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    public class EscapeListener implements NativeKeyListener {
        @Override
        public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
        }

        @Override
        public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        }

        @Override
        public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
            if (nativeKeyEvent.getRawCode() == VK_ESCAPE) {
                shutProcesses();
                isCancelled = true;
            }
        }
    }
}
