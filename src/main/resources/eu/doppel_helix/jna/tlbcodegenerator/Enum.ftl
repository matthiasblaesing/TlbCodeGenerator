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