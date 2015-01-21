package com.completetrsst.store;

import java.util.List;

public interface Storage {

    public void storeFeed(String feedId, String rawFeedXml);

    public void storeEntry(String feedId, String entryTitle, String rawEntryXml);

    public String getFeed(String feedId);

    // TODO: Add optional date param to do paging
    public List<String> getLatestEntries(String feedId);
    
//    public List<String> searchEntries(String searchString);
}
