[#ftl]
[#macro paramList params][#list params as param]
[#if param?index > 0]            [/#if][#if (param.out)]VARIANT[#else]${typeLib.mapPrimitiveIfExists(param.type)}[/#if] ${param.name}[#sep],
[/#sep][/#list][/#macro]

package ${package};

import com.sun.jna.platform.win32.COM.util.annotation.ComEventCallback;
import com.sun.jna.platform.win32.COM.util.annotation.ComInterface;
import com.sun.jna.platform.win32.COM.util.IDispatch;
import com.sun.jna.platform.win32.Variant.VARIANT;

/**
[#if (entry.docString)?has_content] * ${entry.docString!}
 *
[/#if] * <p>uuid(${entry.guid})</p>
 */
@ComInterface(iid="${entry.guid}")
public interface ${javaName} {
    [#list entry.functions as function]
        [#assign returnValue=typeLib.mapPrimitiveIfExists(function.returnType)]
    /**
[#if (function.documentation)?has_content]
     * ${function.documentation}
     *
[/#if]
     * <p>id(${fh.formatHex(function.memberId)})</p>
     */
    @ComEventCallback(dispid = ${fh.formatHex(function.memberId)})
    default ${returnValue} ${fh.prepareProperty(function.methodName, function.property, function.setter)}([@paramList params=function.params/]){
        [#if returnValue == 'com.sun.jna.platform.win32.WinNT.HRESULT']
        return new com.sun.jna.platform.win32.WinNT.HRESULT(com.sun.jna.platform.win32.WinError.E_NOTIMPL);
        [/#if]
        [#if returnValue == 'Boolean']
        return false;
        [/#if]
    };
            
    [/#list]
    
}