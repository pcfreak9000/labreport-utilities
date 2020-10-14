package de.pcfreak9000.command;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Command {
    
    private Map<String, Command> subcommands;
    private String command;
    
    private boolean helpshowname;
    
    public Command(String command) {
        this(command, true);
    }
    
    public Command(String command, boolean helpshowname) {
        if (command.contains("\"")) {
            throw new IllegalArgumentException("'\"' in command");
        }
        if (command.isEmpty()) {
            throw new IllegalArgumentException("Command is empty");
        }
        this.helpshowname = helpshowname;
        this.command = command;
        subcommands = new HashMap<String, Command>();
    }
    
    public void register(Command command) {
        if (command == null) {
            throw new NullPointerException("command == null");
        }
        subcommands.put(command.getName(), command);
    }
    
    public void unregister(Command cmd) {
        if (command == null) {
            throw new NullPointerException("command == null");
        }
        subcommands.remove(cmd.getName());
    }
    
    public String getName() {
        return command;
    }
    
    public void call(List<Argument> args) {
        if (args.size() > 0) {
            Argument arg = args.get(0);
            if (arg.isDirect() && arg.getArgument().equals("help")) {
                help0();
                return;
            }
            Command c = subcommands.get(arg.getArgument());
            if (c != null) {
                c.call(args.subList(1, args.size()));
                return;
            }
        }
        execute(args);
    }
    
    protected void execute(List<Argument> args) {
        if (args.size() > 0) {
            System.out.println("Unknown command: " + args.get(0).getArgument());
            Collection<String> possible = subcommands.keySet().stream()
                    .filter((s) -> s.startsWith(args.get(0).getArgument())).collect(Collectors.toList());
            if (!possible.isEmpty()) {
                System.out.println("Maybe you meant to use one of the following: ");
                System.out.println(possible.stream().collect(Collectors.joining(", ")));
            }
            System.out.println();
            System.out.println("Use 'help' for general help. Use '<command> help' to get help for a (sub)command. Commands are cAsE sensitive.");
        } else {
            help0();
            //System.out.println("Unknown command.");
        }
        //help0();
    }
    
    private void help0() {
        System.out.println("======= Help" + (helpshowname ? (" for '" + command + "' =======") : " ======="));
        if (!subcommands.isEmpty()) {
            System.out.println("Available (sub)commands:");
            System.out.println(subcommands.keySet().stream().collect(Collectors.joining(", ")));
            System.out.println();
            System.out.println("Use 'help' for general help. Use '<command> help' to get help for a (sub)command. Commands are cAsE sensitive.");
        }
        help();
    }
    
    protected void help() {
        if (subcommands.isEmpty()) {
            System.out.println("There is no help.");
        }
    }
    
}
