package com.completetrsst.rome;

import java.util.Date;
import java.util.UUID;

import com.rometools.rome.feed.atom.Entry;

public class EntryCreator {

	static final String ENTRY_ID_PREFIX = "urn:uuid:";

	/**
	 * Creates an entry with the given text, simple use case for now.
	 */
	public static Entry create(String title) {
		Entry entry = new Entry();
		entry.setTitle(title);
		entry.setUpdated(new Date());
		entry.setId(newEntryId());
		return entry;
	}

	static String newEntryId() {
		return ENTRY_ID_PREFIX + UUID.randomUUID().toString();
	}

}
