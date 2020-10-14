package de.pcfreak9000.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    
    private static final PrintStream ORIGINAL_SYSOUT = System.out;
    
    private Command baseCommand = new Command(">>", false);
    
    private List<String> history = new ArrayList<>();
    
    public void parseAndResolve(String in) {
        List<Argument> parts = new ArrayList<>();
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
                        parts.add(new Argument(s, false));
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
                    parts.add(new Argument(s, true));
                    builder = new StringBuilder();
                }
            } else {
                builder.append(c);
            }
        }
        if (parts.isEmpty()) {
            return;
        }
        if (inside) {
            throw new IllegalArgumentException("Unclosed '\"'");
        }
        checkForPipe(parts);
        //Well well well, this is not really required, is it? Just look or copy and paste what the console says you just wrote ffs
        if (parts.size() == 1 && parts.get(0).getArgument().startsWith("<")) {
            String s = parts.get(0).getArgument();
            if (s.length() == 1) {
                if (history.size() > 0) {
                    System.out.println(history.get(history.size() - 1));
                } else {
                    System.out.println("History is empty");
                }
            } else {
                String sub = s.substring(1);
                try {
                    int i = Integer.parseInt(sub);
                    if (history.size() - i < 0 || i < 1) {
                        System.out.println("Malformed history command");
                    } else {
                        System.out.println(history.get(history.size() - i));
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Malformed history command");
                }
            }
        } else if (parts.size() == 2 && parts.get(0).getArgument().trim().equals("<")) {
            try {
                int i = Integer.parseInt(parts.get(1).getArgument().trim());
                if (history.size() - i < 0 || i < 1) {
                    System.out.println("Malformed history command");
                } else {
                    System.out.println(history.get(history.size() - i));
                }
            } catch (NumberFormatException e) {
                System.out.println("Malformed history command");
            }
        } else {
            baseCommand.call(parts);
            history.add(in);
        }
        System.setOut(ORIGINAL_SYSOUT);
    }
    
    private void checkForPipe(List<Argument> args) {
        int limit = args.size();
        boolean changeTarget = false;
        for (int i = 0; i < args.size(); i++) {
            Argument a = args.get(i);
            if (a.isDirect() && a.getArgument().equals("|")) {
                limit = i;
                changeTarget = true;
                break;
            }
        }
        if (changeTarget && limit == args.size() - 1) {
            throw new IllegalArgumentException("missing output");
        }
        PrintStream target = ORIGINAL_SYSOUT;
        if (changeTarget) {
            File file = new File(args.get(limit + 1).getArgument());
            try {
                target = new PrintStream(new FileOutputStream(file, true));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        while (args.size() > limit) {
            args.remove(limit);
        }
        System.setOut(target);
    }
    
    public Command getBaseCommand() {
        return baseCommand;
    }
}
