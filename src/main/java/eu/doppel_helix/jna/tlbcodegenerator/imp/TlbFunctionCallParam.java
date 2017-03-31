
package eu.doppel_helix.jna.tlbcodegenerator.imp;

import com.sun.jna.platform.win32.COM.TypeInfoUtil;
import com.sun.jna.platform.win32.OaIdl;

public class TlbFunctionCallParam {

    private static final int PARAMFLAG_NONE = 0;
    private static final int PARAMFLAG_FIN = 0x1;
    private static final int PARAMFLAG_FOUT = 0x2;
    private static final int PARAMFLAG_FLCID = 0x4;
    private static final int PARAMFLAG_FRETVAL = 0x8;
    private static final int PARAMFLAG_FOPT = 0x10;
    private static final int PARAMFLAG_FHASDEFAULT = 0x20;
    private static final int PARAMFLAG_FHASCUSTDATA = 0x40;

    private final String type;
    private final String name;
    private final int paramFlags;

    public TlbFunctionCallParam(TypeLib tl, TypeInfoUtil typeInfoUtil, OaIdl.FUNCDESC funcDesc, int paramIndex, String name) {
        OaIdl.ELEMDESC elemdesc = funcDesc.lprgelemdescParam.elemDescArg[paramIndex];
        this.paramFlags = elemdesc._elemdesc.paramdesc.wParamFlags.intValue();
        this.type = tl.getType(typeInfoUtil, elemdesc);
        this.name = name;
    }

    public String getType() {
        return type;
    }
    
    public String getJavaType() {
        if(isOptional()) {
            return "Object";
        } else {
            return getType();
        }
    }

    public String getName() {
        return name;
    }

    public boolean isOut() {
        return (paramFlags & PARAMFLAG_FOUT) > 0;
    }

    public boolean isIn() {
        return (paramFlags & PARAMFLAG_FIN) > 0;
    }

    public boolean isOptional() {
        return (paramFlags & PARAMFLAG_FOPT) > 0;
    }
    
    @Override
    public String toString() {
        return "TlbFunctionCallParam{" + "type=" + type + ", name=" + name + '}';
    }
}
