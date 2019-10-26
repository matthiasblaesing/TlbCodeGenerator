/* Copyright (c) 2016 Matthias Bläsing, All Rights Reserved
 *
 * The contents of this file is dual-licensed under 2
 * alternative Open Source/Free licenses: LGPL 2.1 or later and
 * Apache License 2.0. (starting with JNA version 4.0.0).
 *
 * You can freely decide which license you want to apply to
 * the project.
 *
 * You may obtain a copy of the LGPL License at:
 *
 * http://www.gnu.org/licenses/licenses.html
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "LGPL2.1".
 *
 * You may obtain a copy of the Apache License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "AL2.0".
 */

package eu.doppel_helix.jna.tlbcodegenerator.maven;

import eu.doppel_helix.jna.tlbcodegenerator.imp.FormatHelper;
import eu.doppel_helix.jna.tlbcodegenerator.imp.TlbAlias;
import eu.doppel_helix.jna.tlbcodegenerator.imp.TlbCoClass;
import eu.doppel_helix.jna.tlbcodegenerator.imp.TlbEntry;
import eu.doppel_helix.jna.tlbcodegenerator.imp.TlbEnum;
import eu.doppel_helix.jna.tlbcodegenerator.imp.TlbInterface;
import eu.doppel_helix.jna.tlbcodegenerator.imp.TypeLib;
import eu.doppel_helix.jna.tlbcodegenerator.maven.util.JULBridge;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Generate JNA COM bindings for supplied typelibraries
 *
 * <p>
 * <b>Microsoft Shell Controls And Automation</b></p>
 * <table>
 * <caption>Via file:</caption>
 * <tr><th>file</th><td>shell32.dll</td></tr>
 * </table>
 *
 * <table>
 * <caption>Via GUID, Major, Minor:</caption>
 * <tr><th>guid</th><td>{50A7E9B0-70EF-11D1-B75A-00A0C90564FE}</td></tr>
 * <tr><th>major</th><td>1</td></tr>
 * <tr><th>minor</th><td>0</td></tr>
 * </table>
 *
 * <table>
 * <caption>Sample: Microsoft Word 12.0 Object Library</caption>
 * <tr><th>guid</th><td>{00020905-0000-0000-C000-000000000046}</td></tr>
 * <tr><th>major</th><td>8</td></tr>
 * <tr><th>minor</th><td>4</td></tr>
 * </table>
 *
 * @author Matthias Bläsing
 */
