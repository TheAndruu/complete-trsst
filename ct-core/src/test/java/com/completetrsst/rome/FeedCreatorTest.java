package com.completetrsst.rome;

import static com.completetrsst.crypto.keys.TrsstKeyFunctions.toFeedId;
import static com.completetrsst.crypto.keys.TrsstKeyFunctions.toFeedUrn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.rome.modules.TrsstModule;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.module.Module;

public class FeedCreatorTest {
	private static final KeyPair keyPair = new EllipticCurveKeyCreator().createKeyPair();

	@Test
	public void testCreateFeed() {
		Feed feed = FeedCreator.createFor(keyPair);
		String expectedId = toFeedUrn(toFeedId(keyPair.getPublic()));
		assertEquals(expectedId, feed.getId());
		assertTrue(feed.getUpdated().before(new Date()));
		assertEquals("atom_1.0", feed.getFeedType());
		
		List<Module> modules = feed.getModules();
		assertTrue(modules.size() == 1);
		TrsstModule module = (TrsstModule) modules.get(0);
		assertTrue(module.getIsSigned());
		assertEquals(keyPair, module.getKeyPair());
	}
}
