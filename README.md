TlbCodeGenerator is a code generator for COM libraries. Based on the
type library information, bindings for many COM based libraries can
be created automaticly.

Usage
=====

Please note: This plugin uses the typelibrary parser, that is build into the
Windows COM subsystem by directly calling into the native libraries. It won't
work on non-windows platforms!

The basic idea is, that per bound COM library one maven project is
created. This is for example reflected in the option to derive the
version of the type library from the project version.

The type library can be used/referenced in two ways:

- by specifying the file that holds the typelibrary or
- by specifying the triplet: GUID/major/minor

The second options requires the COm library to be correctly registered in the
system.

Download
--------

[![Maven Central](https://img.shields.io/maven-central/v/eu.doppel-helix.jna.tlbcodegenerator/TlbCodeGenerator.svg?label=Maven%20Central)](https://search.maven.org/artifact/eu.doppel-helix.jna.tlbcodegenerator/TlbCodeGenerator/1.0.0/jar)&nbsp;[TlbCodeGenerator-1.0.0.jar](http://repo1.maven.org/maven2/eu/doppel-helix/jna/tlbcodegenerator/TlbCodeGenerator/1.0.0/TlbCodeGenerator-1.0.0.jar)

Listing
-------

A list of all installed libraries can be generated by calling the `list` mojo:

```
mvn eu.doppel-helix.jna.tlbcodegenerator:TlbCodeGenerator:list
```

This will generate a list with columns:

- GUID
- Major verion
- Minor version
- Name of the type library (internal)
- User visible name of the type library

The task can be called without a project.

Generation
----------

The complete sources for the following samples can be found here:

https://github.com/matthiasblaesing/COMTypelibraries

The first sample creates bindings for "OLE Automation (Ver 2.0)".

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>eu.doppel-helix.jna.tlb</groupId>
        <artifactId>parent</artifactId>
        <version>1.1</version>
    </parent>
    
    <artifactId>stdole2</artifactId>
    <version>2.0.1</version>
    <packaging>jar</packaging>
    
    <build>
        <plugins>
            <plugin>
                <groupId>eu.doppel-helix.jna.tlbcodegenerator</groupId>
                <artifactId>TlbCodeGenerator</artifactId>
                <version>1.0.0</version>
                <configuration>
                    <guid>{00020430-0000-0000-C000-000000000046}</guid>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

The major and minor version of the type library are 2 and 0. In this sample
the values are derived from the first two components of the project version.

In this case the third part of the version string denotes the revision of the
bindings.

The only configuration in this case is the specification of the GUID of the
library that is to be bound.

Invoking the `generate` mojo will generate the sources in the project:

```
mvn eu.doppel-helix.jna.tlbcodegenerator:TlbCodeGenerator:generate
```

Existing source will be overwritten without warning.

The plugin does not bind to any maven lifecycle phase, so it is only executed
if explicitly called.

Dependencies
------------

COM type libraries can depend on each other. For example the "OLE Automation 
(Ver 2.0)" library is a pre-requisite for the "Windows Image Acquisition Automation"
library. To enable resolving dependencies, this plugin uses a combination of
maven depdencies and additional meta-data.

Each generated type library created by this plugin contains two files in the
`META-INF/typelib` folder. Both files are java property files:

- PACKAGE_NAME.info.properties:
  - GUID
  - Major version
  - Minor version
  - Native version
  - package name

- PACKAGE_NAME.types.properties: This file mapps CLSIDs to java class names

The bindings of WIA looks like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>eu.doppel-helix.jna.tlb</groupId>
        <artifactId>parent</artifactId>
        <version>1.1</version>
    </parent>
    
    <artifactId>wia1</artifactId>
    <version>1.0.1</version>
    <packaging>jar</packaging>
    
    <description>Windows Image Acquisition Automation</description>
    
    <dependencies>
        <dependency>
            <groupId>eu.doppel-helix.jna.tlb</groupId>
            <artifactId>stdole2</artifactId>
            <version>2.0.1</version>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>eu.doppel-helix.jna.tlbcodegenerator</groupId>
                <artifactId>TlbCodeGenerator</artifactId>
                <version>1.0.0</version>
                <configuration>
                    <file>c:/windows/system32/wiaaut.dll</file>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

This file shows two thinks:

- demonstrate dependency
- building bindings from a typelibrary file

As in the simple case, the bindings are generated by invoking:

```
mvn eu.doppel-helix.jna.tlbcodegenerator:TlbCodeGenerator:generate
```

Release
=======

- Update README.md with the new version number in the samples
- For a local snapshot run `mvn install`
- To deploy a snapshot to `https://oss.sonatype.org/content/repositories/snapshots/`
  run `mvn deploy` if you don't have a siging key configured and want to be
  closer to the final release run `mvn -P release-profile deploy`
- To do a full release run `mvn release:prepare` followed by `mvn release:perform`