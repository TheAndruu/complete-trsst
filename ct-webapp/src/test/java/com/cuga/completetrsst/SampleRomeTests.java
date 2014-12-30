package com.cuga.completetrsst;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.SyndFeedOutput;

public class SampleRomeTests {

	@Test
	public void testCreateFeed() throws Exception {
		SyndFeed feed = createFeed();
		List<SyndEntry> entries = getEntries();
		feed.setEntries(entries);

		writeFeed(feed);
	}

	private void writeFeed(SyndFeed feed) throws Exception {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));

		SyndFeedOutput output = new SyndFeedOutput();
		output.output(feed, writer);
		
		writer.flush();
	}

	private SyndFeed createFeed() {
		SyndFeed feed = new SyndFeedImpl();
		feed.getSupportedFeedTypes();
		feed.setFeedType("atom_1.0");

		feed.setTitle("Sample Feed (created with ROME)");
		feed.setLink("http://rome.dev.java.net");
		feed.setDescription("This feed has been created using ROME (Java syndication utilities");
		return feed;
	}

	private Date getLocalTimeAsUtcDate() {
		ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		Date out = Date.from(now.toInstant());
		return out;
	}

	private List<SyndEntry> getEntries() {
		List<SyndEntry> entries = new ArrayList<SyndEntry>();
		SyndEntry entry;
		SyndContent description;

		entry = new SyndEntryImpl();
		entry.setTitle("ROME v1.0");
		entry.setLink("http://wiki.java.net/bin/view/Javawsxml/Rome01");

		entry.setPublishedDate(getLocalTimeAsUtcDate());
		description = new SyndContentImpl();
		description.setType("text/plain");
		description.setValue("Initial release of ROME");
		entry.setDescription(description);
		entries.add(entry);

		entry = new SyndEntryImpl();
		entry.setTitle("ROME v3.0");
		entry.setLink("http://wiki.java.net/bin/view/Javawsxml/Rome03");
		entry.setPublishedDate(getLocalTimeAsUtcDate());
		description = new SyndContentImpl();
		description.setType("text/html");
		description
		        .setValue("<p>More Bug fixes, mor API changes, some new features and some Unit testing</p>"
		                + "<p>For details check the <a href=\"https://rometools.jira.com/wiki/display/ROME/Change+Log#ChangeLog-Changesmadefromv0.3tov0.4\">Changes Log</a></p>");
		entry.setDescription(description);
		entries.add(entry);

		return entries;
	}

}
