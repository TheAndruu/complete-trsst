package com.completetrsst.spring.views;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom2.Element;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;

import com.completetrsst.model.Story;
import com.completetrsst.rome.Bar;
import com.completetrsst.rome.Foo;
import com.completetrsst.rome.SampleModule;
import com.completetrsst.rome.SampleModuleImpl;
import com.rometools.rome.feed.atom.Content;
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
			entry.setUpdated(story.getDatePublished());

			Content content = new Content();
			content.setValue(story.getContent());
			content.setType(story.getContentType());
			entry.setSummary(content);
			List<Element> markup = new ArrayList<Element>();
			Element element = new Element("stamp", "http://trsst.com/spec/0.1");
			// TODO: This how to set signature-- as a new module?
			SampleModule module = new SampleModuleImpl();
			Foo foo = new Foo();
			Bar bar = new Bar();
			bar.setItem("bar item");
			foo.setBar(bar);
			module.setFoo(foo);
			List<Module> modules = new ArrayList<Module>();
			modules.add(module);
			entry.setModules(modules);
			element.addContent("signature go here?");
			markup.add(element);
			entry.setForeignMarkup(markup);
			entries.add(entry);
		}
		return entries;

	}
}