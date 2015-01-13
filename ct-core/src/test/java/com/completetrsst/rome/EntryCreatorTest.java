package com.completetrsst.rome;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.rometools.rome.feed.atom.Entry;

public class EntryCreatorTest {

	@Test
	public void testNewEntryId() {
		String id = EntryCreator.newEntryId();
		assertTrue(id.startsWith(EntryCreator.ENTRY_ID_PREFIX));
		String justUuid = id.substring(EntryCreator.ENTRY_ID_PREFIX.length());
		// Type 4 UUID
		assertTrue(justUuid.substring(13,15).equals("-4"));
		assertTrue(justUuid.substring(18,19).equals("-"));
		String y = justUuid.substring(19,20);
		List<String> allowedY = Arrays.asList("8", "9", "A", "B", "a", "b");
		assertTrue(allowedY.contains(y));
	}

	@Test
	public void createEntry() {
		Entry entry = EntryCreator.create("my title");
		assertEquals("my title", entry.getTitle());
		assertTrue(entry.getUpdated().toInstant().isBefore(new Date().toInstant().plusMillis(1L)));
		assertTrue(entry.getId().startsWith(EntryCreator.ENTRY_ID_PREFIX));
		
	}
}
