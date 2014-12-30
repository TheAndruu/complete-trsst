package com.cuga.completetrsst.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.MappingMediaTypeFileExtensionResolver;
import org.springframework.web.accept.MediaTypeFileExtensionResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableWebMvc
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

	/**
	 * Create the CNVR. Get Spring to inject the ContentNegotiationManager
	 * created by the configurer (see previous method).
	 */
	@Bean
	public ViewResolver contentNegotiatingViewResolver(
			ContentNegotiationManager manager) {

		ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
		Map<String, MediaType> types = new HashMap<String, MediaType>();
		// This gives us the atom feed resolver. HTLM resolver is made by
		// including thymeleaf dependency
		types.put("atom", MediaType.APPLICATION_ATOM_XML);
		
		MediaTypeFileExtensionResolver extensions = new MappingMediaTypeFileExtensionResolver(
				types);
		manager.addFileExtensionResolvers(extensions);

		resolver.setContentNegotiationManager(manager);

		return resolver;
	}

}
