package com.completetrsst.spring;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.keys.FileSystemKeyManager;
import com.completetrsst.crypto.keys.KeyManager;
import com.completetrsst.operations.InMemoryStoryOps;
import com.completetrsst.operations.StoryOperations;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableWebMvc
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public StoryOperations storyOperations() {
        return new InMemoryStoryOps();
    }

    /**
     * Create the CNVR. Get Spring to inject the ContentNegotiationManager
     * created by the configurer (see previous method).
     */
    @Bean
    public ViewResolver contentNegotiatingViewResolver(ContentNegotiationManager manager) {

        ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
        Map<String, MediaType> types = new HashMap<String, MediaType>();
        // This gives us the atom feed resolver. HTLM resolver is made by
        // including thymeleaf dependency
        types.put("atom", MediaType.APPLICATION_ATOM_XML);

        MediaTypeFileExtensionResolver extensions = new MappingMediaTypeFileExtensionResolver(types);
        manager.addFileExtensionResolvers(extensions);

        resolver.setContentNegotiationManager(manager);
        
        return resolver;
    }

    @Bean
    public KeyManager getKeyManager() {
        KeyManager manager = new FileSystemKeyManager();
        manager.setKeyGenerator(new EllipticCurveKeyCreator());
        log.info("Key home: " + manager.getKeyStoreHome().toString());
        return manager;
    }

}