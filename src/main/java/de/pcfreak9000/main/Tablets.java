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
    
    public Tablet createFunctionTablet(String name) {
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
