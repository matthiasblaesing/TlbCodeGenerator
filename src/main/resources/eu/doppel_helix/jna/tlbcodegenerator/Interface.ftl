[#ftl]
[#macro paramList params][#list params as param]
[#if param?index > 0]            [/#if][#if (param.out)]VARIANT[#else]${typeLib.mapPrimitiveIfExists(param.type)}[/#if] ${param.name}[#sep],
[/#sep][/#list][/#macro]

package ${package};

import com.sun.jna.platform.win32.COM.util.annotation.ComInterface;
import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;
import com.sun.jna.platform.win32.COM.util.annotation.ComProperty;
import com.sun.jna.platform.win32.COM.util.IDispatch;
import com.sun.jna.platform.win32.COM.util.IUnknown;
import com.sun.jna.platform.win32.COM.util.IRawDispatchHandle;
import com.sun.jna.platform.win32.Variant.VARIANT;

/**
[#if (entry.docString)?has_content] * ${entry.docString!}
 *
[/#if] * <p>uuid(${entry.guid})</p>
 */
@ComInterface(iid="${entry.guid}")
public interface ${javaName} extends IUnknown, IRawDispatchHandle[#if (entry.dual || entry.dispatch || entry.dispatchable)], IDispatch[/#if] {
    [#list entry.functions as function]
    /**
[#if (function.documentation)?has_content]
     * ${function.documentation}
     *
[/#if]
     * <p>id(${fh.formatHex(function.memberId)})</p>
[#if (function.vtableId)?has_content]
     * <p>vtableId(${function.vtableId})</p>
[/#if]
     */
    [#if function.property]@ComProperty[#else]@ComMethod[/#if](name = "${function.methodName}", dispId = ${fh.formatHex(function.memberId)})
    ${typeLib.mapPrimitiveIfExists(function.returnType)} ${fh.prepareProperty(function.methodName, function.property, function.setter)}([@paramList params=function.params/]);
            
    [/#list]
    
    [#list entry.dispatchableVariables as variable]
    /**
[#if (variable.documentation)?has_content]
     * ${variable.documentation}
     *
[/#if]
     * <p>id(${fh.formatHex(variable.memberId)})</p>
     */
    @ComProperty(name = "${variable.name}", dispId = ${fh.formatHex(variable.memberId)})
    ${typeLib.mapPrimitiveIfExists(variable.type)} ${fh.preparePropertyGetter(variable.name)}();

[#if ! variable.readonly]
    /**
[#if (variable.documentation)?has_content]
     * ${variable.documentation}
     *
[/#if]
     * <p>id(${fh.formatHex(variable.memberId)})</p>
     */
    @ComProperty(name = "${variable.name}", dispId = ${fh.formatHex(variable.memberId)})
    void ${fh.preparePropertySetter(variable.name)}(${typeLib.mapPrimitiveIfExists(variable.type)} value);

[/#if]
    [/#list]
}