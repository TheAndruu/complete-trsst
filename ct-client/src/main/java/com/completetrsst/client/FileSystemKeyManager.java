package com.completetrsst.client;

import java.io.File;
import java.security.KeyStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.keys.KeyCreator;
import com.completetrsst.crypto.keys.KeyManager;

public class FileSystemKeyManager implements KeyManager {

    private static final Logger log = LoggerFactory.getLogger(FileSystemKeyManager.class);

    private static final String KEY_HOME;

    private KeyCreator keyCreator = new EllipticCurveKeyCreator();

    static {
        log.info("Static init block");
        String trsstHome = System.getProperty("user.home") + File.separator + ".trsst";
        String defaultKeyStorage = trsstHome + File.separator + "trsst-keys";
        File trsstKeyHome = new File(defaultKeyStorage);
        boolean wasCreated = false;
        if (!trsstKeyHome.exists()) {
            log.info("Creating keystore home directory at: " + defaultKeyStorage);
            wasCreated = trsstKeyHome.mkdirs();
            if (!wasCreated) {
                log.error("Can't create trsst key storage in default location");
            }
        }
        KEY_HOME = defaultKeyStorage;
        log.info("KEY_HOME location: " + KEY_HOME);
    }

    @Override
    public KeyStore loadSignKey(String password) {
//        TrsstKeyFunctions.readSigningKeyPair(id, file, password);
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KeyStore loadEncryptKey(String password) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveSignKey(String password) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void saveEncryptKey(String password) {
        // TODO Auto-generated method stub
        
    }
}
