package com.completetrsst.client;

import java.io.File;
import java.security.KeyPair;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.keys.KeyCreator;
import com.completetrsst.crypto.keys.KeyManager;
import com.completetrsst.crypto.keys.TrsstKeyFunctions;

public class FileSystemKeyManager implements KeyManager {

    private static final Logger log = LoggerFactory.getLogger(FileSystemKeyManager.class);

    private static final String KEY_HOME;
    private static final String KEY_FILE_EXTENSION = ".p12";

    private KeyCreator creator = null;

    /** ID is the satoshi hash of the signing key */
    private String id;
    private KeyPair signKey;
    private KeyPair encryptKey;

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
    public void loadKeys(String id, String password) {
        char[] passChars = password.toCharArray();
        this.id = id;
        File keyFile = createFile();

        signKey = TrsstKeyFunctions.readSigningKeyPair(id, keyFile, passChars);
        if (signKey == null) {
            log.debug("Couldn't load sign key for given id and password");
        }

        encryptKey = TrsstKeyFunctions.readEncryptionKeyPair(id, keyFile, passChars);
        if (encryptKey == null) {
            log.debug("Couldn't load encrypt key for given id and password");
        }
    }

    @Override
    public void saveKeys(String password) {
        char[] passChars = password.toCharArray();
        File keyFile = createFile();

        TrsstKeyFunctions.writeSigningKeyPair(signKey, id, keyFile, passChars);
        TrsstKeyFunctions.writeEncryptionKeyPair(encryptKey, id, keyFile, passChars);
    }

    @Override
    public void createKeys(String password) {
        signKey = getKeyCreator().createKeyPair();
        id = TrsstKeyFunctions.toFeedId(signKey.getPublic());
        encryptKey = getKeyCreator().createKeyPair();
    }

    private KeyCreator getKeyCreator() {
        return creator == null ? new EllipticCurveKeyCreator() : creator;
    }

    @Override
    public KeyPair getSignKey() {
        return signKey;
    }

    @Override
    public KeyPair getEncryptKey() {
        return encryptKey;
    }

    @Override
    public String getId() {
        return id;
    }

    /** Returns feeds for which we have a keystore. */
    @Override
    public List<String> getAvailableFeedIds() {
        List<String> results = new LinkedList<String>();

        File[] files = new File(KEY_HOME).listFiles();
        if (files == null) {
            return results;
        }

        for (File f : files) {
            int i = f.getName().indexOf(KEY_FILE_EXTENSION);
            if (i != -1) {
                results.add(StringEscapeUtils.unescapeHtml3(f.getName().substring(0, i)));
            }
        }
        return results;
    }

    private File createFile() {
        return new File(KEY_HOME, id + KEY_FILE_EXTENSION);
    }
}
