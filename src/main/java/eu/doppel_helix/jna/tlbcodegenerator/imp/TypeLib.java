
package eu.doppel_helix.jna.tlbcodegenerator.imp;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.ITypeInfo;
import com.sun.jna.platform.win32.COM.TypeInfoUtil;
import com.sun.jna.platform.win32.COM.TypeLibUtil;
import com.sun.jna.platform.win32.COM.util.IDispatch;
import com.sun.jna.platform.win32.COM.util.IUnknown;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.OaIdl;
import com.sun.jna.platform.win32.Variant;
import static com.sun.jna.platform.win32.Variant.VT_ARRAY;
import static com.sun.jna.platform.win32.Variant.VT_BLOB_OBJECT;
import static com.sun.jna.platform.win32.Variant.VT_BOOL;
import static com.sun.jna.platform.win32.Variant.VT_BSTR;
import static com.sun.jna.platform.win32.Variant.VT_BYREF;
import static com.sun.jna.platform.win32.Variant.VT_CARRAY;
import static com.sun.jna.platform.win32.Variant.VT_CF;
import static com.sun.jna.platform.win32.Variant.VT_CLSID;
import static com.sun.jna.platform.win32.Variant.VT_CY;
import static com.sun.jna.platform.win32.Variant.VT_DATE;
import static com.sun.jna.platform.win32.Variant.VT_DECIMAL;
import static com.sun.jna.platform.win32.Variant.VT_DISPATCH;
import static com.sun.jna.platform.win32.Variant.VT_EMPTY;
import static com.sun.jna.platform.win32.Variant.VT_ERROR;
import static com.sun.jna.platform.win32.Variant.VT_FILETIME;
import static com.sun.jna.platform.win32.Variant.VT_HRESULT;
import static com.sun.jna.platform.win32.Variant.VT_I1;
import static com.sun.jna.platform.win32.Variant.VT_I2;
import static com.sun.jna.platform.win32.Variant.VT_I4;
import static com.sun.jna.platform.win32.Variant.VT_I8;
import static com.sun.jna.platform.win32.Variant.VT_ILLEGAL;
import static com.sun.jna.platform.win32.Variant.VT_INT;
import static com.sun.jna.platform.win32.Variant.VT_INT_PTR;
import static com.sun.jna.platform.win32.Variant.VT_LPSTR;
import static com.sun.jna.platform.win32.Variant.VT_LPWSTR;
import static com.sun.jna.platform.win32.Variant.VT_NULL;
import static com.sun.jna.platform.win32.Variant.VT_PTR;
import static com.sun.jna.platform.win32.Variant.VT_R4;
import static com.sun.jna.platform.win32.Variant.VT_R8;
import static com.sun.jna.platform.win32.Variant.VT_RECORD;
import static com.sun.jna.platform.win32.Variant.VT_RESERVED;
import static com.sun.jna.platform.win32.Variant.VT_SAFEARRAY;
import static com.sun.jna.platform.win32.Variant.VT_STORAGE;
import static com.sun.jna.platform.win32.Variant.VT_STORED_OBJECT;
import static com.sun.jna.platform.win32.Variant.VT_STREAM;
import static com.sun.jna.platform.win32.Variant.VT_STREAMED_OBJECT;
import static com.sun.jna.platform.win32.Variant.VT_UI1;
import static com.sun.jna.platform.win32.Variant.VT_UI2;
import static com.sun.jna.platform.win32.Variant.VT_UI4;
import static com.sun.jna.platform.win32.Variant.VT_UI8;
import static com.sun.jna.platform.win32.Variant.VT_UINT;
import static com.sun.jna.platform.win32.Variant.VT_UINT_PTR;
import static com.sun.jna.platform.win32.Variant.VT_UNKNOWN;
import static com.sun.jna.platform.win32.Variant.VT_USERDEFINED;
import static com.sun.jna.platform.win32.Variant.VT_VARIANT;
import static com.sun.jna.platform.win32.Variant.VT_VECTOR;
import static com.sun.jna.platform.win32.Variant.VT_VERSIONED_STREAM;
import static com.sun.jna.platform.win32.Variant.VT_VOID;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class TypeLib {

    private static final Logger LOG = Logger.getLogger(TypeLib.class.getName());
    
    private static final Set<String> basicTypes;
    
    static {
        Set<String> basicTypesBuilder = new HashSet<>();
        basicTypesBuilder.addAll(Arrays.asList(
                "byte", "short", "int", "long", "float", "double", "boolean", "char",
                "Byte", "Short", "Integer", "Long", "Float", "Double", "Boolean", "Character",
                "String"
        ));
        basicTypes = Collections.unmodifiableSet(basicTypesBuilder);
    }
    
    private final Map<String,TlbEntry> entries;
    private final Map<String,String> primitives;
    private final String name;
    private final String docString;
    private final String uuid;
    private final int majorVersion;
    private final int minorVersion;
    private final TypeLibUtil typeLibUtil;
    
    public TypeLib(String clsid, int majorVersion, int minorVersion) {
        this(new TypeLibUtil(clsid, majorVersion, minorVersion));
    }
    
    public TypeLib(String file) {
        this(new TypeLibUtil(file));
    }
    
    private TypeLib(TypeLibUtil typeLibUtil) {
        this.typeLibUtil = typeLibUtil;
        name = typeLibUtil.getName();
        majorVersion = typeLibUtil.getLibAttr().wMajorVerNum.intValue();
        minorVersion = typeLibUtil.getLibAttr().wMinorVerNum.intValue();
        uuid = typeLibUtil.getLibAttr().guid.toGuidString();
        docString = typeLibUtil.getDocString();
        
        Map<String, TlbEntry> entriesBuilder = new HashMap<>();
        Map<String,String> primitivesBuilder = new HashMap<>();

        int typeInfoCount = typeLibUtil.getTypeInfoCount();
        for (int i = 0; i < typeInfoCount; ++i) {
            OaIdl.TYPEKIND typekind = typeLibUtil.getTypeInfoType(i);
            String name = typeLibUtil.getDocumentation(i).getName();
            switch (typekind.value) {
                case OaIdl.TYPEKIND.TKIND_ENUM:
                    entriesBuilder.put(name, new TlbEnum(this, i));
                    break;
                case OaIdl.TYPEKIND.TKIND_RECORD:
                    LOG.fine("'TKIND_RECORD' objects are currently not supported! => " + name);
                    break;
                case OaIdl.TYPEKIND.TKIND_MODULE:
                    LOG.fine("'TKIND_MODULE' objects are currently not supported! => " + name);
                    break;
                case OaIdl.TYPEKIND.TKIND_INTERFACE:
                    entriesBuilder.put(name, new TlbInterface(this, i));
                    break;
                case OaIdl.TYPEKIND.TKIND_DISPATCH:
                    entriesBuilder.put(name, new TlbInterface(this, i));
                    break;
                case OaIdl.TYPEKIND.TKIND_COCLASS:
                    entriesBuilder.put(name, new TlbCoClass(this, i));
                    break;
                case OaIdl.TYPEKIND.TKIND_ALIAS:
                    entriesBuilder.put(name, new TlbAlias(this, i));
                    break;
                case OaIdl.TYPEKIND.TKIND_UNION:
                    LOG.fine("'TKIND_UNION' objects are currently not supported! => " + name);
                    break;
                default:
                    break;
            }
        }
        
        for(TlbEntry ent: entriesBuilder.values()) {
            if(ent instanceof TlbCoClass) {
                TlbCoClass entCoClass = (TlbCoClass) ent;
                for(String iface: entCoClass.getInterfaces()) {
                    TlbEntry targetEntry = entriesBuilder.get(iface);
                    if(targetEntry instanceof TlbInterface) {
                        ((TlbInterface) targetEntry).setUsedAsImplementation(true);
                    }
                }
                for(String iface: entCoClass.getSourceInterfaces()) {
                    TlbEntry targetEntry = entriesBuilder.get(iface);
                    if(targetEntry instanceof TlbInterface) {
                        ((TlbInterface) targetEntry).setUsedAsSource(true);
                    }
                }
            } else if (ent instanceof TlbAlias) {
                TlbAlias alias = (TlbAlias) ent;
                if(basicTypes.contains(alias.getReferencedType())) {
                    primitivesBuilder.put(alias.getName(), alias.getReferencedType());
                }
            }
        }
        
        entries = Collections.unmodifiableMap(entriesBuilder);
        primitives = Collections.unmodifiableMap(primitivesBuilder);
    }

    public String getName() {
        return name;
    }

    public String getDocString() {
        return docString;
    }
    
    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public String getUUID() {
        return uuid;
    }
    
    public Map<String, TlbEntry> getEntries() {
        return entries;
    }
    
    public TlbEntry getEntry(String name) {
        return entries.get(name);
    }
    
    public String mapPrimitiveIfExists(String name) {
        String result = primitives.get(name);
        if(result == null){
            return name;
        } else {
            return result;
        }
    }

    public TypeLibUtil getTypeLibUtil() {
        return typeLibUtil;
    }

    /**
     * Gets the var type.
     *
     * @param vt the vt
     * @return the var type
     */
    String getVarType(WTypes.VARTYPE vt) {
        switch (vt.intValue()) {
            case VT_EMPTY:
                return "";
            case VT_NULL:
                return "null";
            case VT_I1:
            case VT_UI1:
                return "Byte";
            case VT_I2:
                return "Short";
            case VT_UI2:
                return "Character";
            case VT_UINT:
            case VT_INT:
            case VT_UI4:
            case VT_I4:
                return "Integer";
            case VT_I8:
            case VT_UI8:
                return "Long";
            case VT_R4:
                return "Float";
            case VT_R8:
                return "Double";
            case VT_CY:
                return OaIdl.CURRENCY.class.getCanonicalName();
            case VT_DATE:
                return "java.util.Date";
            case VT_BSTR:
                return "String";
            case VT_DISPATCH:
                return IDispatch.class.getCanonicalName();
            case VT_ERROR:
                return WinDef.SCODE.class.getCanonicalName();
            case VT_BOOL:
                return "Boolean";
            case VT_VARIANT:
                return "Object";
            case VT_UNKNOWN:
                return IUnknown.class.getCanonicalName();
            case VT_DECIMAL:
                return OaIdl.DECIMAL.class.getCanonicalName();
            case VT_VOID:
                return "void";
            case VT_HRESULT:
                return WinNT.HRESULT.class.getCanonicalName();
            case VT_PTR:
                return Pointer.class.getCanonicalName();
            case VT_SAFEARRAY:
                return "safearray";
            case VT_CARRAY:
                return "carray";
            case VT_USERDEFINED:
                return "userdefined";
            case VT_LPSTR:
                return WTypes.LPSTR.class.getCanonicalName();
            case VT_LPWSTR:
                return WTypes.LPWSTR.class.getCanonicalName();
            case VT_RECORD:
                return "record";
            case VT_INT_PTR:
                return WinDef.INT_PTR.class.getCanonicalName();
            case VT_UINT_PTR:
                return WinDef.UINT_PTR.class.getCanonicalName();
            case VT_FILETIME:
                return WinBase.FILETIME.class.getCanonicalName();
            case VT_STREAM:
                return "steam";
            case VT_STORAGE:
                return "storage";
            case VT_STREAMED_OBJECT:
                return "steamed_object";
            case VT_STORED_OBJECT:
                return "stored_object";
            case VT_BLOB_OBJECT:
                return "blob_object";
            case VT_CF:
                return "cf";
            case VT_CLSID:
                return Guid.CLSID.class.getCanonicalName();
            case VT_VERSIONED_STREAM:
                return "";
            // case VT_BSTR_BLOB:
            // return "";
            case VT_VECTOR:
                return "";
            case VT_ARRAY:
                return "SAFEARRAY";
            case VT_BYREF:
                return WinDef.PVOID.class.getCanonicalName();
            case VT_RESERVED:
                return "";
            case VT_ILLEGAL:
                return "illegal";
            /*
             * case VT_ILLEGALMASKED: return "illegal_masked"; case VT_TYPEMASK:
             * return "typemask";
             */
            default:
                return null;
        }
    }

    String getUserdefinedType(TypeInfoUtil tiu, OaIdl.HREFTYPE hreftype) {
        ITypeInfo refTypeInfo = tiu.getRefTypeInfo(hreftype);
        TypeInfoUtil reftiu = new TypeInfoUtil(refTypeInfo);
        TypeInfoUtil.TypeInfoDoc documentation = reftiu
                .getDocumentation(OaIdl.MEMBERID_NIL);
        OaIdl.TYPEATTR refTypeAttr = reftiu.getTypeAttr();
        if (OaIdl.TYPEKIND.TKIND_ALIAS == refTypeAttr.typekind.value) {
            OaIdl.TYPEDESC referenced = reftiu.getTypeAttr().tdescAlias;
            return getType(reftiu, referenced);
        } else {
//            PointerByReference pbr = new PointerByReference();
//            HRESULT hr = reftiu.GetContainingTypeLib().getTypeLib().GetLibAttr(pbr);
//            if (COMUtils.SUCCEEDED(hr)) {
//                OaIdl.TLIBATTR tlib = new OaIdl.TLIBATTR(pbr.getValue());
//                try {
//                    if(tlib.guid.toGuidString().equals(getUUID()) 
//                            && tlib.wMajorVerNum.intValue() == getMajorVersion() 
//                            && tlib.wMinorVerNum.intValue() == getMinorVersion()) {
                        return documentation.getName();
//                    } else {
//                        return String.format("{%s-%d-%d}.%s",
//                                tlib.guid.toGuidString(),
//                                tlib.wMajorVerNum.intValue(),
//                                tlib.wMinorVerNum.intValue(),
//                                documentation.getName());
//                    }
//                } finally {
//                    Auskommentiert -- leaked so memory
//                    tiu.GetContainingTypeLib().getTypeLib().ReleaseTLibAttr(tlib);
//                }
//            } else {
//                return documentation.getName();
//            }
        }
    }

    String getType(TypeInfoUtil tiu, OaIdl.VARDESC vardesc) {
        OaIdl.ELEMDESC elemDesc = vardesc.elemdescVar;
        return getType(tiu, elemDesc);
    }
    
    String getType(TypeInfoUtil tiu, OaIdl.FUNCDESC funcDesc) {
        OaIdl.ELEMDESC elemDesc = funcDesc.elemdescFunc;
        return getType(tiu, elemDesc);
    }

    String getType(TypeInfoUtil tiu, OaIdl.ELEMDESC elemDesc) {
        OaIdl.TYPEDESC _typeDesc = elemDesc.tdesc;
        return getType(tiu, _typeDesc);
    }

    String getType(TypeInfoUtil tiu, OaIdl.TYPEDESC typeDesc) {
        WTypes.VARTYPE vt = typeDesc.vt;
        String type = "not_defined";

        if (vt.intValue() == Variant.VT_PTR) {
            OaIdl.TYPEDESC lptdesc = typeDesc._typedesc.getLptdesc();
            type = getType(tiu, lptdesc);
            if("void".equals(type)) {
                type="Object";
            }
        } else if (vt.intValue() == Variant.VT_SAFEARRAY
                || vt.intValue() == Variant.VT_CARRAY) {
            OaIdl.TYPEDESC tdescElem = typeDesc._typedesc.getLpadesc().tdescElem;
            type = getType(tiu, tdescElem);
        } else if (vt.intValue() == Variant.VT_USERDEFINED) {
            OaIdl.HREFTYPE hreftype = typeDesc._typedesc.hreftype;
            type = getUserdefinedType(tiu, hreftype);
        } else {
            type = getVarType(vt);
        }

        return type;
    }
}
