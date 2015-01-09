package com.completetrsst.spring.views;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;

import com.completetrsst.crypto.keys.KeyManager;
import com.completetrsst.model.CtEntry;
import com.completetrsst.rome.TrsstModule;
import com.completetrsst.rome.TrsstSignatureModule;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.module.Module;

// This 'value' matches up with the name of the 'view' specified by the controller
@Component(value = "storyContent")
public class StoryFeedView extends AbstractAtomFeedView {

    @Autowired
	private KeyManager keyCommander;
	
	@Override
	@SuppressWarnings("unchecked")
	protected void buildFeedMetadata(Map<String, Object> model, Feed feed, HttpServletRequest request) {
		List<CtEntry> stories = (List<CtEntry>) model.get("stories");

		// TODO: Have this feed's data also come form the database,
		// as in-- put the feed data (title, etc) on the model obj passed in
		feed.setId("id: " + UUID.randomUUID().toString());
		feed.setTitle("Sample stories");

		for (CtEntry story : stories) {
			OffsetDateTime date = story.getDateUpdated();
			if (feed.getUpdated() == null || Date.from(date.toInstant()).compareTo(feed.getUpdated()) > 0) {
				feed.setUpdated(Date.from(date.toInstant()));
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Entry> buildFeedEntries(Map<String, Object> model, HttpServletRequest request,
	        HttpServletResponse response) throws Exception {
		List<CtEntry> stories = (List<CtEntry>) model.get("stories");

		List<Entry> entries = new ArrayList<Entry>(stories.size());

		for (CtEntry story : stories) {
			Entry entry = new Entry();

			entry.setId(story.getId());
			entry.setTitle(story.getTitle());
			entry.setUpdated(Date.from(story.getDateUpdated().toInstant()));

			// TODO: add logic here for whether to encrypt as well
			TrsstModule module = new TrsstSignatureModule();
			module.setIsSigned(true);
			// TODO: This is where we can get and set the keypair to use, ideally stored on controller
			// also set 'isSigned' on controller and get it from model
			module.setKeyPair(keyCommander.getKeyPair());
			List<Module> modules = new ArrayList<Module>();
			modules.add(module);
			entry.setModules(modules);
			entries.add(entry);
		}
		return entries;

	}
}