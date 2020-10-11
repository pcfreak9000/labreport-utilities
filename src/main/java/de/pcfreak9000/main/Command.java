package de.pcfreak9000.main;

import java.util.HashMap;
import java.util.Map;

public class Command {
    
    private Map<String, Command> subcommands;
    
    public Command() {
        subcommands = new HashMap<String, Command>();
    }
    
    public void register(String cmd, Command command) {
        if (cmd.contains("\"")) {
            throw new IllegalArgumentException("'\"' in command");
        }
        if (command == null) {
            throw new NullPointerException("command == null");
        }
        subcommands.put(cmd, command);
    }
    
    public void unregister(String cmd) {
        subcommands.remove(cmd);
    }
    
    public String call(String... strings) {
        if (strings.length > 0) {
            String s = strings[0];
            Command c = subcommands.get(s);
            if (c != null) {
                String[] args = null;
                if (strings.length > 0) {
                    args = new String[strings.length - 1];
                    System.arraycopy(strings, 1, args, 0, args.length);
                }
                return c.call(args);
            }
        }
        return execute(strings);
    }
    
    protected String execute(String... strings) {
        return "Undefined command";
    }
    
}
