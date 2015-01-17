package com.completetrsst.spring.store;

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
import com.orientechnologies.orient.core.record.impl.ODocument;
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

		ODocument feed = new ODocument("Feed");
		feed.field("id", feedId);
		feed.field("date", dateUpdated);
		feed.field("xml", rawFeedXml);
		feed.save();
		log.info("Feed should be saved!");
	}

	@Override
	public void storeEntry(String feedId, String entryId, String dateEntryUpdated, String rawEntryXml) {
		log.info("Call to orient store entry with id " + entryId);
		ODocument feed = new ODocument("Entry");
		feed.field("feedId", feedId);
		feed.field("id", entryId);
		feed.field("date", dateEntryUpdated);
		feed.field("xml", rawEntryXml);
		feed.save();
		log.info("Entry should be saved!");
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

	/** Returns a live db connection which must be closed (in finally block) */
	private ODatabaseDocumentTx openDatabase() {
		// OPEN THE DATABASE
		return pool.acquire();
	}

	/** Close each database connection, to release it back to the pool */
	private void closeDatabase(ODatabaseDocumentTx db) {
		db.close();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Starting up orient server");
		server = OServerMain.create();
		server.startup(getClass().getResourceAsStream(databaseConfigFile));
		server.activate();

		pool = new OPartitionedDatabasePool(dbUrl, dbUsername, dbPassword);

	}

	@Override
	public void destroy() throws Exception {
		log.info("Shutting down orient server");
		if (server != null) {
			server.shutdown();
		}
		if (pool != null) {
			pool.close();
		}
	}

}
