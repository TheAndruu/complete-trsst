package com.completetrsst.rome;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;

import com.completetrsst.crypto.keys.TrsstKeyFunctions;
import com.completetrsst.rome.modules.TrsstModule;
import com.completetrsst.rome.modules.TrsstSignatureModule;
import com.rometools.rome.feed.atom.Feed;

public class FeedCreator {

	// Creates a feed for the given PublicKey, which determines the feed's ID
	public static Feed createFor(KeyPair keyPair) {
		Feed feed = new Feed("atom_1.0");
		feed.setUpdated(new Date());
		PublicKey publicKey = keyPair.getPublic();
		String id = TrsstKeyFunctions.toFeedUrn(TrsstKeyFunctions.toFeedId(publicKey));
		feed.setId(id);
		feed.setModules(Arrays.asList(createModule(keyPair)));
		return feed;
	}
	
	private static TrsstModule createModule(KeyPair keyPair) {
		TrsstSignatureModule module = new TrsstSignatureModule();
		module.setIsSigned(true);
		module.setKeyPair(keyPair);
		return module;
	}
}
