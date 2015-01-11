package com.completetrsst.crypto.keys;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Original Trsst code
 * 
 * @author mpowers
 */
public class HashCashStamper {

	private static final Logger log = LoggerFactory.getLogger(HashCashStamper.class);

	private static final int BIT_STRENGTH = 20;

	/**
	 * Creates a hashcash stamp for signing messages. 
	 * 
	 * FeedId here has no urn:uuid prefix
	 */
	public static final String computeStamp(long entryUpdatedMillis, String feedId) {
		try {
			if (feedId.indexOf(':') != -1) {
				feedId = feedId.replace(":", ".");
			}
			String formattedDate = new SimpleDateFormat("YYMMdd").format(new Date(entryUpdatedMillis));
			String prefix = "1:" + Integer.toString(BIT_STRENGTH) + ":" + formattedDate + ":" + feedId + "::"
			        + Long.toHexString(entryUpdatedMillis) + ":";
			int masklength = BIT_STRENGTH / 8;
			byte[] prefixBytes = prefix.getBytes("UTF-8");
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

			int i;
			int b;
			byte[] hash;
			long counter = 0;
			while (true) {
				sha1.update(prefixBytes);
				sha1.update(Long.toHexString(counter).getBytes());
				hash = sha1.digest(); // 20 bytes long
				for (i = 0; i < 20; i++) {
					b = (i < masklength) ? 0 : 255 >> (BIT_STRENGTH % 8);
					if (b != (b | hash[i])) {
						// no match; keep trying
						break;
					}
					if (i == masklength) {
						// we're a match: return the stamp
						// System.out.println(Common.toHex(hash));
						return prefix + Long.toHexString(counter);
					}
				}
				counter++;
				// keep going forever until we find it
			}
		} catch (UnsupportedEncodingException e) {
			log.error("No string encoding found: ", e);
		} catch (NoSuchAlgorithmException e) {
			log.error("No hash algorithm found: ", e);
		}
		log.error("Exiting without stamp: should never happen");
		return null;
	}
}