@Mojo(name = "generate", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class Generator extends AbstractMojo {

    @Component
    private MavenProject project;

    /**
     * Tlb file to use for code generation. If specified superseeds guid.
     */
    @Parameter(property = "tlbcodegenerator.file")
    private String file;

    /**
     * GUID of typelibrary to analyse.
     */
    @Parameter(property = "tlbcodegenerator.guid")
    private String guid;

    /**
     * Major version of typelibrary to analyse. If not specified, project major
     * version is used.
     */
    @Parameter(property = "tlbcodegenerator.major")
    private Integer major;

    /**
     * Minor version of typelibrary to analyse. If not specified, project minor
     * version is used.
     */
    @Parameter(property = "tlbcodegenerator.minor")
    private Integer minor;

    /**
     * Package for the generated sources. Defaults to artifactId.
     */
    @Parameter(property = "tlbcodegenerator.packageName")
    private String packageName;
    
    /**
     * Prefix for the package. Defaults to groupId
     */
    @Parameter(property = "tlbcodegenerator.basepackage")
    private String basepackage;
    
    /**
     * If set to true the parsing result will be displayed and not written.
     */
    @Parameter(property = "tlbcodegenerator.displayonly", defaultValue = "false")
    private boolean displayonly;
    
    /**
     * If set to true option values are mapped to object, if false optional values are mapped to their "correct" type
     */
    @Parameter(property = "tlbcodegenerator.mapOptionalToObject", defaultValue = "false")
    private boolean mapOptionalToObject;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        JULBridge bridge = new JULBridge(getLog());
        try {
            initDefaults();

            ArrayList<File> dependencies = new ArrayList<>();
            for(Artifact a: (Set<Artifact>) project.getArtifacts()) {
                dependencies.add(a.getFile());
            }
            
            TypeLib typeLibrary;
            if (file != null) {
                typeLibrary = new TypeLib(dependencies, file);
            } else if (guid != null && major != null && minor != null) {
                typeLibrary = new TypeLib(dependencies, guid, major, minor);
            } else {
                throw new MojoFailureException("Either file or guid need to be specified");
            }
            
            typeLibrary.setMapOptionalToObject(mapOptionalToObject);

            Configuration cfg = getFreemarkerConfig();
            
            String packageName = getPackageName(typeLibrary);

            File srcOutputDir = null;
            File rsrcOutputDir = null;

            if (! displayonly) {
                srcOutputDir = createSourceDir(packageName);
                rsrcOutputDir = createResourceDir(packageName);
                writePackageInfo(typeLibrary, srcOutputDir, cfg, packageName);
            }

            int typesCount = 0;
            int writtenCount = 0;
            
            Properties packageData = new Properties();
            Properties typeData = new Properties();
            
            for (TlbEntry ent : typeLibrary.getEntries().values()) {
                typesCount++;
                String qualifiedName = packageName + "." + FormatHelper.replaceJavaKeyword(ent.getName());
                if(ent.getGuid() != null) {
                    typeData.put(ent.getGuid(), qualifiedName);
                }
                int written = writeEntry(typeLibrary, srcOutputDir, cfg, ent, packageName, null, displayonly);
                writtenCount += written;
            }
            
            packageData.put("guid", typeLibrary.getUUID());
            packageData.put("name", packageName);
            packageData.put("nativename", typeLibrary.getName());
            packageData.put("major", Integer.toString(typeLibrary.getMajorVersion()));
            packageData.put("minor", Integer.toString(typeLibrary.getMinorVersion()));
            
            try (OutputStream info = new FileOutputStream(new File(rsrcOutputDir, packageName + ".info.properties"));
                    OutputStream types = new FileOutputStream(new File(rsrcOutputDir, packageName + ".types.properties"));
                    ) {
                packageData.store(info, "");
                typeData.store(types, "");
            }
            
            
            if(srcOutputDir != null) {
                getLog().info(String.format("%d/%d types parsed and results written to: %s", typesCount, writtenCount, srcOutputDir.toString()));
            } else {
                getLog().info(String.format("%d/%d types parsed", typesCount, writtenCount));
            }

        } catch (Exception e) {
            throw new MojoExecutionException("Faild to parse typelibrary", e);
        } finally {
            bridge.restore();
        }
    }
    
    private String getPackageName(TypeLib tl) {
        String packageNameConst = packageName;
        if (packageNameConst == null) {
            packageNameConst = tl.getName().toLowerCase();
        }
        if (basepackage != null && (!basepackage.isEmpty())) {
            packageNameConst = basepackage + "."
                    + packageNameConst;
        }
        return packageNameConst;
    }

    private void initDefaults() {
        ArtifactVersion av = new DefaultArtifactVersion(project.getVersion());
        if(major == null) {
            major = av.getMajorVersion();
        }
        if(minor == null) {
            minor = av.getMinorVersion();
        }
        if(packageName == null) {
            packageName = project.getArtifactId();
        }
        if(basepackage == null) {
            basepackage = project.getGroupId();
        }
    }
    
    private Configuration getFreemarkerConfig() {
        Configuration cfg = new Configuration(new Version("2.3.23"));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.ENGLISH);
        cfg.setDateFormat("yyyy-MM-dd");
        cfg.setTimeFormat("HH:mm:ss");
        cfg.setDateTimeFormat("yyyy-MM-dd HH:mm.ss");
        cfg.setObjectWrapper(new DefaultObjectWrapper(new Version("2.3.23")));
        if (displayonly) {
            cfg.setClassForTemplateLoading(Generator.class, "/eu/doppel_helix/jna/tlbcodegenerator/output");
        } else {
            cfg.setClassForTemplateLoading(Generator.class, "/eu/doppel_helix/jna/tlbcodegenerator");
        }
        return cfg;
    }
    
    private File createSourceDir(String packageName) {
        File _outputDir = new File(project.getBasedir(), "src/main/java");

        String path = packageName.replace(".", "/");

        _outputDir = new File(_outputDir, path);
        _outputDir.mkdirs();
        
        return _outputDir;
    }
   
    private File createResourceDir(String packageName) {
        File _outputDir = new File(project.getBasedir(), "src/main/resources/META-INF/typelib");
        _outputDir.mkdirs();
        return _outputDir;
    }
    
    private void fillTemplate(Configuration cfg, String template, File target, Map<String, Object> data, boolean output) throws TemplateException, IOException {
        Template t = cfg.getTemplate(template);
        if (output) {
            t.process(data, new OutputStreamWriter(System.out));
        } else {
            try (FileOutputStream fos = new FileOutputStream(target);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, Charset.forName("UTF-8"))) {
                t.process(data, osw);
            }
        }
    }
    
    private void writePackageInfo(TypeLib typeLibrary, File outputDirectory, Configuration cfg, String packageName) throws MalformedTemplateNameException, ParseException, IOException, TemplateException {
        File target = new File(outputDirectory, "package-info.java");
        Map<String,Object> data = new HashMap<>();
        data.put("fh", new FormatHelper());
        data.put("package", packageName);
        data.put("name", typeLibrary.getName());
        data.put("docString", typeLibrary.getDocString());
        data.put("guid", typeLibrary.getUUID());
        data.put("majorversion", typeLibrary.getMajorVersion());
        data.put("minorversion", typeLibrary.getMinorVersion());
        fillTemplate(cfg, "Package.ftl", target, data, false);
    }
    
    private int writeEntry(TypeLib typeLibrary, File outputDirectory, Configuration cfg, TlbEntry entry, String packageName, String overrideName, boolean output) throws MalformedTemplateNameException, ParseException, IOException, TemplateException {
        if(entry == null) {
            return 0;
        }
        Map<String,Object> data = new HashMap<>();
        data.put("fh", new FormatHelper());
        data.put("entry", entry);
        data.put("package", packageName);
        data.put("typeLib", typeLibrary);
        String javaName = FormatHelper.replaceJavaKeyword(entry.getName());
        if(overrideName != null) {
            javaName = FormatHelper.replaceJavaKeyword(overrideName);
        }
        data.put("javaName", javaName);
        
        File target = new File(outputDirectory, javaName + ".java");
        
        if (entry instanceof TlbAlias) {
            TlbEntry referenced = typeLibrary.getEntry(((TlbAlias) entry).getReferencedType());
            return writeEntry(typeLibrary, outputDirectory, cfg, referenced, packageName, entry.getName(), output);
        } else if (entry instanceof TlbEnum) {
            fillTemplate(cfg, "Enum.ftl", target, data, output);
            return 1;
        } else if (entry instanceof TlbInterface) {
            TlbInterface tdi = (TlbInterface) entry;
            int res = 0;
            if (tdi.isDispatch() || tdi.isDispatchable() || tdi.isDual() || tdi.isOleautomation()) {
                fillTemplate(cfg, "Interface.ftl", target, data, output);
                res++;
                if (tdi.isUsedAsSource()) {
                    data.put("javaName", javaName);
                    target = new File(outputDirectory, javaName + "Handler.java");
                    fillTemplate(cfg, "InterfaceListenerHandler.ftl", target, data, output);
                    res++;
                }
            }
            return res;
        } else if (entry instanceof TlbCoClass) {
            TlbCoClass tcc = (TlbCoClass) entry;
            java.util.List<String> implInterfaces = new ArrayList<>(tcc.getInterfaces());
            implInterfaces.retainAll(typeLibrary.getEntries().keySet());
            
            java.util.List<String> interfaces = new ArrayList<>();
            if(! implInterfaces.isEmpty()) {
                interfaces.add(implInterfaces.get(0));
            }
            if(! tcc.getSourceInterfaces().isEmpty()) {
                interfaces.add("IConnectionPoint");
            }
            data.put("interfaces", interfaces);
            fillTemplate(cfg, "CoClass.ftl", target, data, output);
            return 1;
        }
        
        return 0;
    }
}
