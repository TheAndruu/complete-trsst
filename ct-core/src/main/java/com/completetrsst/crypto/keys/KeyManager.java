package com.completetrsst.crypto.keys;

import java.nio.file.Path;
import java.security.KeyPair;

public interface KeyManager {

    public Path getKeyStoreHome();

    public KeyPair getKeyPair();
    
    public void setKeyGenerator(KeyCreator generator);
}
