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
[#macro paramList params][#list params as param]
[#if param?index > 0]            [/#if][#if (param.out)]VARIANT[#else]${typeLib.mapPrimitiveIfExists(param.javaType)} /* ${typeLib.mapPrimitiveIfExists(param.type)} */[/#if] ${param.name}[#sep], [/#sep][/#list][/#macro]

${package}.${javaName} (${entry.guid!})
    [#list entry.functions as function]
    ${r"["}memberId=${function.memberId?c}, vtableID=${function.vtableId?c}${r"]"}
    ${typeLib.mapPrimitiveIfExists(function.returnType)} ${fh.prepareProperty(function.methodName, function.property, function.setter)}([@paramList params=function.params/])
    [/#list]