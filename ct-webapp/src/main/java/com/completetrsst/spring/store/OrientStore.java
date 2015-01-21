package com.completetrsst.spring.store;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import com.completetrsst.store.Storage;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;

public class OrientStore implements Storage, InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(OrientStore.class);

    @Value("${orient.config.file}")
    private String databaseConfigFile;

    @Value(("${orient.database.url}"))
    private String dbUrl;
    @Value(("${orient.database.username}"))
    private String dbUsername;
    @Value(("${orient.database.password}"))
    private String dbPassword;

    private OServer server = null;
    private OPartitionedDatabasePool pool = null;

    @Override
    public void storeFeed(String feedId, String rawFeedXml) {
        log.info("Call to orient store feed with id " + feedId);

        ODatabaseDocumentTx db = null;
        ODocument feed;

        OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select from Feed where id = ? limit 1");
        try {
            db = openDatabase();
            // Add transaction around this or no? Index ensures records are unique

            List<ODocument> results = db.command(query).execute(feedId);
            if (results.size() == 0) {
                // create new feed
                feed = new ODocument("Feed");
                feed.field("id", feedId);
                feed.field("date", new Date());
                feed.field("xml", rawFeedXml);
            } else {
                // update existing feed
                feed = results.get(0);
                feed.field("date", new Date());
                feed.field("xml", rawFeedXml);
            }
            feed.save();

        } finally {
            closeDatabase(db);
        }

        log.info("Feed should be saved!");
    }

    @Override
    public void storeEntry(String feedId, String entryTitle, String rawEntryXml) {
        log.info("Call to orient store entry on feed " + feedId);
        ODatabaseDocumentTx db = null;
        try {
            db = openDatabase();
            ODocument entry = new ODocument("Entry");
            entry.field("feedId", feedId);
            entry.field("title", entryTitle);
            // Don't use the date from the entry -- RFC3339 isn't millisecond
            // specific, which would lead to inaccuracies in sorting by date
            entry.field("date", new Date());
            entry.field("xml", rawEntryXml);
            entry.save();
        } finally {
            closeDatabase(db);
        }
        log.info("Entry should be saved!");
    }

    @Override
    public String getFeed(String feedId) {
        log.info("Request to orient for feed with id: " + feedId);
        ODatabaseDocumentTx db = null;
        List<ODocument> results = new ArrayList<ODocument>(0);
        OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select from Feed where id = ? limit 1");
        try {
            db = openDatabase();
            results = db.command(query).execute(feedId);
            return results.size() == 0 ? "" : results.get(0).field("xml");
        } catch (Exception e) {
            log.error("Error getting feed: " + feedId, e);
            log.error(e.getMessage());
        } finally {
            closeDatabase(db);
        }
        // all fetching has to happen before the db is closed
        return "";
    }

    // Have this overridden to take a date and return from that date, for paging
    @Override
    public List<String> getLatestEntries(String feedId) {
        ODatabaseDocumentTx db = null;
        List<ODocument> results = new ArrayList<ODocument>(0);
        OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select from Entry where feedId = ? order by date desc limit 50");
        try {
            db = openDatabase();
            results = db.command(query).execute(feedId);
            log.info("Got " + results.size() + " entries for feed");
            List<String> xml = new ArrayList<String>(results.size());
            results.forEach(result -> xml.add(result.field("xml")));
            return xml;
        } catch (Exception e) {
            log.error("Error getting entries on feed: " + feedId, e);
            log.error(e.getMessage());
            return Collections.<String> emptyList();
        } finally {
            closeDatabase(db);
        }
    }

    @Override
    public List<String> searchEntries(String searchString) {
        // TODO: Use lucene (version 4.7 currently) to tokenize search strings

        ODatabaseDocumentTx db = null;
        List<ODocument> results = new ArrayList<ODocument>(0);
        OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select * from Entry where title LUCENE ? limit 50");
        try {
            db = openDatabase();
            results = db.command(query).execute(searchString);
            List<String> entryXml = new ArrayList<String>(results.size());
            results.forEach(result -> entryXml.add(result.field("xml")));
            log.info("Got " + entryXml.size() + " entries for search");
            return entryXml;
        } catch (Exception e) {
            log.error(e.getMessage());
            return Collections.singletonList(e.getMessage());
        } finally {
            closeDatabase(db);
        }
    }

    /** Returns a live db connection which must be closed (in finally block) */
    private ODatabaseDocumentTx openDatabase() {
        return pool.acquire();
    }

    /** Close each database connection, to release it back to the pool */
    private void closeDatabase(ODatabaseDocumentTx db) {
        if (db != null) {
            db.close();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Starting up orient server");

        if (dbUrl.equalsIgnoreCase("default")) {
            dbUrl = "plocal:" + System.getProperty("user.home") + File.separator + "trsst-db";
        }
        log.info("Using db url: " + dbUrl);

        server = OServerMain.create();
        server.startup(getClass().getResourceAsStream(databaseConfigFile));
        server.activate();

        ODatabaseDocumentTx tx = new ODatabaseDocumentTx(dbUrl);
        try {
            if (!tx.exists()) {
                tx.create();
                OClass feed = tx.getMetadata().getSchema().createClass("Feed");
                feed.createProperty("id", OType.STRING);
                feed.createProperty("date", OType.DATETIME);
                feed.createProperty("xml", OType.STRING);
                feed.createIndex("Feed.id", OClass.INDEX_TYPE.UNIQUE, "id");
                feed.createIndex("Feed.date", OClass.INDEX_TYPE.NOTUNIQUE, "date");

                OClass entry = tx.getMetadata().getSchema().createClass("Entry");
                entry.createProperty("feedId", OType.STRING);
                entry.createProperty("title", OType.STRING);
                entry.createProperty("date", OType.DATETIME);
                entry.createProperty("xml", OType.STRING);
                entry.createIndex("Entry.date", OClass.INDEX_TYPE.NOTUNIQUE, "date");
                // https://github.com/orientechnologies/orientdb-lucene/wiki/Full-Text-Index
                entry.createIndex("Entry.title", "FULLTEXT", null, null, "LUCENE", new String[] { "title" });
            }
        } finally {
            tx.close();
        }

        pool = new OPartitionedDatabasePool(dbUrl, dbUsername, dbPassword);
    }

    @Override
    public void destroy() throws Exception {
        log.info("Shutting down orient server");
        if (pool != null) {
            pool.close();
        }

        if (server != null) {
            server.shutdown();
        }
    }

}
