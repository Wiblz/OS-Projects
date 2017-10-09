package com.company;

public class ResultContainer {
    private String functionName;
    private int functionResult;

    ResultContainer() {
        this.functionResult = 0;
        this.functionName = "";
    }

    ResultContainer(String functionName, int functionResult) {
        this.functionName = functionName;
        this.functionResult = functionResult;
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
}
