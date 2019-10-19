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

package eu.doppel_helix.jna.tlbcodegenerator.imp;

import com.sun.jna.platform.win32.COM.TypeInfoUtil;
import com.sun.jna.platform.win32.OaIdl;

public class TlbAlias extends TlbEntry {
    private final OaIdl.TYPEDESC referenced;
    private final TypeInfoUtil tiu;

    public TlbAlias(TypeLib tl, int index) {
        super(tl, index);
        tiu = tl.getTypeLibUtil().getTypeInfoUtil(index);
        referenced = tiu.getTypeAttr().tdescAlias;
    }

    public String getReferencedType() {
        return getTypeLib().getType(tiu, referenced);
    }

    @Override
    public String toString() {
        return "TlbAlias{" + "referenced=" + referenced + ", tiu=" + tiu + '}';
    }
}
