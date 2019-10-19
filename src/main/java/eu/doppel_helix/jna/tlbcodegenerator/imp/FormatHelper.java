/* Copyright (c) 2016 Matthias Bläsing, All Rights Reserved
 *
 * The contents of this file is dual-licensed under 2
 * alternative Open Source/Free licenses: LGPL 2.1 or later and
 * Apache License 2.0. (starting with JNA version 4.0.0).
 *
 * You can freely decide which license you want to apply to
 * the project.
 *
 * You may obtain a copy of the LGPL License at:
 *
 * http://www.gnu.org/licenses/licenses.html
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "LGPL2.1".
 *
 * You may obtain a copy of the Apache License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "AL2.0".
 */

package eu.doppel_helix.jna.tlbcodegenerator.imp;

public class FormatHelper {
    public static String replaceJavaKeyword(String name) {
        if (name.equalsIgnoreCase("final")) {
            return "_" + name;
        } else if (name.equalsIgnoreCase("default")) {
            return "_" + name;
        } else if (name.equalsIgnoreCase("case")) {
            return "_" + name;
        } else if (name.equalsIgnoreCase("char")) {
            return "_" + name;
        } else if (name.equalsIgnoreCase("private")) {
            return "_" + name;
        } else if (name.equalsIgnoreCase("class")) {
            return "_" + name;
        } else if (name.equalsIgnoreCase("toString")) {
            return "_" + name;
        } else {
            return name;
        }
    }
    
    public static String prepareProperty(String name, boolean property, boolean setter) {
        if (name == null) {
            return null;
        }
        name = replaceJavaKeyword(name);
        if(! property) {
            return name;
        }
        if (name.length() < 1) {
            return (setter ? "set" : "get") + name.toUpperCase();
        }
        return (setter ? "set" : "get") + name.substring(0, 1).toUpperCase() + name.substring(1);
    }
    
    public static String preparePropertySetter(String name) {
        return prepareProperty(name, true, true);
    }
    
    public static String preparePropertyGetter(String name) {
        return prepareProperty(name, true, false);
    }
    
    public static String formatHex(Integer input) {
        return "0x" + Integer.toHexString(input);
    }
}
