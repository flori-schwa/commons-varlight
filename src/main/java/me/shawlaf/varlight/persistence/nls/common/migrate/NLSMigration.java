package me.shawlaf.varlight.persistence.nls.common.migrate;

import me.shawlaf.varlight.persistence.nls.NLSVersion;
import me.shawlaf.varlight.persistence.nls.common.NLSHeader;
import me.shawlaf.varlight.persistence.nls.implementations.v2.NLS2Migrator;
import me.shawlaf.varlight.util.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NLSMigration {

    private static final Map<Integer, INLSMigrator> MIGRATORS = new HashMap<>();

    static {
        // Register Migrators here
        MIGRATORS.put(NLSVersion.VERSION_2.versionNum(), new NLS2Migrator());
    }

    public static void migrateFileToVersion(int targetVersion, File file) throws IOException {
        NLSHeader header = NLSHeader.readFromFile(file);
        header.validRequired();

        if (header.getVersion() >= targetVersion) {
            return;
        }

        while (header.getVersion() < targetVersion) {
            final int oldVersion = header.getVersion();
            final int nextVersion = oldVersion + 1;
            INLSMigrator migrator = MIGRATORS.get(nextVersion);
            Objects.requireNonNull(migrator, String.format("No NLS migrator found for Version %d -> %d", oldVersion, nextVersion));

            migrator.migrateFile(file);
            NLSHeader migratedHeader = NLSHeader.readFromFile(file);

            if (migratedHeader.getVersion() <= oldVersion) {
                throw new IllegalStateException("Migrator did not migrate NLS to a higher version (Old Version: %s, Version after Migration: %s)".formatted(oldVersion, migratedHeader.getVersion()));
            }
        }

    }

}
