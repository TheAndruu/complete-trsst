package com.completetrsst.spring.views;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;

import com.completetrsst.model.Story;
import com.completetrsst.rome.TrsstModule;
import com.completetrsst.rome.TrsstSignatureModule;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.module.Module;

// This 'value' matches up with the name of the 'view' specified by the controller
@Component(value = "storyContent")
public class StoryFeedView extends AbstractAtomFeedView {

	@Override
	@SuppressWarnings("unchecked")
	protected void buildFeedMetadata(Map<String, Object> model, Feed feed, HttpServletRequest request) {
		List<Story> stories = (List<Story>) model.get("stories");

		// TODO: Have this feed's data also come form the database,
		// as in-- put the feed data (title, etc) on the model obj passed in
		feed.setId("id: " + UUID.randomUUID().toString());
		feed.setTitle("Sample stories");

		for (Story story : stories) {
			Date date = story.getDatePublished();
			if (feed.getUpdated() == null || date.compareTo(feed.getUpdated()) > 0) {
				feed.setUpdated(date);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Entry> buildFeedEntries(Map<String, Object> model, HttpServletRequest request,
	        HttpServletResponse response) throws Exception {
		List<Story> stories = (List<Story>) model.get("stories");

		List<Entry> entries = new ArrayList<Entry>(stories.size());

		for (Story story : stories) {
			Entry entry = new Entry();

			entry.setId(story.getId());
			entry.setTitle(story.getTitle());
			entry.setUpdated(story.getDateUpdated());
			entry.setPublished(story.getDatePublished());

			// TODO: add logic here for whether to encrypt as well
			TrsstModule module = new TrsstSignatureModule();
			module.setIsSigned(true);
			List<Module> modules = new ArrayList<Module>();
			modules.add(module);
			entry.setModules(modules);
			entries.add(entry);
		}
		return entries;

	}
}