
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
