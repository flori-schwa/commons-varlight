package me.shawlaf.varlight.persistence.nls.common.exception;

public class ExpectedMagicNumberException extends RuntimeException {

    private int expected;
    private int got;

    public ExpectedMagicNumberException(int expected, int got) {
        super(String.format("Expected Magic number %08x, got %08x", expected, got));

        this.expected = expected;
        this.got = got;
    }

    public int getExpected() {
        return expected;
    }

    public int getGot() {
        return got;
    }
}
