package io.rocketbase.commons.obfuscated;

import io.rocketbase.commons.exception.ObfuscatedDecodeException;

public interface IdObfuscator {

    ObfuscatedId obfuscate(long id);

    ObfuscatedId decode(String obfuscated) throws ObfuscatedDecodeException;
}
