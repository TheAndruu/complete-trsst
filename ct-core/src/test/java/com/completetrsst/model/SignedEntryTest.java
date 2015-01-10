package com.completetrsst.model;

import static org.junit.Assert.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class SignedEntryTest {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	private SignedEntry entry;

	@Before
	public void init() {
		entry = new SignedEntry();
	}

	@Test
	public void testGetDateUpdated() {
		final String testDateString = "2015-01-08T21:59:13.71-05:00";
		final OffsetDateTime testDate = OffsetDateTime.parse(testDateString);
		final OffsetDateTime now = OffsetDateTime.now();
		final String nowString = formatter.format(now);

		// set via offsetDate setter
		entry.setDateUpdated(OffsetDateTime.parse(testDateString));
		OffsetDateTime result = entry.getDateUpdated();
		assertEquals(testDate, result);
		assertEquals(testDateString, entry.getDateUpdatedIso());
		entry.setDateUpdated(now);
		assertEquals(now, entry.getDateUpdated());
		assertEquals(nowString, entry.getDateUpdatedIso());

		// set via the offset date string setter
		entry.setDateUpdated(testDateString);
		result = entry.getDateUpdated();
		assertEquals(testDate, result);
		assertEquals(testDateString, entry.getDateUpdatedIso());
		entry.setDateUpdated(now);
		assertEquals(now, entry.getDateUpdated());
		assertEquals(nowString, entry.getDateUpdatedIso());
	}

	@Test
	public void testDateSortingEntries() {
		String first = "2015-01-08T21:59:13.71-05:00";
		String second = "2015-01-08T21:59:13.71-06:00";
		String third = "2015-01-08T21:59:13.71-07:00";
		SignedEntry e1 = new SignedEntry();
		e1.setDateUpdated(first);
		SignedEntry e2 = new SignedEntry();
		e2.setDateUpdated(second);
		SignedEntry e3 = new SignedEntry();
		e3.setDateUpdated(third);
		List<SignedEntry> entries = Arrays.asList(e1, e2, e3);
		
		// order is first second third in the list, earliest first
		assertTrue(entries.get(0).getDateUpdated().isBefore(entries.get(1).getDateUpdated()));
		assertTrue(entries.get(1).getDateUpdated().isBefore(entries.get(2).getDateUpdated()));
		
		// Descending order
		Collections.sort(entries);
		assertEquals(e3, entries.get(0));
		assertEquals(e2, entries.get(1));
		assertEquals(e1, entries.get(2));
		
		// order is third, second, first, latest first
		assertTrue(entries.get(0).getDateUpdated().isAfter(entries.get(1).getDateUpdated()));
		assertTrue(entries.get(1).getDateUpdated().isAfter(entries.get(2).getDateUpdated()));
	}

}
