package de.pcfreak9000.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Arguments {
    
    public static Map<String, String> mapArguments(List<Argument> args, String[] keys) {
        Map<String, String> map = new HashMap<>();
        for (Argument a : args) {
            String s = a.getArgument();
            String[] parts = s.split("[^\\\\]=");
            if (parts.length != 2) {
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
    
    private static boolean contains(String[] ar, String a) {
        for (String g : ar) {
            if (g.equals(a)) {
                return true;
            }
        }
        return false;
    }
}
