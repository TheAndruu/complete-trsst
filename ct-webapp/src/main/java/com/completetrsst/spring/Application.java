package com.completetrsst.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.completetrsst.crypto.keys.EllipticCurveKeyCreator;
import com.completetrsst.crypto.keys.FileSystemKeyManager;
import com.completetrsst.crypto.keys.KeyManager;
import com.completetrsst.operations.CompleteTrsstOps;
import com.completetrsst.operations.TrsstOperations;
import com.completetrsst.spring.store.OrientStore;
import com.completetrsst.store.Storage;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application extends SpringBootServletInitializer {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	// how to discover port at runtime:
	// http://docs.spring.io/spring-boot/docs/current/reference/html/howto-embedded-servlet-containers.html

	// TODO: Remove spring boot actuator since we have other ways of getting shutdowns to fire
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(Application.class);
		ConfigurableApplicationContext context = app.run(args);
		// Once the GUI is running, can shut down with this line and remove
		// spring boot actuator
		// SpringApplication.exit(context);
		// or
		// ((ConfigurableApplicationContext)context).close();
	}

	// Must let Spring control the scope for it to register init /shutdown hooks
	@Bean
	public Storage getStorage() {
		return new OrientStore();
	}

	// use the bean provided by spring for storage
	@Bean
	public TrsstOperations trsstOperations() {
		CompleteTrsstOps operations = new CompleteTrsstOps();
		operations.setStorage(getStorage());
		return operations;
	}

	// TODO: Remove the key manager-- should only be relevant on the client
	@Bean
	public KeyManager keyManager() {
		KeyManager manager = new FileSystemKeyManager();
		manager.setKeyGenerator(new EllipticCurveKeyCreator());
		log.info("Key home: " + manager.getKeyStoreHome().toString());
		return manager;
	}

	// Used when deploying to a standalone servlet container, i.e. tomcat
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

}