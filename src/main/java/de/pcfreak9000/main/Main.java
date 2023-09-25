/*******************************************************************************
 * Copyright (C) 2020 Roman Borris
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.pcfreak9000.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.matheclipse.core.eval.ExprEvaluator;

import de.pcfreak9000.command.BaseCommand;
import picocli.CommandLine;
//FIXME functions with a - in front somehow cause problems when parsing, e.g. set -a -p=linear rr "(-1)*sqrt(R^2-R0^2*(sin(l*pi/180))^2)+R0*cos(l*pi/180)" (-1) works, - doesnt
public class Main {
    
    public static final int CODE_NORMAL = 0;
    public static final int CODE_EXIT = 4;
    public static final int CODE_ERROR = 5;
    
    public static final String SUPPORTED_NUMBER_FORMAT_REGEX = "\\d+(?:[.]\\d+)|\\d+";
    private static ExprEvaluator EXPRESSION_EVALUATOR;
    private static CommandLine commandline;
    public static final Tablets data = new Tablets();
    
    public static ExprEvaluator ev2 = new ExprEvaluator();
    
    public static ExprEvaluator evaluator() {
        if (EXPRESSION_EVALUATOR == null) {
            System.out.println("Initializing...");
            EXPRESSION_EVALUATOR = new ExprEvaluator();
        }
        return EXPRESSION_EVALUATOR;
    }
    
    public static void main(String[] args) {
        evaluator();//Initializing the evaluator takes a second or two
        commandline = new CommandLine(new BaseCommand());
        commandline.setCaseInsensitiveEnumValuesAllowed(true);
        //commandline.setErr(new PrintWriter(System.err));
        if (args.length > 0 && !args[0].isEmpty()) {
            parseFile(args[0]);
        } else {
            System.out.println("Ready. Type -h for help.");
            System.out.print(">> ");
            parseConsole();
        }
    }
    
    private static void parseConsole() {
        Reader reader;
        if (System.console() != null) {
            reader = System.console().reader();
        } else {
            reader = new InputStreamReader(System.in);
        }
        try (Scanner scan = new Scanner(reader)) {
            readScannerLines(scan, true);
        }
    }
    
    public static int parseFile(String name) {
        System.out.println("Trying to read instructions from the file \"" + name + "\"...");
        try (Scanner scan = new Scanner(new FileReader(new File(name)))) {
            return readScannerLines(scan, false);
        } catch (FileNotFoundException e) {
            System.err.println(e.toString());
        }
        return CODE_ERROR;
    }
    
    private static int readScannerLines(Scanner scan, boolean consoleMode) {
        while (scan.hasNextLine()) {
            String line = scan.nextLine().trim();
            String[] parseResult = parse(line);
            if (parseResult == null || parseResult.length == 0) {
                if (consoleMode) {
                    System.out.print(">> ");
                }
                continue;
            }
            int x = commandline.execute(parseResult);
            if (x == CODE_EXIT) {
                //1 means this loop should be exited regularly
                break;
            } else if ((x == CODE_ERROR || x == CommandLine.ExitCode.SOFTWARE || x == CommandLine.ExitCode.USAGE)
                    && !consoleMode) {
                return x;
            }
            if (consoleMode) {
                System.out.print(">> ");
            }
        }
        return Main.CODE_NORMAL;
    }
    
    private static String[] parse(String in) {
        List<String> parts = new ArrayList<>();
        if (in.startsWith("%") || in.startsWith("#")) {//Indicates a comment
            return parts.toArray(String[]::new);
        }
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
            System.err.println("Unclosed '\"'");
            return null;
        }
        return parts.toArray(String[]::new);
    }
    
}
