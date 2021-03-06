package com.company;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FunctionsContainer {

    public static int numberOfFunctions = 3;
    private final Map<String, FunctionInterface> availableFunctions;

    FunctionsContainer() {
        availableFunctions = new HashMap<>();
        availableFunctions.put("f", new FunctionInterface() {
            @Override
            public int calculate(int x) throws InterruptedException{
                Thread.sleep(4000);
                return x;
            }
        });
        availableFunctions.put("g", new FunctionInterface() {
            @Override
            public int calculate(int x) throws InterruptedException {
                Thread.sleep(1000);
                return x*10;
            }
        });
        availableFunctions.put("h", new FunctionInterface() {
            @Override
            public int calculate(int x) throws InterruptedException {
                Thread.sleep(100000);
                return x-10;
            }
        });
    }

    public Set<Map.Entry<String, FunctionInterface>> getAvailableFunctionsEntrySet() {
            return availableFunctions.entrySet();
    }

    public Map<String, FunctionInterface> getAvailableFunctions() {
        return availableFunctions;
    }
}
