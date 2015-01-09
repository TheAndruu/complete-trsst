package com.completetrsst.operations;

import java.util.List;

import com.completetrsst.model.CtEntry;

public interface StoryOperations {

    public void create(String publisherId, CtEntry story);

    public List<CtEntry> getStories(String publisherId);

    public String publishEntry(String publisherId, String xml);
}
