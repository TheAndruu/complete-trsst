package com.cuga.completetrsst.operations;

import java.util.List;

import com.cuga.completetrsst.model.Story;

public interface StoryOperations {

	public void create(String publisherId, Story story);

	public List<Story> getStories(String publisherId);
}
