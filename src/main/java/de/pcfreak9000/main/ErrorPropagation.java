package de.pcfreak9000.main;

import org.matheclipse.core.interfaces.IExpr;

public class ErrorPropagation {
    
    public static enum PropagationType {
        Linear, Gaussian
    }
    
    private String function;
    private String fheader;
    private String[] vars;
    
    private String[] partialDerivatives;
    
    public ErrorPropagation(String function, String... args) {
        this.function = prepareFunction(function);
        this.fheader = getHeader(function);
        this.vars = args;
        this.partialDerivatives = new String[args.length];
        calculatePartialDerivatives();
    }
    
    public String getErrorPropFunction(PropagationType type) {
        return "D" + fheader + " = " + calculateErrorPropagation(type);
    }
    
    private String calculateErrorPropagation(PropagationType type) {
        StringBuilder b = new StringBuilder();
        switch (type) {
        case Gaussian:
            b.append("sqrt(");
            for (int i = 0; i < this.vars.length; i++) {
                b.append("(" + partialDerivatives[i] + " * \"D" + vars[i] + "\")^2"
                        + (i == vars.length - 1 ? ")" : " + "));
            }
            break;
        case Linear:
            for (int i = 0; i < this.vars.length; i++) {
                b.append("abs(" + partialDerivatives[i] + ")" + " * \"D" + vars[i] + "\""
                        + (i == vars.length - 1 ? "" : " + "));
            }
            break;
        default:
            throw new IllegalStateException(type + "");
        }
        return Main.evaluator().eval(b.toString()).toString();
    }
    
    private String getHeader(String function) {
        String f = function.split("=")[0].trim();
        if (f.contains("(")) {
            f = f.split("(")[0].trim();
        }
        return f;
    }
    
    private String prepareFunction(String function, String... args) {
        if (!function.contains("=")) {
            return "f = " + function;
        }
        return function;
    }
    
    private void calculatePartialDerivatives() {
        for (int i = 0; i < partialDerivatives.length; i++) {
            IExpr jv = Main.evaluator().eval("D(" + function + ", " + vars[i] + ")");
            partialDerivatives[i] = jv.toString();
        }
    }
}
