package com.cuga.completetrsst.model;

import java.util.Date;
import java.util.Random;

import de.svenjacobs.loremipsum.LoremIpsum;

public class SampleContent {
	
	private static final LoremIpsum textGenerator = new LoremIpsum();
	private static final Random random = new Random();	
	private static int idCounter = 0;
	
	private String author;
	
	private Date publicationDate;
	
	private String text;
	
	private int id;
	
	public static SampleContent generateContent(String author, Date date) {
		SampleContent content = new SampleContent();
		content.author = author;
		content.publicationDate = date;
		content.id = idCounter++;
		content.text = textGenerator.getParagraphs(random.nextInt(9) + 1);
		
		return content;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public Date getPublicationDate() {
		return publicationDate;
	}
	
	public String getText() {
		return text;
	}

	public int getId() {
		return id;
	}
}