package com.completetrsst.crypto.keys;

import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyStore;

public interface KeyManager {

    public Path getKeyStoreHome();

    public KeyPair getKeyPair();

    public void setKeyGenerator(KeyCreator generator);

    public KeyStore loadSignKey(String password);

    public KeyStore loadEncryptKey(String password);

    public void saveSignKey(String password);

    public void saveEncryptKey(String password);
}
