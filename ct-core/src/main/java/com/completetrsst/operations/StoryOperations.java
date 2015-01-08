package com.completetrsst.operations;

import java.util.List;

import org.w3._2005.atom.EntryType;

import com.completetrsst.model.Story;

public interface StoryOperations {

    public void create(String publisherId, Story story);

    public List<Story> getStories(String publisherId);

    public String publishEntry(String publisherId, EntryType entryElement);
}
