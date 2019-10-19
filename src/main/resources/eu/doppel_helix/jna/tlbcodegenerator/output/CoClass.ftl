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
${package}.${javaName} (${entry.guid})
[#list entry.sourceInterfaces as iface]
    Source(${fh.replaceJavaKeyword(iface)})
[/#list]
[#list interfaces as iface]
    Extends(${fh.replaceJavaKeyword(iface)})
[/#list]