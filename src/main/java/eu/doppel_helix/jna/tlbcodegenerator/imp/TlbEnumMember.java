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

import com.sun.jna.platform.win32.COM.TypeInfoUtil;
import com.sun.jna.platform.win32.OaIdl;
import com.sun.jna.platform.win32.Variant;

public class TlbEnumMember {

    private final String name;
    private final long value;
    private final String documentation;

    public TlbEnumMember(TypeInfoUtil typeInfoUtil, int i) {
        // Get the property description
        OaIdl.VARDESC varDesc = typeInfoUtil.getVarDesc(i);
        Variant.VARIANT constValue = varDesc._vardesc.lpvarValue;
        Object valueString = constValue.getValue();

        // Get the member ID
        OaIdl.MEMBERID memberID = varDesc.memid;

        // Get the name of the property
        TypeInfoUtil.TypeInfoDoc typeInfoDoc2 = typeInfoUtil.getDocumentation(memberID);
        documentation = typeInfoDoc2.getDocString();
        name = typeInfoDoc2.getName();
        value = Long.parseLong(valueString.toString());
        
        // release the pointer
        typeInfoUtil.ReleaseVarDesc(varDesc);
    }

    public String getName() {
        return name;
    }

    public long getValue() {
        return value;
    }

    public String getDocumentation() {
        return documentation;
    }

    @Override
    public String toString() {
        return "TlbEnumMember{" + "name=" + name + ", value=" + value +
                ", documentation=" + documentation + '}';
    }

}
