package com.completetrsst.model;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <pre>
 * Represents an Atom 'entry' to a 'feed'.
 * Note: We diverge from the Atom Protocol RFC in certain ways:
 * -- server cannot change values (such as ID) on any signed entries, since this would break the XML signature
 * 
 * Required Entry fields:
 * -- id
 * -- title 
 * -- updated
 * -- and more, but this is good to start
 * 
 * Atom protocol:
 * {@see <a href="https://tools.ietf.org/html/rfc5023">https://tools.ietf.org/html/rfc5023</a>}
 * Entry definition:
 * {@see <a href="https://tools.ietf.org/html/rfc4287#page-11">https://tools.ietf.org/html/rfc4287#page-11</a>}
 * </pre>
 */
public class CtEntry implements Comparable<CtEntry> {
	private String id;
	private String title;
	// This should be ISO 8601.1988 compliant
	// for usage see:
	// http://stackoverflow.com/questions/22463062/how-to-parse-format-dates-with-localdatetime-java-8
	private OffsetDateTime dateUpdated;
	private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public OffsetDateTime getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(OffsetDateTime dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getDateUpdatedIso() {
		return formatter.format(dateUpdated);
	}

	public void setDateUpdated(String isoString) {
		setDateUpdated(OffsetDateTime.parse(isoString));
	}

	/** Returns the latest/newest first by date updated (descending order) */
	@Override
	public int compareTo(CtEntry other) {
		return other.getDateUpdated().compareTo(getDateUpdated());
	}
}
