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

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.TypeLibUtil;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "list")
public class List extends AbstractMojo {

    /**
     * Report typelib GUIDs, that could not be resolved
     */
    @Parameter(property = "tlbcodegenerator.reportedFailed")
    private Boolean reportedFailed;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        java.util.List<String> failedGUIDs = new LinkedList<>();
        Pattern versionPattern = Pattern.compile("^(\\d).(\\d)$");
        for (String guid : listTypeLibGUIDS()) {
            if (guid.trim().isEmpty()) {
                continue;
            }
            try {
                for (String version : listTypeLibVersions(guid)) {
                    Matcher m = versionPattern.matcher(version);
                    if (m.matches()) {
                        int major = Integer.parseInt(m.group(1));
                        int minor = Integer.parseInt(m.group(2));
                        TypeLibUtil tlu = new TypeLibUtil(guid, major, minor);
                        System.out.println(String.format("%s\t%d\t%d\t%s\t%s",
                                guid, major, minor, tlu.getName().toLowerCase(),
                                typelibGetName(guid, major, minor)));
                    }
                }
            } catch (Win32Exception | COMException ex) {
                failedGUIDs.add(guid);
            }
        }

        if (reportedFailed != null && reportedFailed) {
            System.out.println("\nFailed GUIDs:");
            for (String guid : failedGUIDs) {
                System.out.println(guid);
            }
        }
    }

    private String[] listTypeLibGUIDS() {
        return Advapi32Util.registryGetKeys(WinReg.HKEY_CLASSES_ROOT, "TypeLib");
    }

    private String[] listTypeLibVersions(String guid) {
        return Advapi32Util.registryGetKeys(WinReg.HKEY_CLASSES_ROOT, "TypeLib\\"
                + guid);
    }

    private String typelibGetName(String guid, int major, int minor) {
        return Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT,
                String.format("TypeLib\\%s\\%d.%d", guid, major, minor), "");
    }

}
