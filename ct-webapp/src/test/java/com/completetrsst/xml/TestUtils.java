package com.completetrsst.xml;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import com.rometools.rome.feed.atom.Entry;

public class TestUtils {

	public static final String PLAIN_ATOM_ENTRY;
	static {
		PLAIN_ATOM_ENTRY = TestUtils.class.getResource("plainAtomEntry.xml").getPath();
	}

	public static String readFile(String path) throws IOException {
		return new String(readAllBytes(get(path)));
	}

	public static Entry createSimpleEntry() {
		Entry entry = new Entry();
		entry.setId(UUID.randomUUID().toString());
		entry.setTitle("Title: create entry for element conversion test");
		entry.setUpdated(new Date());
		entry.setPublished(new Date());
		return entry;
	}
}
