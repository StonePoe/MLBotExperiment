package util;

import static com.mongodb.client.model.Filters.eq;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.MongoServerException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;


/**
 * Access the data from MongoDB.
 * 
 * @author Stone
 * 
 */
public class MongodbConnector {
	 /**
     * log4j logger
     */
    private static final Logger LOGGER = Logger.getLogger(MongodbConnector.class);

    /**
     * properties file
     */
    private static final String PROPERTIES = "/mongo.properties";
    
    // properties tags
    private static final String TAG_HOST = "mongodb.host";
    private static final String TAG_PORT = "mongodb.port";
    private static final String TAG_DB = "mongodb.dbname";
    
    // MongoDB client
    private MongoClient mongoClient = null; 
    
    // MongoDB database name
    private String dbName = null;
    
    // src.main.java.util.MongodbConnector singleton
    private static MongodbConnector mongodbConnector = null;
    
    /**
     * Singleton holder of MongodbClient.
     */
    public static MongodbConnector getInstance(String dbName) {
    	if(mongodbConnector == null) {
    		mongodbConnector = new MongodbConnector(dbName);
    	}
    	else if(!mongodbConnector.getDbName().equals(dbName)) {
    		mongodbConnector.dbName = dbName;
    	}
		return mongodbConnector;
    }
    
    public static MongodbConnector getInstance() {
    	if(mongodbConnector == null) {
    		mongodbConnector = new MongodbConnector();
    	}
    	return mongodbConnector;
    }

    private MongodbConnector(String dbName) {
        init();
        this.dbName = dbName;
    }

