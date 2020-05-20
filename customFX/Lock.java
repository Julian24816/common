package de.julianpadawan.common.customFX;

public interface Lock {
    boolean holdByUs();
    String getHolder();
    void release();
}
