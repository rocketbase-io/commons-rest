package io.rocketbase.commons.service;

import io.rocketbase.commons.exception.ObfuscatedDecodeException;
import io.rocketbase.commons.obfuscated.IdObfuscator;
import io.rocketbase.commons.obfuscated.ObfuscatedId;
import io.rocketbase.commons.obfuscated.SimpleObfuscatedId;
import lombok.RequiredArgsConstructor;
import org.hashids.Hashids;

@RequiredArgsConstructor
public class DefaultIdObfuscator implements IdObfuscator {

    private final Hashids hashids;

    @Override
    public ObfuscatedId obfuscate(long id) {
        return new SimpleObfuscatedId(id, hashids.encode(id));
    }

    @Override
    public ObfuscatedId decode(String obfuscated) {
        long[] decode = hashids.decode(obfuscated);
        if (decode.length != 1) {
            throw new ObfuscatedDecodeException();
        }
        return new SimpleObfuscatedId(decode[0], obfuscated);
    }
}
