package de.pcfreak9000.main;

import java.util.HashMap;
import java.util.Map;

public class Tablets {
    
    private Map<String, Tablet> dataTablets;
    
    public Tablets() {
        this.dataTablets = new HashMap<>();
    }
    
    public Tablet getTablet(String name) {
        return dataTablets.get(name);
    }
    
    public Tablet createDataTablet(String name) {
        Tablet t = new DataTablet();
        this.dataTablets.put(name, t);
        return t;
    }
    
    public Tablet createFormulaTablet(String name) {
        Tablet t = new FunctionTablet();
        this.dataTablets.put(name, t);
        return t;
    }
    
    public boolean exists(String name) {
        return dataTablets.containsKey(name);
    }
    
    public void deleteTablet(String argument) {
        this.dataTablets.remove(argument);
    }
    
}
