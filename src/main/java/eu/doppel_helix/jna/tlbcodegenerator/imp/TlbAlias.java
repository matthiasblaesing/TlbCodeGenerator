package eu.doppel_helix.jna.tlbcodegenerator.imp;

import com.sun.jna.platform.win32.COM.TypeInfoUtil;
import com.sun.jna.platform.win32.OaIdl;

public class TlbAlias extends TlbEntry {
    private final OaIdl.TYPEDESC referenced;
    private final TypeInfoUtil tiu;

    public TlbAlias(TypeLib tl, int index) {
        super(tl, index);
        tiu = tl.getTypeLibUtil().getTypeInfoUtil(index);
        referenced = tiu.getTypeAttr().tdescAlias;
    }

    public String getReferencedType() {
        return getTypeLib().getType(tiu, referenced);
    }

    @Override
    public String toString() {
        return "TlbAlias{" + "referenced=" + referenced + ", tiu=" + tiu + '}';
    }
}
