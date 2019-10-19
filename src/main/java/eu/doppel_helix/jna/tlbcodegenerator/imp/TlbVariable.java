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
import com.sun.jna.platform.win32.COM.TypeInfoUtil.TypeInfoDoc;
import com.sun.jna.platform.win32.OaIdl;

public class TlbVariable {
    private final static int VARFLAG_READONLY = 0x1;
    private final static int VARFLAG_FSOURCE = 0x2;
    
    private final String name;
    private final String documentation;
    private final String type;
    private final int wVarFlags;
    private final OaIdl.MEMBERID memberId;
    private final int typeIndex;
    private final OaIdl.VARKIND varkind;

    public TlbVariable(TypeLib tl, int index, OaIdl.VARDESC varDesc, TypeInfoUtil typeInfoUtil) {
        TypeInfoDoc typeInfoDoc = typeInfoUtil.getDocumentation(varDesc.memid);
        name = typeInfoDoc.getName();
        documentation = typeInfoDoc.getDocString();
        wVarFlags = varDesc.wVarFlags.intValue();
        typeIndex = index;
        type = tl.getType(typeInfoUtil, varDesc);
        memberId = varDesc.memid;
        this.varkind = varDesc.varkind;
    }

    public String getName() {
        return name;
    }

    public String getDocumentation() {
        return documentation;
    }

    public OaIdl.MEMBERID getMemberId() {
        return memberId;
    }

    public String getType() {
        return type;
    }
    
    public int getTypeIndex() {
        return typeIndex;
    }

    public boolean isSource() {
        return (wVarFlags & VARFLAG_FSOURCE) > 0;
    }
    
    public boolean isReadonly() {
        return (wVarFlags & VARFLAG_READONLY) > 0;
    }

    public OaIdl.VARKIND getVarkind() {
        return varkind;
    }
}