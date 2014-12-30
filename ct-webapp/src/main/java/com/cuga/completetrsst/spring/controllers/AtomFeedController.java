package com.cuga.completetrsst.spring.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.cuga.completetrsst.model.SampleContent;

@Controller
public class AtomFeedController {
	
	private List<SampleContent> contentList = new ArrayList<SampleContent>();
	
	@RequestMapping(value="/content", method=RequestMethod.GET)
	public ModelAndView getContent() {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("content");
		mav.addObject("sampleContentList", contentList);
		return mav;
	}
	
	@RequestMapping(value="/content", method=RequestMethod.POST)
	public String addContent() {
		contentList.add(SampleContent.generateContent("Alef Arendsen", new Date()));
		return "redirect:content.html";
	}
}