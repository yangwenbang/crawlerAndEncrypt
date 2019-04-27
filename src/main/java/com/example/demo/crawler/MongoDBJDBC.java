package com.example.demo.crawler;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

public class MongoDBJDBC {

	private MongoClient mongoClient = null;

	/**
	 * 构造方法，连接MongoDB服务器
	 * @author ouyang
	 * @param ip
	 * @param port
	 * @return
	 */
	public MongoDBJDBC(String ip, int port) {

		if (mongoClient == null) {
			ServerAddress serverAddress = new ServerAddress(ip, port);

			// 设置连接参数
			Builder builder = MongoClientOptions.builder()
					.connectTimeout(1000 * 60) // 设置连接超时为60秒
					.maxWaitTime(1000 * 60 * 2) // 设置最大等待时间为120秒
					.connectionsPerHost(50); // 设置最大连接数为50

			MongoClientOptions options = builder.build();

			// 连接MongoDB服务,有多种方式
			try {
				// MongoClient mongoClient = new MongoClient(ip,port);
				mongoClient = new MongoClient(serverAddress, options);
			} catch (MongoException e) {
				e.printStackTrace();
			}

		}

	}

	/******** 单例模式声明开始，采用饿汉式方式生成，保证线程安全 ********************/

	// 类初始化时，自行实例化，饿汉式单例模式
	private static final MongoDBJDBC mongoDBJDBC = new MongoDBJDBC(
			"127.0.0.1", 27017);

	/**
	 * 单例的静态工厂方法
	 * @author ouyang
	 * @return
	 */
	public static MongoDBJDBC getMongoDBJDBC() {
		return mongoDBJDBC;
	}

	/************************ 单例模式声明结束 *************************************/

	/**
	 * 根据指定条件获取Document
	 * @author ouyang
	 * @param dbName
	 * @param collectionName
	 * @param keys
	 * @param values
	 * @param num
	 * @return
	 */
	public List<Document> find(String dbName, String collectionName,
			String[] keys, Object[] values, int num) {

		// 创建返回的结果集
		List<Document> resultList = new ArrayList<Document>();

		MongoDatabase db = null;

		MongoCollection<Document> dbCollection = null;

		FindIterable<Document> cursor = null;

		if (keys != null && values != null) {
			if (keys.length != values.length) {
				// 如果传来的查询参数对不对，直接返回空的结果集
				return resultList;
			} else {
				try {
					// 获取数据库实例
					db = mongoClient.getDatabase(dbName);

					// 获取数据库中指定的collection集合
					dbCollection = db.getCollection(collectionName);

					// 构建查询条件
					BasicDBObject queryObj = new BasicDBObject();

					// 填充查询条件
					for (int i = 0; i < keys.length; i++) {
						queryObj.put(keys[i], values[i]);
					}

					// 查询获取数据
					cursor = dbCollection.find(queryObj);
					if (num != -1) {
						// 判断是否是返回全部数据，num=-1返回查询全部数据，num!=-1则返回指定的num数据
						MongoCursor<Document> mongoCursor = cursor.iterator();
						while(mongoCursor.hasNext()) {
							resultList.add(mongoCursor.next());
						}
						return resultList;
					} else {
						MongoCursor<Document> mongoCursor = cursor.iterator();
						int i = 0;
						while(mongoCursor.hasNext() && i < num) {
							resultList.add(mongoCursor.next());
							i++;
							System.out.println(i);
						}
						return resultList;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return resultList;
	}

	/**
	 * 插入文档
	 * @author ouyang
	 * @param dbName
	 * @param collectionName
	 * @param documents
	 * @return
	 */
	public Boolean inSert(String dbName, String collectionName, List<Document> documents) {

		MongoDatabase db = null;

		MongoCollection<Document> dbCollection = null;

		if (documents.size() < 1) {
			return false;
		} else {
			// 获取数据库实例
			db = mongoClient.getDatabase(dbName);

			// 获取数据库中指定的collection集合
			dbCollection = db.getCollection(collectionName);

			dbCollection.insertMany(documents);
			return true;
		}

	}
	
	/**
	 * 更新文档
	 * @author ouyang
	 * @param dbName
	 * @param collectionName
	 * @param documents
	 * @return
	 */
	public Boolean update(String dbName, String collectionName, 
			BasicDBObject whereDoc,BasicDBObject updateDoc) {

		MongoDatabase db = null;

		MongoCollection<Document> dbCollection = null;

		if (whereDoc.size() > 0 && updateDoc.size() > 0) {
			return false;
		} else {
			// 获取数据库实例
			db = mongoClient.getDatabase(dbName);

			// 获取数据库中指定的collection集合
			dbCollection = db.getCollection(collectionName);

			UpdateResult updateManyResult = dbCollection.updateMany(whereDoc,
					new Document("$set",updateDoc));
			
			System.out.println("更新成功：" + updateManyResult + "个");
			
			return true;
		}

	}
	
	/**
	 * 更新一个文档
	 * @author ouyang
	 * @param dbName
	 * @param collectionName
	 * @param documents
	 * @return
	 */
	public Boolean updateOne(String dbName, String collectionName, 
			BasicDBObject whereDoc,BasicDBObject updateDoc) {

		MongoDatabase db = null;

		MongoCollection<Document> dbCollection = null;
		
		if (whereDoc.size() < 1 && updateDoc.size() < 1) {
			return false;
		} else {
			// 获取数据库实例
			db = mongoClient.getDatabase(dbName);

			// 获取数据库中指定的collection集合
			dbCollection = db.getCollection(collectionName);

			UpdateResult updateManyResult = dbCollection.updateOne(whereDoc,
					new Document("$set",updateDoc));
			System.out.println("更新成功：" + updateManyResult + "个");
			return true;
		}

	}
	
	

	/**
	 * 获取指定MongoCollection
	 * @author ouyang
	 * @param dbName
	 * @param collectionName
	 * @return
	 */
	public MongoCollection<Document> getCollection(String dbName,
			String collectionName) {
		return mongoClient.getDatabase(dbName).getCollection(collectionName);
	}

	/**
	 * 根据数据库名获取指定数据库实例
	 * @author ouyang
	 * @param dbName
	 * @return
	 */
	public MongoDatabase getDatabase(String dbName) {
		return mongoClient.getDatabase(dbName);
	}

}

