package com.completetrsst.crypto.keys;

import java.security.KeyPair;
import java.security.PublicKey;
import static org.junit.Assert.*;

import org.junit.Test;

public class TrsstKeyFunctionsTest {

	private static final KeyPair keyPair = new EllipticCurveKeyCreator().createKeyPair();
	private static final PublicKey key = keyPair.getPublic();
	
	/** Verifies that a feed ID created from a public key is valid */
	@Test
	public void toFeedId() {
		String feedId = TrsstKeyFunctions.toFeedId(key);
		assertEquals(33, feedId.length());
		
		// This verifies the above id.  Returns null if invalid
		byte [] result = TrsstKeyFunctions.decodeChecked(feedId);
		assertNotNull(result);
		System.out.println(new String(result));
	}

}
