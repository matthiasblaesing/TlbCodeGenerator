package eu.doppel_helix.jna.tlbcodegenerator.imp;

import com.sun.jna.platform.win32.COM.TypeInfoUtil;
import com.sun.jna.platform.win32.COM.TypeLibUtil;
import com.sun.jna.platform.win32.OaIdl;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TlbEnum extends TlbEntry {

    private final List<TlbEnumMember> members;
    
    public TlbEnum(TypeLib typeLib, int index) {
        super(typeLib, index);
        
        List<TlbEnumMember> membersBuilder = new LinkedList<>();
        
        TypeInfoUtil typeInfoUtil = typeLib.getTypeLibUtil().getTypeInfoUtil(index);
        OaIdl.TYPEATTR typeAttr = typeInfoUtil.getTypeAttr();
        int cVars = typeAttr.cVars.intValue();
        for (int i = 0; i < cVars; i++) {
            membersBuilder.add(new TlbEnumMember(typeInfoUtil, i));
        }
        
        members = Collections.unmodifiableList(membersBuilder);
    }

    public List<TlbEnumMember> getMembers() {
        return members;
    }

    @Override
    public String toString() {
        return "TlbEnum{" + super.toString() + ", members=" + members + '}';
    } 
}
