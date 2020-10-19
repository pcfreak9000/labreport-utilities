package de.pcfreak9000.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Arguments {
    
    public static Map<String, String> mapArguments(List<Argument> args, String[] keys, boolean strict) {
        Map<String, String> map = new HashMap<>();
        for (Argument a : args) {
            String s = a.getArgument();
            String[] parts = s.split("=");
            if (parts.length != 2 && strict) {//TODO meh... problematic
                return null;
                //throw new IllegalArgumentException("Malformed arguments");
            }
            if (keys != null) {
                if (!contains(keys, parts[0])) {
                    return null;
                    //throw new IllegalArgumentException("Aruments not matching keys");
                }
            }
            map.put(parts[0], parts[1]);
        }
        return map;
    }
    
    public static List<Argument> commandOptions(List<Argument> args, String optionPrefix, int commandIndex) {
        int end = commandIndex + 1;
        for (int i = commandIndex + 1; i < args.size(); i++) {
            if (!args.get(i).getArgument().startsWith(optionPrefix)) {
                end = i;
            }
        }
        return args.subList(commandIndex + 1, end);
    }
    
    private static boolean contains(String[] ar, String a) {
        for (String g : ar) {
            if (g.equals(a)) {
                return true;
            }
        }
        return false;
    }
}
