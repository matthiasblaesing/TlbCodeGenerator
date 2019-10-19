[#ftl]
[#--
Copyright (c) 2016 Matthias Bläsing, All Rights Reserved

The contents of this file is dual-licensed under 2
alternative Open Source/Free licenses: LGPL 2.1 or later and
Apache License 2.0. (starting with JNA version 4.0.0).

You can freely decide which license you want to apply to
the project.

You may obtain a copy of the LGPL License at:

http://www.gnu.org/licenses/licenses.html

A copy is also included in the downloadable source code package
containing JNA, in file "LGPL2.1".

You may obtain a copy of the Apache License at:

http://www.apache.org/licenses/

A copy is also included in the downloadable source code package
containing JNA, in file "AL2.0".
--]
[#macro paramList params][#list params as param]
[#if param?index > 0]            [/#if][#if (param.out)]VARIANT[#else]${typeLib.mapPrimitiveIfExists(param.type)}[/#if] ${param.name}[#sep],
[/#sep][/#list][/#macro]

package ${package};

import com.sun.jna.platform.win32.COM.util.AbstractComEventCallbackListener;
import com.sun.jna.platform.win32.COM.util.annotation.ComEventCallback;
import com.sun.jna.platform.win32.COM.util.annotation.ComInterface;
import com.sun.jna.platform.win32.COM.util.IDispatch;
import com.sun.jna.platform.win32.Variant.VARIANT;

/**
[#if (entry.docString)?has_content] * ${entry.docString!}
 *
[/#if] * <p>uuid(${entry.guid})</p>
 */
public abstract class ${javaName}Handler extends AbstractComEventCallbackListener implements ${javaName} {
    @Override
    public void errorReceivingCallbackEvent(java.lang.String string, java.lang.Exception excptn) {
    }

    [#list entry.functions as function]
        [#assign returnValue=typeLib.mapPrimitiveIfExists(function.returnType)]
    /**
[#if (function.documentation)?has_content]
     * ${function.documentation}
     *
[/#if]
     * <p>id(${fh.formatHex(function.memberId)})</p>
     */
    @Override
    public [#if returnValue != 'void']abstract [/#if]${returnValue} ${fh.prepareProperty(function.methodName, function.property, function.setter)}([@paramList params=function.params/])[#if returnValue != 'void'];[#else]{
    }[/#if]
            
    [/#list]
    
}