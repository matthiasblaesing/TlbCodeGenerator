[#ftl]
[#--
Copyright (c) 2016 Matthias Bläsing, All Rights Reserved

The contents of this file is dual-licensed under 2
alternative Open Source/Free licenses: LGPL 2.1 or later and
Apache License 2.0.

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

package ${package};

import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.util.IComEventCallbackCookie;
import com.sun.jna.platform.win32.COM.util.IComEventCallbackListener;
import com.sun.jna.platform.win32.COM.util.IConnectionPoint;
import com.sun.jna.platform.win32.COM.util.IUnknown;
import com.sun.jna.platform.win32.COM.util.annotation.ComObject;
import com.sun.jna.platform.win32.COM.util.IRawDispatchHandle;

/**
[#if (entry.docString)?has_content] * ${entry.docString!}
 *
[/#if] * <p>uuid(${entry.guid})</p>
[#list entry.sourceInterfaces as iface]
 * <p>source(${fh.replaceJavaKeyword(iface)})</p>
[/#list]
[#list interfaces as iface]
 * <p>interface(${fh.replaceJavaKeyword(iface)})</p>
[/#list]
 */
@ComObject(clsId = "${entry.guid}")
public interface ${javaName} extends IUnknown[#list interfaces as iface]
    ,${fh.replaceJavaKeyword(iface)}[/#list]
{

}