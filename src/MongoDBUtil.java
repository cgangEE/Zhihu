import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoOptions;


public class MongoDBUtil {
	private static Mongo mongo = null;
	
	private static String DBString = "zhihu";
	private static String hostName = "localhost";
	private static int port = 27017;
	private static int poolSize = 2000;
	
	private MongoDBUtil() {
	}
	
	public static DB getDB(){
		if (mongo == null)
			init();
		return mongo.getDB(DBString);
	}
	
	private static void init(){
		try {
			mongo = new Mongo(hostName, port);
			MongoOptions opt = mongo.getMongoOptions();
			opt.connectionsPerHost = poolSize;
		} catch(Exception e){
			System.out.println("Mongo"+e.toString());
		}
	}
}
