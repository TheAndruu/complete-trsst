package com.completetrsst.spring.store;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import com.completetrsst.store.Storage;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;

// on indexes: https://github.com/orientechnologies/orientdb/wiki/Performance-Tuning#use-of-indexes
// on java api: https://github.com/orientechnologies/orientdb/wiki/Tutorial-Java

public class OrientStore implements Storage, InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(OrientStore.class);
    private OServer server = null;
    
    @Value("${orient.config.file}")
    private String databaseConfigFile;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Starting up orient server");
        server = OServerMain.create();
        server.startup(getClass().getResourceAsStream(databaseConfigFile));
        server.activate();

    }

    @Override
    public void destroy() throws Exception {
        log.info("Shutting down orient server");
        if (server != null) {
            server.shutdown();
        }
    }

    @Override
    public void storeFeed(String feedId, String dateUpdated, String rawFeedXml) {
        // TODO Auto-generated method stub
        log.info("Call to orient store feed");
    }

    @Override
    public void storeEntry(String feedId, String entryId, String dateEntryUpdated, String rawEntryXml) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getFeed(String feedId) {
        log.info("Request to orient for feed with id: " + feedId);
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getLatestEntries(String feedId) {
        // TODO Auto-generated method stub
        return null;
    }

    // see
    // http://www.orientechnologies.com/docs/2.0/orientdb.wiki/Embedded-Server.html

}
