package me.shawlaf.varlight.persistence.old;

import me.shawlaf.varlight.util.pos.IntPosition;

import java.util.Objects;

@Deprecated
public class BasicCustomLightSource implements ICustomLightSource {

    private final IntPosition position;
    private final String type;
    private final int emittingLight;
    private final boolean migrated;

    public BasicCustomLightSource(IntPosition position, int emittingLight, boolean migrated, String type) {
        this.position = position;
        this.type = type;
        this.emittingLight = emittingLight;
        this.migrated = migrated;
    }

    @Override
    public IntPosition getPosition() {
        return position;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int getCustomLuminance() {
        return emittingLight;
    }

    @Override
    public boolean isMigrated() {
        return migrated;
    }

    @Override
    public String toString() {
        return "BasicStoredLightSource{" +
                "position=" + position +
                ", type='" + type + '\'' +
                ", emittingLight=" + emittingLight +
                ", migrated=" + migrated +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicCustomLightSource that = (BasicCustomLightSource) o;
        return emittingLight == that.emittingLight &&
                migrated == that.migrated &&
                position.equals(that.position) &&
                type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, type, emittingLight, migrated);
    }
}