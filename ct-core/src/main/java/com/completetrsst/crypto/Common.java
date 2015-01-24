/*
 * Copyright 2013 mpowers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.completetrsst.crypto;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * Shared utilities and constants used by both clients and servers.
 * 
 * @author mpowers
 */
public class Common {
    /**
     * Converts a X509-encoded EC key to a PublicKey.
     */
    public static PublicKey toPublicKeyFromX509(String stored) throws GeneralSecurityException {
        KeyFactory factory = KeyFactory.getInstance("EC");
        byte[] data = Base64.decodeBase64(stored);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        return factory.generatePublic(spec);
    }

    /**
     * Converts an EC PublicKey to an X509-encoded string.
     */
    public static String toX509FromPublicKey(PublicKey publicKey) throws GeneralSecurityException {
        KeyFactory factory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec spec = factory.getKeySpec(publicKey, X509EncodedKeySpec.class);
        return new Base64(0, null, true).encodeToString(spec.getEncoded());
    }
}
