import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mongodb.BasicDBObject;

public class Zhihu {
	static Map<String, String> cookies = null;

	public static void prepare() {
		MyThread.queue = new ArrayBlockingQueue<String>(1000000);
		MyThread.peopleSet = Collections.synchronizedSet(new HashSet<String>());
	}

	static void getPeople() {
		try {

			TopicUtil.queue = new ArrayBlockingQueue<String>(1000000);
			TopicUtil.queue.put("19903940");
			TopicUtil.getPeopleFromTopic(cookies);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void getNetwork() {
		try {
			ExecutorService exec = Executors.newCachedThreadPool();

			for (int i = 0; i < 10; ++i) {
				MyThread thread = new MyThread(cookies);
				exec.execute(thread);
			}
			exec.shutdown();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getPeopleFromDB() {
		BasicDBObject bean = new BasicDBObject();
		List<BasicDBObject> list = new DAO().find("people", bean);
		for (BasicDBObject x : list) {
			String name = x.getString("name");
			try {
				MyThread.queue.put(name);
				MyThread.peopleSet.add(name);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void getUserInfo(){
		try {
			ExecutorService exec = Executors.newCachedThreadPool();

			for (int i = 0; i < 10; ++i) {
				UserInfo thread = new UserInfo(cookies);
				exec.execute(thread);
			}
			exec.shutdown();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		cookies = Cookies.getCookie();
		prepare();
		// getPeople();
		getPeopleFromDB();

		// getNetwork();
		getUserInfo();
	}

}
