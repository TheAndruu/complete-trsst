package com.completetrsst.store;

import java.util.List;

public interface Storage {

    public void storeFeed(String feedId, String dateUpdated, String rawFeedXml);

    public void storeEntry(String feedId, String entryId, String dateEntryUpdated, String rawEntryXml);

    public String getFeed(String feedId);

    public List<String> getLatestEntries(String feedId);

    // TODO: later getEntriesAfter(date)
    // TODO: later searchFeedEntries(feedId, search terms)
}
