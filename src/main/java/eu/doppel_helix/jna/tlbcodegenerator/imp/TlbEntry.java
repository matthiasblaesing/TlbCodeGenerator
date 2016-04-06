
package eu.doppel_helix.jna.tlbcodegenerator.imp;

import com.sun.jna.platform.win32.COM.TypeInfoUtil;
import com.sun.jna.platform.win32.COM.TypeLibUtil;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.OaIdl;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class TlbEntry {
    private final TypeLib typeLib;
    private final String name;
    private final String guid;
    private final String docString;
    protected final int index;

    public TlbEntry(TypeLib typeLib, int index) {
        this.index = index;
        this.typeLib = typeLib;
        
        TypeLibUtil.TypeLibDoc typeLibDoc = typeLib.getTypeLibUtil().getDocumentation(index);
        name = typeLibDoc.getName();
        docString = typeLibDoc.getDocString();
        
        // Get the TypeAttributes
        TypeInfoUtil typeInfoUtil = typeLib.getTypeLibUtil().getTypeInfoUtil(index);
        OaIdl.TYPEATTR typeAttr = typeInfoUtil.getTypeAttr();
        
        if(! typeAttr.guid.dataEquals(Guid.IID_NULL)) {
            guid = typeAttr.guid.toGuidString();
        } else {
            guid = null;
        }
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public String getGuid() {
        return guid;
    }

    public String getDocString() {
        return docString;
    }

    public TypeLib getTypeLib() {
        return typeLib;
    }
    
    /** The iunknown methods. */
    private final static String[] IUNKNOWN_METHODS = { "QueryInterface", "AddRef",
            "Release" };

    /** The idispatch methods. */
    private final static String[] IDISPATCH_METHODS = { "GetTypeInfoCount",
            "GetTypeInfo", "GetIDsOfNames", "Invoke" };
    
    private final static String[] IEnumVARIANT_METHODS = {"_NewEnum"};
    
    private final static Set<String> reservedMethods;
    
    static {
        Set<String> reservedMethodsBuilder = new HashSet<>();

        for (String method : IUNKNOWN_METHODS) {
            reservedMethodsBuilder.add(method.toLowerCase());
        }

        for (String method : IDISPATCH_METHODS) {
            reservedMethodsBuilder.add(method.toLowerCase());
        }

        for (String method : IEnumVARIANT_METHODS) {
            reservedMethodsBuilder.add(method.toLowerCase());
        }
        reservedMethods = Collections.unmodifiableSet(reservedMethodsBuilder);
    }
    
    /**
     * Checks if is reserved method.
     *
     * @param method the method
     * @return true, if is reserved method
     */
    public static boolean isReservedMethod(String method) {
        if(method == null) {
            return false;
        }
        return reservedMethods.contains(method.toLowerCase());
    }

    @Override
    public String toString() {
        return "TlbEntry{" + "name=" + name + ", guid=" + guid + ", docString=" +
                docString + ", index=" + index + '}';
    }
}