    private MongodbConnector() {
    	init();
    }
    /**
     * Get the instance of MongodbClient.
     * 
     * @return
     */
    private void init() {
        // load configuration
        InputStream inputStream = null;
        inputStream = this.getClass().getResourceAsStream(PROPERTIES);

        Properties props = new Properties();
        try {
            props.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String host = props.getProperty(TAG_HOST);
        int port = Integer.parseInt(props.getProperty(TAG_PORT));
        
        try {
        	 mongoClient = new MongoClient(host, port);
        	 dbName = props.getProperty(TAG_DB);
        } catch (MongoServerException e) {
            e.printStackTrace();
            LOGGER.error("mongodb initialization failure: " + e.getMessage());
        }
    }
    
    public String getDbName() {
	    return this.dbName;
    }
    
    public MongoCollection<Document> getCollection(String colName) {
    	return mongoClient.getDatabase(dbName).getCollection(colName);
    }
    
    /**
     * Get one record in a json string from a collection.
     * 
     * @param key
     *            key
     * @param value
     *            value
     * @param colName
     *            collection name
     * @return record in json string
     */
    public String getOne(String key, String value, String colName) {
        MongoCollection<Document> collection = getCollection(colName);
        Document doc = null;
        try {
            doc = collection.find(eq(key, value)).projection(Projections.excludeId()).first();
        } catch (MongoException e) {
            e.printStackTrace();
            LOGGER.error("could not get a resource from mongodb: " + e.getMessage());
        }
        return doc == null ? null : doc.toJson();
    }
    
    
    /**
     * Get multiple records in a json string list from a collection.
     * 
     * @param key
     *            key
     * @param value
     *            value
     * @param colName
     *            collection name
     * @return records in json string list
     */
    public List<String> getMany(String key, String value, String colName) {
        MongoCollection<Document> collection = getCollection(colName);
        List<String> jsonList = new ArrayList<>();
        try {
            collection.find(eq(key, value)).projection(Projections.excludeId()).iterator()
                    .forEachRemaining(x -> jsonList.add(x.toJson()));
        } catch (MongoException e) {
            e.printStackTrace();
            LOGGER.error("could not get a resource from mongodb: " + e.getMessage());
        }
        return jsonList;
    }
    
    /**
     * Get one record in a json string from a collection.
     * 
     * @param map
     *            query map
     * @param colName
     *            collection name
     * @return record in json string
     */
    public String getOne(Map<String, Object> map, String colName) {
        MongoCollection<Document> collection = getCollection(colName);

        BasicDBObject queryObject = new BasicDBObject(map);
        Document doc = null;
        try {
            doc = collection.find(queryObject).projection(Projections.excludeId()).first();
        } catch (MongoException e) {
            e.printStackTrace();
            LOGGER.error("could not get a resource from mongodb: " + e.getMessage());
        }
        return doc == null ? null : doc.toJson();
    }

    /**
     * Judge the existence of a collection.
     * 
     * @param key
     *            key
     * @param value
     *            value
     * @param colName
     *            collection name
     * @return true = exist; false = not exist
     */
    public boolean isExist(String key, String value, String colName) {
        boolean exist = false;
        MongoCollection<Document> collection = getCollection(colName);

        try {
            Document doc = collection.find(eq(key, value)).first();
            exist = (doc != null);
        } catch (MongoException e) {
            e.printStackTrace();
            LOGGER.error("could not get a resource from mongodb: " + e.getMessage());
        }

        return exist;
    }

    
    /**
     * Get all records in a json string list from a collection.
     * 
     * @param colName
     *            collection name
     * @return list of json strings
     */
    public List<String> getAll(String colName) {
        return getAll(colName, null);
    }

    /**
     * Get all records in a json string list from a collection, with field names
     * assigned.
     * 
     * @param colName
     *            collection name
     * @param fieldNames
     *            filed names in a string list
     * @return list of json strings
     */
    public List<String> getAll(String colName, List<String> fieldNames) {
        MongoCollection<Document> collection = getCollection(colName);
        List<String> resultJsonList = null;
        try {
            FindIterable<Document> docIter = null;
            if (fieldNames != null) {
                docIter = collection.find().projection(
                        Projections.fields(Projections.excludeId(), Projections.include(fieldNames)));
            } else {
                docIter = collection.find().projection(Projections.excludeId());
            }
            if (docIter != null) {
                resultJsonList = new ArrayList<String>();
                for (Document doc : docIter) {
                    resultJsonList.add(doc.toJson());
                }
            }
        } catch (MongoException e) {
            e.printStackTrace();
            LOGGER.error("could not get resources from mongodb: " + e.getMessage());
        }
        return resultJsonList;
    }

    /**
     * Insert a record to a collection.
     * 
     * @param key
     *            key
     * @param value
     *            value
     * @param colName
     *            collection name
     */
    public void insertOne(String key, String value, String colName) {
        MongoCollection<Document> collection = getCollection(colName);
        Document doc = new Document(key, value);
        try {
            collection.insertOne(doc);
        } catch (MongoException e) {
            e.printStackTrace();
            LOGGER.error("could not insert a resource into mongodb: " + e.getMessage());
        }
    }

    /**
     * Replace a record in a collection.
     * 
     * @param key
     *            key
     * @param value
     *            value
     * @param colName
     *            collection name
     */
    public void replaceOne(String key, String value, String colName) {
        MongoCollection<Document> collection = getCollection(colName);
        Document doc = new Document(key, value);
        try {
            collection.replaceOne(new Document(key, value), doc, new UpdateOptions().upsert(true));
        } catch (MongoException e) {
            e.printStackTrace();
            LOGGER.error("could not replace a resource into mongodb: " + e.getMessage());
        }
    }

    /**
     * Insert a record which is represented by a map to a collection.
     * 
     * @param map
     *            a record map
     * @param colName
     *            collection name
     */
    public void insertMap(Map<String, Object> map, String colName) {
        MongoCollection<Document> collection = getCollection(colName);
        Document doc = new Document(map);
        try {
            collection.insertOne(doc);
        } catch (MongoException e) {
            e.printStackTrace();
            LOGGER.error("could not insert a resource into mongodb: " + e.getMessage());
        }
    }

    /**
     * Insert multiple records represented by a list of bson documents to a
     * collection.
     * 
     * @param docs
     *            list of bson documents
     * @param colName
     *            collection name
     */
    public void insertDocuments(List<Document> docs, String colName) {
        MongoCollection<Document> collection = getCollection(colName);
        try {
            collection.insertMany(docs);
        } catch (MongoException e) {
            e.printStackTrace();
            LOGGER.error("could not insert resources into mongodb: " + e.getMessage());
        }
    }

    /**
     * Replace a record which is represented by a map to a collection.
     * 
     * @param map
     *            a record map
     * @param colName
     *            collection name
     * @param filter
     *            key filter for replacing
     */
    public void replaceMap(Map<String, Object> map, String colName, String... filter) {
        if (filter.length == 0) {
            insertMap(map, colName);
            return;
        }
        MongoCollection<Document> collection = getCollection(colName);
        Document doc = new Document(map);
        BasicDBObject filterDoc= new BasicDBObject();
        for (String key: filter) {
            filterDoc.append(key, map.get(key));
        }
        try {
            collection.replaceOne(filterDoc, doc, new UpdateOptions().upsert(true));
        } catch (MongoException e) {
            e.printStackTrace();
            LOGGER.error("could not get a resource from mongodb: " + e.getMessage());
        }
    }

    /**
     * Delete a record in a collection.
     * 
     * @param key
     *            key
     * @param value
     *            value
     * @param colName
     *            collection name
     */
    public void deleteOne(String key, String value, String colName) {
        MongoCollection<Document> collection = getCollection(colName);
        collection.deleteOne(eq(key, value));
    }

    /**
     * Set index in a collection.
     * 
     * @param key
     *            key for indexing
     * @param colName
     *            collection name
     */
    public void setIndex(String key, String colName) {
        MongoCollection<Document> collection = getCollection(colName);
        collection.createIndex(new BasicDBObject(key, 1));
    }

    public static void main(String[] args) {
    	MongodbConnector mongodbConnector = MongodbConnector.getInstance();
    	System.out.println(mongodbConnector.getOne("name", "test", "test"));
    }
}
