package com.completetrsst.crypto.keys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.completetrsst.constants.Nodes;

/**
 * Crypto functions copied over from Trsst.
 * 
 * @author mpowers.
 */
public class TrsstKeyFunctions {
    private static final Logger log = LoggerFactory.getLogger(TrsstKeyFunctions.class);

    public static final String FEED_URN_PREFIX = "urn:feed:";
    private static final char[] b58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final int[] r58 = new int[256];
    static {
        for (int i = 0; i < 256; ++i) {
            r58[i] = -1;
        }
        for (int i = 0; i < b58.length; ++i) {
            r58[b58[i]] = i;
        }
    }

    private TrsstKeyFunctions() {
        // Utility class
    }

    public static final KeyPair readSigningKeyPair(String id, File file, char[] pwd) {
        return readKeyPairFromFile(id + '-' + Nodes.TRSST_SIGN, file, pwd);
    }

    public static final KeyPair readEncryptionKeyPair(String id, File file, char[] pwd) {
        return readKeyPairFromFile(id + '-' + Nodes.TRSST_ENCRYPT, file, pwd);
    }

    public static final void writeSigningKeyPair(KeyPair keyPair, String id, File file, char[] pwd) {
        writeKeyPairToFile(keyPair, createCertificate(keyPair, "SHA1withECDSA"), id + '-' + Nodes.TRSST_SIGN, file, pwd);
    }

    public static final void writeEncryptionKeyPair(KeyPair keyPair, String id, File file, char[] pwd) {
        writeKeyPairToFile(keyPair, createCertificate(keyPair, "SHA1withECDSA"), id + '-' + Nodes.TRSST_ENCRYPT, file, pwd);
    }

