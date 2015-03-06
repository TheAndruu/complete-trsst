package com.completetrsst.crypto.keys;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;

import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assumes elsewhere that BouncyCastle has been added as a provider with Java
 * Security
 */
public class EllipticCurveKeyCreator implements KeyCreator {

    private static final Logger log = LoggerFactory.getLogger(EllipticCurveKeyCreator.class);
    private static final String CURVE_256K1 = "secp256k1";

    static {
        try {
            int result = Security.addProvider(new BouncyCastleProvider());
            log.debug("Result of BC registration: " + result);
        } catch (Exception e) {
            log.error("Could not initialize security provider: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public KeyPair createKeyPair() {
        try {
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(CURVE_256K1);
            KeyPairGenerator g = new KeyPairGeneratorSpi.EC();
            g.initialize(ecSpec, new SecureRandom());
            return g.generateKeyPair();
        } catch (Exception e) {
            log.error("Couldn't create keypair: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
