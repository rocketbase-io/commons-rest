package io.rocketbase.commons.obfuscated;

public interface IdObfuscator {

    ObfuscatedId obfuscate(long id);

    ObfuscatedId decode(String obfuscated);
}
