
package eu.doppel_helix.jna.tlbcodegenerator;

import eu.doppel_helix.jna.tlbcodegenerator.imp.TlbInterface;
import eu.doppel_helix.jna.tlbcodegenerator.imp.TlbCoClass;
import eu.doppel_helix.jna.tlbcodegenerator.imp.FormatHelper;
import eu.doppel_helix.jna.tlbcodegenerator.imp.TlbEntry;
import eu.doppel_helix.jna.tlbcodegenerator.imp.TlbAlias;
import eu.doppel_helix.jna.tlbcodegenerator.imp.TypeLib;
import eu.doppel_helix.jna.tlbcodegenerator.imp.TlbEnum;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.COM.COMException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.sun.jna.platform.win32.COM.TypeLibUtil;
import com.sun.jna.platform.win32.COM.tlb.imp.TlbConst;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class TlbImp implements TlbConst {

    private static final Logger LOG = Logger.getLogger(TlbImp.class.getName());

    /** The out. */
    private File comRootDir = new File("");

    private final OptionSet cmdlineArgs;

    
    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
        new TlbImp(args).main();
//        new TlbImp(new String[] {
//            "--tlb.id", "{0002E157-0000-0000-C000-000000000046}",
//            "--tlb.major.version", "5",
//            "--tlb.minor.version", "3",
//            "--output.dir", "E:\\NetbeansProjects\\IETest2\\src"
//        }).main();
    }

    private TlbImp(String[] args) {
        this.cmdlineArgs = parseCmdLine(args);
    }

    private void main() {
        if (this.cmdlineArgs.has("tlb.list")) {
            this.listTlbs();
        } else if (this.cmdlineArgs.has("tlb.file") || this.cmdlineArgs.has("tlb.id")) {
            this.startCOM2Java();
        }
    }
    
    private void listTlbs() {
        List<String> failedGUIDs = new LinkedList<>();
        Pattern versionPattern = Pattern.compile("^(\\d).(\\d)$");
        for (String guid : listTypeLibGUIDS()) {
            if(guid.trim().isEmpty()) {
                continue;
            }
            try {
                for (String version : listTypeLibVersions(guid)) {
                    Matcher m = versionPattern.matcher(version);
                    if(m.matches()) {
                        int major = Integer.parseInt(m.group(1));
                        int minor = Integer.parseInt(m.group(2));
                        TypeLibUtil tlu = new TypeLibUtil(guid, major, minor);
                        System.out.println(String.format("%s\t%d\t%d\t%s\t%s", 
                                guid, major, minor, tlu.getName().toLowerCase(),
                                typelibGetName(guid, major, minor)));
                    }
                }
            } catch (Win32Exception ex) {
                failedGUIDs.add(guid);
            } catch (COMException ex) {
                failedGUIDs.add(guid);
            }
        }
        
        if((! failedGUIDs.isEmpty()) && ((Boolean) cmdlineArgs.valueOf("tlb.list"))) {
            System.out.println("\nFailed GUIDs:");
            for(String guid: failedGUIDs) {
                System.out.println(guid);
            }
        }
    }
    
    private String[] listTypeLibGUIDS() {
        return Advapi32Util.registryGetKeys(WinReg.HKEY_CLASSES_ROOT, "TypeLib");
    }
    
    private String[] listTypeLibVersions(String guid) {
        return Advapi32Util.registryGetKeys(WinReg.HKEY_CLASSES_ROOT, "TypeLib\\" + guid);
    }
    
    private String typelibGetName(String guid, int major, int minor) {
        return Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, 
                String.format("TypeLib\\%s\\%d.%d", guid, major, minor), "");
    }
    
    /**
     * Start startCOM2Java.
     */
    public void startCOM2Java() {
        try {
            TypeLib typeLibrary;
            if (this.cmdlineArgs.has("tlb.file")) {
                String file = (String) this.cmdlineArgs.valueOf("tlb.file");
                System.out.println(file);
                typeLibrary = new TypeLib(file);
            } else if (this.cmdlineArgs.has("tlb.id")) {
                String clsid = (String) this.cmdlineArgs.valueOf("tlb.id");
                int majorVersion = (Integer) this.cmdlineArgs.valueOf("tlb.major.version");
                int minorVersion = (Integer) this.cmdlineArgs.valueOf("tlb.minor.version");
                typeLibrary = new TypeLib(clsid, majorVersion, minorVersion);
            } else {
                throw new IllegalStateException("");
            }

            Configuration cfg = new Configuration(new Version("2.3.23"));
            cfg.setDefaultEncoding("UTF-8");
            cfg.setLocale(Locale.ENGLISH);
            cfg.setDateFormat("yyyy-MM-dd");
            cfg.setTimeFormat("HH:mm:ss");
            cfg.setDateTimeFormat("yyyy-MM-dd HH:mm.ss");
            cfg.setObjectWrapper(new DefaultObjectWrapper(new Version("2.3.23")));
            
            if(this.cmdlineArgs.has("display")) {
                cfg.setClassForTemplateLoading(TlbImp.class, "output");
            } else {
                cfg.setClassForTemplateLoading(TlbImp.class, "");
            }

            String packageName = getPackageName(typeLibrary);

            if(! this.cmdlineArgs.has("display")) {
                this.createDir(packageName);
                writePackageInfo(typeLibrary, cfg, packageName);
            }
            
            int typesCount = 0;
            int writtenCount = 0;
            for (TlbEntry ent : typeLibrary.getEntries().values()) {
                typesCount++;
                int written = writeEntry(typeLibrary, cfg, ent, packageName, null, this.cmdlineArgs.has("display"));
                writtenCount += written;
            }

            LOG.info(String.format("%d/%d types parsed and results written to: %s", typesCount, writtenCount, this.comRootDir.toString()));

        } catch (Exception e) { 
            LOG.log(Level.SEVERE, "Failed to parse", e);
        }
    }
    
    private void writePackageInfo(TypeLib typeLibrary, Configuration cfg, String packageName) throws MalformedTemplateNameException, ParseException, IOException, TemplateException {
        File target = new File(this.comRootDir, "package-info.java");
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
    
    private int writeEntry(TypeLib typeLibrary, Configuration cfg, TlbEntry entry, String packageName, String overrideName, boolean output) throws MalformedTemplateNameException, ParseException, IOException, TemplateException {
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
        
        File target = new File(this.comRootDir, javaName + ".java");
        
        if (entry instanceof TlbAlias) {
            TlbEntry referenced = typeLibrary.getEntry(((TlbAlias) entry).getReferencedType());
            return writeEntry(typeLibrary, cfg, referenced, packageName, entry.getName(), output);
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
                    data.put("javaName", javaName + "Listener");
                    target = new File(this.comRootDir, javaName + "Listener.java");
                    fillTemplate(cfg, "InterfaceListener.ftl", target, data, output);
                    res++;
                }
            }
            return res;
        } else if (entry instanceof TlbCoClass) {
            TlbCoClass tcc = (TlbCoClass) entry;
            List<String> implInterfaces = new ArrayList<>(tcc.getInterfaces());
            implInterfaces.retainAll(typeLibrary.getEntries().keySet());
            
            List<String> interfaces = new ArrayList<>();
            if(! implInterfaces.isEmpty()) {
                interfaces.add(implInterfaces.get(0));
            }
            if(! tcc.getSourceInterfaces().isEmpty()) {
                interfaces.add("IConnectionPoint");
            }
            interfaces.add("IUnknown");
            data.put("interfaces", interfaces);
            fillTemplate(cfg, "CoClass.ftl", target, data, output);
            return 1;
        }
        return 0;
    }

    public void fillTemplate(Configuration cfg, String template, File target, Map<String, Object> data, boolean output) throws TemplateException, IOException {
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
    

    private void createDir(String packageName) throws FileNotFoundException {
        String _outputDir = (String) this.cmdlineArgs.valueOf("output.dir");

        String path = packageName.replace(".", "/");

        this.comRootDir = new File(_outputDir, path);
        this.comRootDir.mkdirs();
        
        if (this.comRootDir.canWrite()) {
            LOG.info("Output directory found.");
        } else {
            throw new FileNotFoundException(
                    "Output directory can't be written to: "
                            + this.comRootDir.toString());
        }
    }

    private String getPackageName(TypeLib tl) {
        String configBasepackageName = (String) this.cmdlineArgs.valueOf("basepackage");
        String packageNameConst = (String) this.cmdlineArgs.valueOf("package");
        if (packageNameConst == null) {
            packageNameConst = tl.getName().toLowerCase();
        }
        if (configBasepackageName != null
                && (!configBasepackageName.isEmpty())) {
            packageNameConst = configBasepackageName + "."
                    + packageNameConst;
        }
        return packageNameConst;
    }

    private OptionSet parseCmdLine(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("tlb.list", "List detected type libraries from registry")
                .withOptionalArg()
                .describedAs("Show error output")
                .ofType(Boolean.class)
                .defaultsTo(false);
        parser.accepts("tlb.id", "The guid of the type library")
                .withRequiredArg()
                .describedAs("GUID")
                .ofType(String.class);
        parser.accepts("tlb.file", "The file of the type library")
                .withRequiredArg()
                .describedAs("FILE")
                .ofType(String.class);
        parser.accepts("tlb.major.version", "The major version of the type library.")
                .requiredIf("tlb.id")
                .withRequiredArg()
                .ofType(Integer.class);
        parser.accepts("tlb.minor.version", "The minor version of the type library.")
                .requiredIf("tlb.id")
                .withRequiredArg()
                .ofType(Integer.class);
        parser.accepts("output.dir", "The optional output directory")
                .withRequiredArg()
                .ofType(String.class);
        parser.accepts("basepackage", "Basis for package name")
                .withRequiredArg()
                .ofType(String.class);
        parser.accepts("package", "Target package (if specified will be appended to basepackage)")
                .withRequiredArg()
                .ofType(String.class);
        parser.accepts("display", "Show parsed data and don't write output");
        parser.accepts("help", "Show help");
        
        // Missing: sample:


        try {
            OptionSet result = parser.parse(args);
         
            if(result.has("help")) {
                try {
                    parser.printHelpOn(System.out);
                } catch (IOException ex) {
                }
                outputSample(System.out);
                System.exit(0);
            }
            
            for(OptionSpec os: result.specs()) {
                os.values(result);
            }
            
            return result;
        } catch (OptionException ex) {
            System.err.println(ex.getMessage());
            System.err.println("");
            try {
                parser.printHelpOn(System.err);
            } catch (IOException ex1) {
            }
            outputSample(System.err);
            System.exit(1);
            return null; // This is superfloues!
        }
    }
    
    private void outputSample(PrintStream ps) {
        ps.append("\n");
        ps.append("samples:\n");
        ps.append("Microsoft Shell Controls And Automation:\n");
        ps.append("-tlb.file shell32.dll\n");
        ps.append("-tlb.id {50A7E9B0-70EF-11D1-B75A-00A0C90564FE} -tlb.major.version 1 -tlb.minor.version 0\n\n");
        ps.append("Microsoft Word 12.0 Object Library:\n");
        ps.append("-tlb.id {00020905-0000-0000-C000-000000000046} -tlb.major.version 8 -tlb.minor.version 4\n");
    }
}