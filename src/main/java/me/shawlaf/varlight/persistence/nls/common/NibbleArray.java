package me.shawlaf.varlight.persistence.nls.common;

import me.shawlaf.varlight.util.Preconditions;

import java.util.Arrays;

public class NibbleArray {

    protected final byte[] _array;

    public NibbleArray(int size) {
        if ((size & 1) != 0) {
            throw new IllegalArgumentException("Odd values not allowed");
        }

        _array = new byte[size >> 1];
    }

    public NibbleArray(byte[] array) {
        _array = Arrays.copyOf(array, array.length);
    }
    
    public NibbleArray(NibbleArray copyFrom) {
        _array = Arrays.copyOf(copyFrom._array, copyFrom._array.length);
    }

    public int length() {
        return _array.length * 2;
    }

    public int get(int index) {
        int b = _array[index / 2];

        if ((index & 1) == 0) {
            return (b >>> 4) & 0xF;
        }

        return b & 0xF;
    }

    public void set(int index, int value) {
        Preconditions.assertInRange("value", value, 0x0, 0xF);

        int b = _array[index >> 1];

        if ((index & 1) == 0) {
            b = (value << 4) | (b & 0x0F);
        } else {
            b = (b & 0xF0) | value;
        }

        _array[index >> 1] = (byte) b;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(_array, _array.length);
    }
}
