package eu.doppel_helix.jna.tlbcodegenerator.imp;

import com.sun.jna.platform.win32.COM.ITypeInfo;
import com.sun.jna.platform.win32.COM.TypeInfoUtil;
import com.sun.jna.platform.win32.OaIdl;
import static com.sun.jna.platform.win32.OaIdl.TYPEATTR.TYPEFLAGS_FDISPATCHABLE;
import static com.sun.jna.platform.win32.OaIdl.TYPEATTR.TYPEFLAGS_FDUAL;
import static com.sun.jna.platform.win32.OaIdl.TYPEATTR.TYPEFLAGS_FOLEAUTOMATION;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TlbInterface extends TlbEntry {
    private boolean usedAsSource;
    private boolean usedAsImplementation;
    private final boolean dispatch;
    private final int typeflags;
    private final List<TlbFunctionCall> functions;
    private final List<TlbVariable> variables;

    public TlbInterface(TypeLib tl, int index) {
        super(tl, index);
        
        // Get the TypeAttributes
        TypeInfoUtil typeInfoUtil = tl.getTypeLibUtil().getTypeInfoUtil(index);
        OaIdl.TYPEATTR typeAttr = typeInfoUtil.getTypeAttr();
        
        this.dispatch = typeAttr.typekind.value == OaIdl.TYPEKIND.TKIND_DISPATCH;
        this.typeflags = typeAttr.wTypeFlags.intValue();
        
        List<TlbFunctionCall> functionsBuilder = new LinkedList<>();
        List<TlbVariable> variablesBuilder = new LinkedList<>();
        
        int cFuncs = typeAttr.cFuncs.intValue();
        int cVars = typeAttr.cVars.intValue();
        
        for (int i = 0; i < cFuncs; i++) {
            // Get the function description
            OaIdl.FUNCDESC funcDesc = typeInfoUtil.getFuncDesc(i);

            // Get the member ID
            OaIdl.MEMBERID memberID = funcDesc.memid;

            // Get the name of the method
            TypeInfoUtil.TypeInfoDoc typeInfoDoc2 = typeInfoUtil.getDocumentation(memberID);
            String methodName = typeInfoDoc2.getName();

            if (!isReservedMethod(methodName)) {
                functionsBuilder.add(new TlbFunctionCall(tl, index, funcDesc, typeInfoUtil));
            }

            // Release our function description stuff
            typeInfoUtil.ReleaseFuncDesc(funcDesc);
        }
        
        for (int i = 0; i < cVars; i++) {
            // Get the function description
            OaIdl.VARDESC varDesc = typeInfoUtil.getVarDesc(i);

            // Get the member ID
            OaIdl.MEMBERID memberID = varDesc.memid;

            // Get the name of the method
            TypeInfoUtil.TypeInfoDoc typeInfoDoc2 = typeInfoUtil.getDocumentation(memberID);
            String name = typeInfoDoc2.getName();

            if (!isReservedMethod(name)) {
                variablesBuilder.add(new TlbVariable(tl, index, varDesc, typeInfoUtil));
            }

            // Release our variable description stuff
            typeInfoUtil.ReleaseVarDesc(varDesc);
        }

        functions = Collections.unmodifiableList(functionsBuilder);
        variables = Collections.unmodifiableList(variablesBuilder);
    }

    public List<TlbFunctionCall> getFunctions() {
        return functions;
    }

    public List<TlbVariable> getVariables() {
        return variables;
    }
    
    public List<TlbVariable> getDispatchableVariables() {
        List<TlbVariable> result = new LinkedList<>();
        for(TlbVariable var: variables) {
            if(var.getVarkind().value == OaIdl.VARKIND.VAR_DISPATCH) {
                result.add(var);
            }
        }
        return result;
    }
    
    @Override
    public String toString() {
        return "TlbDispInterface{" + super.toString() + ", functions=" + functions + '}';
    }

    public boolean isUsedAsSource() {
        return usedAsSource;
    }

    void setUsedAsSource(boolean usedAsSource) {
        this.usedAsSource = usedAsSource;
    }

    public boolean isUsedAsImplementation() {
        return usedAsImplementation;
    }

    void setUsedAsImplementation(boolean usedAsImplementation) {
        this.usedAsImplementation = usedAsImplementation;
    }

    public boolean isDispatch() {
        return dispatch;
    }
    
    public boolean isDual() {
        return (typeflags & TYPEFLAGS_FDUAL) == TYPEFLAGS_FDUAL;
    }
    
    public boolean isDispatchable() {
        return (typeflags & TYPEFLAGS_FDISPATCHABLE) == TYPEFLAGS_FDISPATCHABLE;
    }
    
    public boolean isOleautomation() {
        return (typeflags & TYPEFLAGS_FOLEAUTOMATION) == TYPEFLAGS_FOLEAUTOMATION;
    }
}
