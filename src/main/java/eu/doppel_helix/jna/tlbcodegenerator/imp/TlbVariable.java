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