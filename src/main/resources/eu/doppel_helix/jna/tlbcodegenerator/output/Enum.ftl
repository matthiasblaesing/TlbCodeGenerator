[#ftl]

${package}.${javaName} (${entry.guid!})
    [#list entry.members as member]
    ${fh.replaceJavaKeyword(member.name)}(${member.value?c})
    [/#list]