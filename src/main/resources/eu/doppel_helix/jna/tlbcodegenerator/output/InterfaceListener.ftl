[#ftl]
[#macro paramList params][#list params as param]
[#if param?index > 0]            [/#if][#if (param.out)]VARIANT[#else]${typeLib.mapPrimitiveIfExists(param.type)}[/#if] ${param.name}[#sep], [/#sep][/#list][/#macro]

${package}.${javaName} (${entry.guid!}) [SOURCE]
    [#list entry.functions as function]
    ${r"["}memberId=${function.memberId?c}, vtableID=${function.vtableId?c}${r"]"}
    ${typeLib.mapPrimitiveIfExists(function.returnType)} ${fh.prepareProperty(function.methodName, function.property, function.setter)}([@paramList params=function.params/])
    [/#list]