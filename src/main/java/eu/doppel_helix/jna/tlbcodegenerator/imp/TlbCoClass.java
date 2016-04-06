package eu.doppel_helix.jna.tlbcodegenerator.imp;

import com.sun.jna.platform.win32.COM.ITypeInfo;
import com.sun.jna.platform.win32.COM.TypeInfoUtil;
import com.sun.jna.platform.win32.OaIdl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TlbCoClass extends TlbEntry {
    private static final int IMPLTYPEFLAG_FDEFAULT = 0x00000001;
    private static final int IMPLTYPEFLAG_FSOURCE = 0x00000002;
    private static final int IMPLTYPEFLAG_FRESTRICTED = 0x00000001;
    private static final int IMPLTYPEFLAG_FDEFAULTVTABLE = 0x00000008;
    
    private final List<String> interfaces;
    private final List<String> sourceInterfaces;

    public TlbCoClass(TypeLib tl, int index) {
        super(tl, index);

        TypeInfoUtil typeInfoUtil = tl.getTypeLibUtil().getTypeInfoUtil(index);
        
        // Get the TypeAttributes
        OaIdl.TYPEATTR typeAttr = typeInfoUtil.getTypeAttr();
        int cImplTypes = typeAttr.cImplTypes.intValue();
        
        List<String> interfaceBuilder = new ArrayList<>();
        List<String> sourceInterfacesBuilder = new ArrayList<>();

        for (int i = 0; i < cImplTypes; i++) {
            int lImplTypeFlags = typeInfoUtil.getImplTypeFlags(i);
            OaIdl.HREFTYPE refTypeOfImplType = typeInfoUtil.getRefTypeOfImplType(i);
            ITypeInfo refTypeInfo = typeInfoUtil.getRefTypeInfo(refTypeOfImplType);
            TypeInfoUtil refTypeInfoUtil = new TypeInfoUtil(refTypeInfo);
            TypeInfoUtil.TypeInfoDoc documentation = refTypeInfoUtil.getDocumentation(new OaIdl.MEMBERID(-1));
            // Only interfaces that are not marked as source
            if((lImplTypeFlags & IMPLTYPEFLAG_FSOURCE) == 0) {
                if((lImplTypeFlags & IMPLTYPEFLAG_FDEFAULT) == IMPLTYPEFLAG_FDEFAULT) {
                    interfaceBuilder.add(0, documentation.getName());
                } else {
                    interfaceBuilder.add(documentation.getName());
                }
            } else {
                if((lImplTypeFlags & IMPLTYPEFLAG_FDEFAULT) == IMPLTYPEFLAG_FDEFAULT) {
                    sourceInterfacesBuilder.add(0, documentation.getName());
                } else {
                    sourceInterfacesBuilder.add(documentation.getName());
                }
            }
        }
        
        interfaces = Collections.unmodifiableList(interfaceBuilder);
        sourceInterfaces = Collections.unmodifiableList(sourceInterfacesBuilder);
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public List<String> getSourceInterfaces() {
        return sourceInterfaces;
    }
    
    @Override
    public String toString() {
        return "TlbCoClass{" + super.toString() + ", interfaces=" + interfaces + '}';
    }

}
