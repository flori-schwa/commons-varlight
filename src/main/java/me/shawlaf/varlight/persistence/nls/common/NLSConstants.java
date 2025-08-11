package me.shawlaf.varlight.persistence.nls.common;


import me.shawlaf.varlight.persistence.nls.NLSVersion;

public final class NLSConstants {

    private NLSConstants() {

    }

    public static final int NLS_MAGIC = 0x4E_41_4C_53; // "NALS" = Nibble Array Light Storage

    public static final NLSVersion CURRENT_VERSION = NLSVersion.VERSION_2;

    public static final int CURRENT_VERSION_NUM = CURRENT_VERSION.versionNum();

}
