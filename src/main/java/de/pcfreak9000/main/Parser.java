package de.pcfreak9000.main;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    
    private Command baseCommand = new Command();
    
    public String parse(String in) {
        List<String> parts = new ArrayList<String>();
        char[] chars = in.toCharArray();
        StringBuilder builder = new StringBuilder();
        boolean inside = false;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '"' && (i <= 1 || chars[i - 1] != '\\')) {
                inside = !inside;
                if (!inside) {
                    String s = builder.toString();
                    if (!s.isEmpty()) {
                        s = s.replace("\\\"", "\"");
                        parts.add(s);
                        builder = new StringBuilder();
                    }
                }
                continue;
            }
            if ((c == ' ' && !inside) || i == chars.length - 1) {
                if (i == chars.length - 1) {
                    builder.append(c);
                }
                String s = builder.toString();
                if (!s.isEmpty()) {
                    s = s.trim();
                    parts.add(s);
                    builder = new StringBuilder();
                }
            } else {
                builder.append(c);
            }
        }
        if (inside) {
            throw new IllegalArgumentException("Unclosed '\"'");
        }
        return baseCommand.call(parts.toArray(new String[parts.size()]));
    }
    
    public Command getBaseCommand() {
        return baseCommand;
    }
}
