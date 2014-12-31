package com.completetrsst.operations;

import java.util.List;

import com.completetrsst.model.Story;

public interface StoryOperations {

	public void create(String publisherId, Story story);

	public List<Story> getStories(String publisherId);
}
