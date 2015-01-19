package com.completetrsst.spring.store;

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
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;

// on indexes: https://github.com/orientechnologies/orientdb/wiki/Performance-Tuning#use-of-indexes
// on java api: https://github.com/orientechnologies/orientdb/wiki/Tutorial-Java

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
	public void storeFeed(String feedId, Date dateUpdated, String rawFeedXml) {
		log.info("Call to orient store feed with id " + feedId);

		// TODO: Check first if the feed exists and do an update

		ODatabaseDocumentTx db = null;
		ODocument feed;
		try {
			db = openDatabase();
			db.begin();
			
			// TODO: Fetch query by ID
			// TODO: Move this outside the transaction
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select from Feed where id = ? limit 1");
			List<ODocument> results = db.command(query).execute(feedId);
			if (results.size() ==0) {
				// create new feed 
				feed = new ODocument("Feed");
				feed.field("id", feedId);
				feed.field("date", dateUpdated);
				feed.field("xml", rawFeedXml);
			} else {
				// update existing feed
				feed = results.get(0);
				feed.field("date", dateUpdated);
				feed.field("xml", rawFeedXml);
			}
			feed.save();
			db.commit();
			
		} finally {
			closeDatabase(db);
		}

		log.info("Feed should be saved!");
	}

	// TODO: have entry title passed in
	// TODO: If have link to feed, should feed and entry be created at same time? i.e. add feed fields?
	@Override
	public void storeEntry(String feedId, String entryId, Date dateEntryUpdated, String rawEntryXml) {
		log.info("Call to orient store entry with id " + entryId);
		ODatabaseDocumentTx db = null;
		try {
			db = openDatabase();
			ODocument entry = new ODocument("Entry");
			entry.field("feedId", feedId);
			entry.field("id", entryId);
			entry.field("date", dateEntryUpdated);
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
		try {
			db = openDatabase();
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select from Feed where id = ? limit 1");
			results = db.command(query).execute(feedId);
			return results.size() == 0 ? "" : results.get(0).field("xml");
		} catch (Exception e) {
			// TODO: First time a db is ever used, it'll throw an exception
			// handle this when the client's GUI is being created
			log.error("Error getting feed: " + feedId, e);
			log.error(e.getMessage());
		}finally {
			closeDatabase(db);
		}
		// all fetching has to happen before the db is closed
		return "";
	}

	// TODO: Have this overridden to take a date and return last 50 from that date
	@Override
	public List<String> getLatestEntries(String feedId) {
		ODatabaseDocumentTx db = null;
		List<ODocument> results = new ArrayList<ODocument>(0);
		try {
			db = openDatabase();
			
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select from Entry where feedId = ? order by date desc limit 50");
			results = db.command(query).execute(feedId);
			
			log.info("Got " + results.size() + " entries for feed");
			List<String> xml = new ArrayList<String>(results.size());
			results.forEach(result -> xml.add(result.field("xml")));
			return xml;
		} catch (Exception e) {
				log.error("Error getting entries on feed: " + feedId, e);
				log.error(e.getMessage());
		}finally {
			closeDatabase(db);
		}
		
		return Collections.<String>emptyList();
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
		server = OServerMain.create();
		server.startup(getClass().getResourceAsStream(databaseConfigFile));
		server.activate();

		ODatabaseDocumentTx tx = new ODatabaseDocumentTx(dbUrl);
		try {
		if (!tx.exists()) 
		{
			tx.create();
			OClass feed = tx.getMetadata().getSchema().createClass("Feed");
			feed.createProperty("id", OType.STRING);
			feed.createProperty("date", OType.DATETIME);
			feed.createProperty("xml", OType.STRING);
			feed.createIndex("Feed.id", OClass.INDEX_TYPE.UNIQUE, "id");
			
			OClass entry = tx.getMetadata().getSchema().createClass("Entry");
			entry.createProperty("feedId", OType.STRING);
			entry.createProperty("id", OType.STRING);
			entry.createProperty("title", OType.STRING);
			entry.createProperty("date", OType.DATETIME);
			entry.createProperty("xml", OType.STRING);
			
		}} finally {
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
