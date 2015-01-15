package com.completetrsst.crypto.keys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.security.PublicKey;

import org.junit.Test;

public class TrsstKeyFunctionsTest {

	private static final KeyPair keyPair = new EllipticCurveKeyCreator().createKeyPair();
	private static final PublicKey key = keyPair.getPublic();

	/** Verifies that a feed ID created from a public key is valid */
	@Test
	public void toFeedId() {
		String feedId = TrsstKeyFunctions.toFeedId(key);
		assertEquals(33, feedId.length());
		// This verifies the above id. Returns null if invalid
		byte[] result = TrsstKeyFunctions.decodeChecked(feedId);
		assertNotNull(result);
	}

	/** Verifies the feed urn is properly prefixed, etc */
	@Test
	public void toFeedUrn() {
		String feedId = TrsstKeyFunctions.toFeedId(key);

		// URN should be added to this feed
		String urn = TrsstKeyFunctions.toFeedUrn(feedId);
		assertTrue(urn.length() > 33);
		assertTrue(urn.startsWith(TrsstKeyFunctions.FEED_URN_PREFIX));

		// urn should be unchanged if we send it what's already a urn
		String urn2 = TrsstKeyFunctions.toFeedUrn(urn);
		assertEquals(urn, urn2);
	}

	@Test
	public void removeFeedUrnPrefix() {
		String feedId = TrsstKeyFunctions.toFeedId(key);
		String urn = TrsstKeyFunctions.toFeedUrn(feedId);
		String noPrefix = TrsstKeyFunctions.removeFeedUrnPrefix(urn);
		assertEquals(feedId, noPrefix);
	}
}
