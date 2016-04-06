[#ftl]

package ${package};

import com.sun.jna.platform.win32.COM.util.IComEnum;

[#if (entry.docString)?has_content || (entry.guid)?has_content]
/**
[#if (entry.docString)?has_content] * ${entry.docString!}
 *
[/#if][#if  (entry.guid)?has_content] * <p>uuid(${entry.guid})</p>
[/#if] */
[/#if]
public enum ${javaName} implements IComEnum {
    [#list entry.members as member]
    
    /**
     * [#if (member.documentation)?has_content]${member.documentation!} [/#if](${member.value?c})
     */
    ${fh.replaceJavaKeyword(member.name)}(${member.value?c}),
    [/#list]
    ;

    private ${javaName}(long value) {
        this.value = value;
    }
    private long value;

    public long getValue() {
        return this.value;
    }
}