package de.pcfreak9000.main;

import static org.junit.Assert.*;

import org.junit.Test;

import de.pcfreak9000.main.MathStuff;

public class ErrorPropagationTest {
    
    @Test
    public void test() {
        String[] vars = { "x", "t" };
        String linear = MathStuff.linearErrorPropagation("F = (x^2-t^2)/(x^2+t^2)", vars);
        String gauss = MathStuff.gaussianErrorPropagation("F = (x^2-t^2)/(x^2+t^2)", vars);
        assertEquals("abs((-2*x*(-t^2+x^2))/(t^2+x^2)^2+(2*x)/(t^2+x^2)) * Dx + abs((-2*t*(-t^2+x^2))/(t^2+x^2)^2+(-2*t)/(t^2+x^2)) * Dt", linear);
        assertEquals("sqrt(((-2*x*(-t^2+x^2))/(t^2+x^2)^2+(2*x)/(t^2+x^2) * Dx)^2 + ((-2*t*(-t^2+x^2))/(t^2+x^2)^2+(-2*t)/(t^2+x^2) * Dt)^2)", gauss);
    }
    
}
