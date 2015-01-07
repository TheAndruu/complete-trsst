package com.completetrsst.crypto.keys;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemKeyManager implements KeyManager {

    private static final Logger log = LoggerFactory.getLogger(FileSystemKeyManager.class);

    private static final Path KEY_HOME;

    private KeyCreator keyCreator;

    // TODO: Other operations, like create new key, save key, etc

    static {
        KEY_HOME = Paths.get("").toAbsolutePath().getParent();
        log.info("KEY_HOME path: " + KEY_HOME.toString());
    }

    @Override
    public Path getKeyStoreHome() {
        return KEY_HOME;
    }

    /** Generates a EC keypair for signing */
    @Override
    public final KeyPair getKeyPair() {
        // TODO:
        // if current is null, create key pair, at least for now
        // later will have someone log in/ out
        // on startup, have it set the keypair from file, if exists
        // otherwise, create a new one
        return keyCreator.createKeyPair();
    }

    public void setKeyGenerator(KeyCreator keyGenerator) {
        this.keyCreator = keyGenerator;
    }

}
