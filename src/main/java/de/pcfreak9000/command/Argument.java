package de.pcfreak9000.command;

public class Argument {
    private String argument;
    private boolean direct;
    
    public Argument(String arg, boolean direct) {
        this.argument = arg;
        this.direct = direct;
    }
    
    public String getArgument() {
        return argument;
    }
    
    public boolean isDirect() {
        return direct;
    }
}
