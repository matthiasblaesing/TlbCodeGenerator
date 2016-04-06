[#ftl]

${package}.${javaName} (${entry.guid})
[#list entry.sourceInterfaces as iface]
    Source(${fh.replaceJavaKeyword(iface)})
[/#list]
[#list interfaces as iface]
    Extends(${fh.replaceJavaKeyword(iface)})
[/#list]