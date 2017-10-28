package com.company;

public class ResultContainer {
    private String functionName;
    private int functionResult;
    boolean hanging;

    ResultContainer() {
        this.functionResult = 0;
        this.functionName = "";
        hanging = false;
    }

    ResultContainer(String functionName, int functionResult, boolean hanging) {
        this.functionName = functionName;
        this.functionResult = functionResult;
        this.hanging = hanging;
    }

    public String getFunctionName() {
        return functionName;
    }

    public int getFunctionResult() {
        return functionResult;
    }

    public String getFormattedResult() {
        return functionName + " result is " + functionResult;
    }

    public boolean isHanging() { return hanging; }
}