    private static final KeyPair readKeyPairFromFile(String alias, File file, char[] pwd) {
        FileInputStream input = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            input = new FileInputStream(file);
            keyStore.load(new FileInputStream(file), pwd);
            input.close();

            KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, new KeyStore.PasswordProtection(pwd));
            PrivateKey privateKey = pkEntry.getPrivateKey();
            PublicKey publicKey = pkEntry.getCertificate().getPublicKey();
            return new KeyPair(publicKey, privateKey);
        } catch (IOException bpe) {
            log.debug("Passphrase could not decrypt key: " + bpe.getMessage());
        } catch (Exception e) {
            log.debug("Unexpected error while reading key: " + e.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    // ignore while closing
                    log.trace("Error while closing: " + e.getMessage(), e);
                }
            }
        }
        return null;
    }

    private static final void writeKeyPairToFile(KeyPair keyPair, X509Certificate cert, String alias, File file, char[] pwd) {
        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            if (file.exists()) {
                input = new FileInputStream(file);
                keyStore.load(new FileInputStream(file), pwd);
                input.close();
            } else {
                keyStore.load(null); // weird but required
            }

            // save my private key
            keyStore.setKeyEntry(alias, keyPair.getPrivate(), pwd, new X509Certificate[] { cert });

            // store away the keystore
            output = new java.io.FileOutputStream(file);
            keyStore.store(output, pwd);
            output.flush();
        } catch (Exception e) {
            log.error("Error while storing key: " + e.getMessage(), e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    // ignore while closing
                    log.trace("Error while closing: " + e.getMessage(), e);
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    // ignore while closing
                    log.trace("Error while closing: " + e.getMessage(), e);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static final X509Certificate createCertificate(KeyPair keyPair, String algorithm) {
        org.bouncycastle.x509.X509V3CertificateGenerator certGen = new org.bouncycastle.x509.X509V3CertificateGenerator();

        long now = System.currentTimeMillis();
        certGen.setSerialNumber(java.math.BigInteger.valueOf(now));

        org.bouncycastle.jce.X509Principal subject = new org.bouncycastle.jce.X509Principal("CN=Trsst Keystore,DC=trsst,DC=com");
        certGen.setIssuerDN(subject);
        certGen.setSubjectDN(subject);

        Date fromDate = new java.util.Date(now);
        certGen.setNotBefore(fromDate);
        Calendar cal = new java.util.GregorianCalendar();
        cal.setTime(fromDate);
        cal.add(java.util.Calendar.YEAR, 100);
        Date toDate = cal.getTime();
        certGen.setNotAfter(toDate);

        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm(algorithm);
        certGen.addExtension(org.bouncycastle.asn1.x509.X509Extensions.BasicConstraints, true, new org.bouncycastle.asn1.x509.BasicConstraints(false));
        certGen.addExtension(org.bouncycastle.asn1.x509.X509Extensions.KeyUsage, true, new org.bouncycastle.asn1.x509.KeyUsage(
                org.bouncycastle.asn1.x509.KeyUsage.digitalSignature | org.bouncycastle.asn1.x509.KeyUsage.keyEncipherment
                        | org.bouncycastle.asn1.x509.KeyUsage.keyCertSign | org.bouncycastle.asn1.x509.KeyUsage.cRLSign));
        X509Certificate x509 = null;
        try {
            x509 = certGen.generateX509Certificate(keyPair.getPrivate());
        } catch (InvalidKeyException e) {
            log.error("Error generating certificate: invalid key", e);
        } catch (SecurityException e) {
            log.error("Unexpected error generating certificate", e);
        } catch (SignatureException e) {
            log.error("Error generating generating certificate signature", e);
        }
        return x509;
    }

    /**
     * While the feed's identifier, not the actual <id> node of a Feed. For that, use {@link #toFeedUrn(String) toFeedUrn}
     * 
     * Hashes an elliptic curve public key into a shortened "satoshi-style" string that we use for a publicly-readable account id. Borrowed from
     * bitsofproof and mpowers.
     * 
     * @param key
     *            the account EC public key.
     * @return the account id
     */
    public static String toFeedId(PublicKey key) {
        byte[] keyDigest = keyHash(key.getEncoded());
        byte[] addressBytes = new byte[keyDigest.length + 4];
        // note: now leaving out BTC's first byte identifier
        System.arraycopy(keyDigest, 0, addressBytes, 0, keyDigest.length);
        byte[] check = hash(addressBytes, 0, keyDigest.length);
        System.arraycopy(check, 0, addressBytes, keyDigest.length, 4);
        return toBase58(addressBytes);
    }

    /** The actual value to go into the Feed's <id> tag, prefixed with a URN */
    public static final String toFeedUrn(String feedId) {
        if (feedId != null) {
            if (!feedId.startsWith(FEED_URN_PREFIX)) {
                feedId = FEED_URN_PREFIX + feedId;
            }
        }
        return feedId;
    }

    /** Returns the Feed URN prefix from the given input, if it exists */
    public static final String removeFeedUrnPrefix(String feedUrn) {
        if (feedUrn.startsWith(FEED_URN_PREFIX)) {
            return feedUrn.substring(FEED_URN_PREFIX.length());
        }
        return feedUrn;
    }

    /**
     * Uses the checksum in the last 4 bytes of the decoded data to verify the rest are correct. The checksum is removed from the returned data.
     * Returns null if invalid. Borrowed from bitcoinj.
     */
    static final byte[] decodeChecked(String input) {
        byte tmp[];
        try {
            tmp = fromBase58(input);
        } catch (IllegalArgumentException e) {
            log.trace("decodeChecked: could not decode: " + input);
            return null;
        }
        if (tmp.length < 4) {
            log.trace("decodeChecked: input too short: " + input);
            return null;
        }
        byte[] bytes = copyOfRange(tmp, 0, tmp.length - 4);
        byte[] checksum = copyOfRange(tmp, tmp.length - 4, tmp.length);

        tmp = doubleDigest(bytes, 0, bytes.length);
        byte[] hash = copyOfRange(tmp, 0, 4);
        if (!Arrays.equals(checksum, hash)) {
            log.trace("decodeChecked: checksum does not validate: " + input);
            return null;
        }
        log.trace("decodeChecked: input is valid: " + input);
        return bytes;
    }

    private static final byte[] keyHash(byte[] key) {
        try {
            byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(key);
            return ripemd160(sha256);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static final byte[] hash(byte[] data, int offset, int len) {
        try {
            MessageDigest a = MessageDigest.getInstance("SHA-256");
            a.update(data, offset, len);
            return a.digest(a.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toBase58(byte[] b) {
        if (b.length == 0) {
            return "";
        }

        int lz = 0;
        while (lz < b.length && b[lz] == 0) {
            ++lz;
        }

        StringBuffer s = new StringBuffer();
        BigInteger n = new BigInteger(1, b);
        while (n.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] r = n.divideAndRemainder(BigInteger.valueOf(58));
            n = r[0];
            char digit = b58[r[1].intValue()];
            s.append(digit);
        }
        while (lz > 0) {
            --lz;
            s.append("1");
        }
        return s.reverse().toString();
    }

    private static byte[] fromBase58(String s) {
        try {
            boolean leading = true;
            int lz = 0;
            BigInteger b = BigInteger.ZERO;
            for (char c : s.toCharArray()) {
                if (leading && c == '1') {
                    ++lz;
                } else {
                    leading = false;
                    b = b.multiply(BigInteger.valueOf(58));
                    b = b.add(BigInteger.valueOf(r58[c]));
                }
            }
            byte[] encoded = b.toByteArray();
            if (encoded[0] == 0) {
                if (lz > 0) {
                    --lz;
                } else {
                    byte[] e = new byte[encoded.length - 1];
                    System.arraycopy(encoded, 1, e, 0, e.length);
                    encoded = e;
                }
            }
            byte[] result = new byte[encoded.length + lz];
            System.arraycopy(encoded, 0, result, lz, encoded.length);

            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid character in address");
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static final byte[] ripemd160(byte[] data) {
        byte[] ph = new byte[20];
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(data, 0, data.length);
        digest.doFinal(ph, 0);
        return ph;
    }

    private static final byte[] copyOfRange(byte[] source, int from, int to) {
        byte[] range = new byte[to - from];
        System.arraycopy(source, from, range, 0, range.length);
        return range;
    }

    /**
     * Calculates the SHA-256 hash of the given byte range, and then hashes the resulting hash again. This is standard procedure in Bitcoin. The
     * resulting hash is in big endian form. Borrowed from bitcoinj.
     */
    private static final byte[] doubleDigest(byte[] input, int offset, int length) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            log.error("Should never happen: could not find SHA-256 MD algorithm", e);
            return null;
        }
        digest.reset();
        digest.update(input, offset, length);
        byte[] first = digest.digest();
        return digest.digest(first);
    }
}
