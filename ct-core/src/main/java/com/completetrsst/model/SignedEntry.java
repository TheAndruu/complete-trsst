package com.completetrsst.model;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <pre>
 * Represents a signable Atom 'entry' to a 'feed'.
 * 
 * Required Entry fields:
 * -- id
 * -- title 
 * -- updated
 * -- and more, but this is good to start
 * 
 * @see <a href="https://tools.ietf.org/html/rfc5023">https://tools.ietf.org/html/rfc5023</a> (Atom protocol)
 * <a href="https://tools.ietf.org/html/rfc4287#page-11">https://tools.ietf.org/html/rfc4287#page-11</a> (Entry definition)
 * </pre>
 */
public class SignedEntry implements Comparable<SignedEntry> {

	public static final String XMLNS = "http://www.w3.org/2005/Atom";

	// Signature validation precludes us from wanting to edit any of these
	// values on the server once they're signed
	private String id;
	private String title;
	// Required ISO 8601.1988 compliant
	private OffsetDateTime dateUpdated;
	private String rawXml;

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

	public String getRawXml() {
		return rawXml;
	}

	public void setRawXml(String rawXml) {
		this.rawXml = rawXml;
	}

	/** Returns the latest/newest first by date updated (descending order) */
	@Override
	public int compareTo(SignedEntry other) {
		return other.getDateUpdated().compareTo(getDateUpdated());
	}
}
