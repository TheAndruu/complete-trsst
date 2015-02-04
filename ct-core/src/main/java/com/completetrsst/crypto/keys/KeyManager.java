package com.completetrsst.crypto.keys;

import java.security.KeyStore;

public interface KeyManager {

    public KeyStore loadSignKey(String password);

    public KeyStore loadEncryptKey(String password);

    public void saveSignKey(String password);

    public void saveEncryptKey(String password);
}
