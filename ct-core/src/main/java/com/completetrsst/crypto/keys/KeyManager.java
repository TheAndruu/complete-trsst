package com.completetrsst.crypto.keys;

import java.security.KeyPair;
import java.util.List;

public interface KeyManager {

    public void loadKeys(String id, String password);

    public void saveKeys(String password);

    public void createKeys(String password);

    public KeyPair getSignKey();

    public KeyPair getEncryptKey();

    public String getId();

    public List<String> getAvailableFeedIds();
}
