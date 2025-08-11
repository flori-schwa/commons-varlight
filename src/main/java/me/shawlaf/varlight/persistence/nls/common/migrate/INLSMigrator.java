package me.shawlaf.varlight.persistence.nls.common.migrate;

import java.io.File;
import java.io.IOException;

public interface INLSMigrator {

    void migrateFile(File file) throws IOException;

}
