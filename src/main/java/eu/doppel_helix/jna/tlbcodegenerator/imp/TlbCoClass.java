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

import com.sun.jna.platform.win32.COM.ITypeInfo;
import com.sun.jna.platform.win32.COM.TypeInfoUtil;
import com.sun.jna.platform.win32.OaIdl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
