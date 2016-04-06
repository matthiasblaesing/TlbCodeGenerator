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
