package de.pcfreak9000.main;

import java.util.Arrays;

public class FunctionTablet implements Tablet {
    
    private String function;
    private String[] args;
    
    public String getFunction() {
        return function;
    }
    
    public void setFunction(String function) {
        this.function = function;
    }
    
    public String[] getArgs() {
        return args;
    }
    
    public void setArgs(String... args) {
        this.args = args;
    }
    
    //TODO performance
    public ErrorPropagation createErrorPropagation() {
        return new ErrorPropagation(function, args);
    }
    
}
