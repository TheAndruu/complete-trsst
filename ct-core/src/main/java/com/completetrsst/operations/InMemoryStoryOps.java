package com.completetrsst.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.completetrsst.model.Story;

public class InMemoryStoryOps implements StoryOperations {

	private Map<String, List<Story>> publishersToStories = new HashMap<String, List<Story>>();

	@Override
	public void create(String publisherId, Story story) {
		List<Story> existingStories = getStories(publisherId);
		story.setId(createUniqueId());
		existingStories.add(story);
		sortByDateDescending(existingStories);

		publishersToStories.put(publisherId, existingStories);
	}

	private String createUniqueId() {
		return UUID.randomUUID().toString();
	}

	private void sortByDateDescending(List<Story> existingStories) {
		Collections.sort(existingStories, new Comparator<Story>() {
			@Override
			public int compare(Story story1, Story story2) {
				return story2.getDatePublished().compareTo(
						story1.getDatePublished());
			}
		});
	}

	@Override
	public List<Story> getStories(String publisherId) {
		List<Story> stories = publishersToStories.get(publisherId);
		return stories == null ? new ArrayList<Story>() : stories;
	}
}
