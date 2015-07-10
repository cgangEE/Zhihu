import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;


public class DAO {
	public boolean insert(String collectionName, BasicDBObject bean){
		DB db = MongoDBUtil.getDB();
		db.getCollection(collectionName).insert(bean);
		return false;
	}
	
	public boolean delete(String collectionName, BasicDBObject bean){
		DB db = MongoDBUtil.getDB();
		db.getCollection(collectionName).remove(bean);
		return false;
	}
	
	public List find(String collectionName, BasicDBObject bean){
		DB db = MongoDBUtil.getDB();
		List list = db.getCollection(collectionName).find(bean).toArray();
		return list;
	}
	
	public boolean update(String collectionName, BasicDBObject oldBean, BasicDBObject newBean){
		DB db = MongoDBUtil.getDB();
		db.getCollection(collectionName).update(oldBean, newBean);
		return false;
	}
}
