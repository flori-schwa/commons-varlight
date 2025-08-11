package me.shawlaf.varlight.persistence.nls;

public enum NLSVersion {
    VERSION_1(1),
    VERSION_2(2);

    private final int _versionNum;

    NLSVersion(int versionNum) {
        _versionNum = versionNum;
    }

    public int versionNum() {
        return _versionNum;
    }
}
