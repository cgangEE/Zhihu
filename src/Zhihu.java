import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
		MyThread.networkSet = Collections
				.synchronizedSet(new HashSet<String>());
		Upvote.answerQueue = new ArrayBlockingQueue<String>(1000000);
		Collecting.idQueue = new ArrayBlockingQueue<String>(1000000);
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

			for (int i = 0; i < 50; ++i) {
				MyThread thread = new MyThread(cookies);
				exec.execute(thread);
			}
			exec.shutdown();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getUserInfo() {
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

	public static void getAnswer() {
		try {
			ExecutorService exec = Executors.newCachedThreadPool();
			for (int i = 0; i < 50; ++i) {
				Answer thread = new Answer(cookies);
				exec.execute(thread);
			}
			exec.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getPeopleFromDB() {
		BasicDBObject bean = new BasicDBObject();
		List<BasicDBObject> list = new DAO().find("people2", bean);
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

	public static void getAnswerFromDB() {
		BasicDBObject bean = new BasicDBObject();
		List<BasicDBObject> list = new DAO().find("answer", bean);
		for (BasicDBObject answer : list) {
			String userName = answer.getString("userName");
			String questionId = answer.getString("question");
			String answerId = answer.getString("answer");
			try {
				Upvote.answerQueue.put(userName + " " + questionId + " "
						+ answerId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	static void getUpvoteFromDB() {
		BasicDBObject bean = new BasicDBObject();
		Map<String, Integer> map = new HashMap<String, Integer>();
		List<BasicDBObject> list = new DAO().find("upvote", bean);
		for (BasicDBObject upvote : list) {
			String A = upvote.getString("A");
			String B = upvote.getString("B");
			String s = A + "\t" + B;
			Integer cnt = map.get(s);
			if (cnt == null)
				cnt = 0;
			map.put(s, ++cnt);
		}

		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			System.out.println(entry.getKey() + "\t" + entry.getValue());
		}
	}

	static void getReplyFromDB() {
		BasicDBObject bean = new BasicDBObject();
		Map<String, Integer> map = new HashMap<String, Integer>();
		List<BasicDBObject> list = new DAO().find("reply", bean);
		for (BasicDBObject reply : list) {
			String A = reply.getString("A");
			String B = reply.getString("B");
			String s = A + "\t" + B;
			Integer cnt = map.get(s);
			if (cnt == null)
				cnt = 0;
			map.put(s, ++cnt);
		}
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			System.out.println(entry.getKey() + "\t" + entry.getValue());
		}
	}

	static void getCollectingFromDB() {
		BasicDBObject bean = new BasicDBObject();
		Map<String, Integer> map = new HashMap<String, Integer>();
		List<BasicDBObject> list = new DAO().find("collecting", bean);
		for (BasicDBObject reply : list) {
			String A = reply.getString("A");
			String B = reply.getString("B");
			if( A.compareTo(B)==0) continue;
			String s = A + "\t" + B;
			Integer cnt = map.get(s);
			if (cnt == null)
				cnt = 0;
			map.put(s, ++cnt);
		}
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			System.out.println(entry.getKey() + "\t" + entry.getValue());
		}
	}

	static void getCollectionsIDFromDB() {
		BasicDBObject bean = new BasicDBObject();
		List<BasicDBObject> list = new DAO().find("collectionId", bean);
		for (BasicDBObject collection : list) {
			String id = collection.getString("id");
			int cnt = collection.getInt("cnt");
			String userName = collection.getString("userName");
			try {
				Collecting.idQueue.put(id + " " + cnt + " " + userName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	static void getAnswerQuestionFromDB() {
		BasicDBObject bean = new BasicDBObject();
		List<BasicDBObject> list = new DAO().find("question", bean);
		Map<String, Integer> userNamesToAnswerCnt = new HashMap<String, Integer>();

		for (BasicDBObject question : list) {
			String userName = question.getString("userName");
			Integer questionId = question.getInt("question");

			BasicDBObject questBean = new BasicDBObject();
			questBean.put("question", questionId);
			List<BasicDBObject> answers = new DAO().find("answer", questBean);

			// System.out.println(answers.size());
			for (BasicDBObject answer : answers) {
				String peopleName = answer.getString("userName");
			//	if (peopleName.compareTo(userName) == 0)
			//		continue;
				String s = peopleName + "\t" + userName;

				Integer cnt = userNamesToAnswerCnt.get(s);
				if (cnt == null)
					cnt = 0;
				userNamesToAnswerCnt.put(s, cnt + 1);
			}
		}

		Iterator iter = userNamesToAnswerCnt.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter
					.next();
			System.out.println(entry.getKey() + "\t" + entry.getValue());
		}
	}

	public static void getQuestion() {
		try {
			ExecutorService exec = Executors.newCachedThreadPool();
			for (int i = 0; i < 50; ++i) {
				Question thread = new Question(cookies);
				exec.execute(thread);
			}
			exec.shutdown();
		} catch (Exception e) {
		}
	}

	static void getUpvote() {

		try {
			ExecutorService exec = Executors.newCachedThreadPool();
			for (int i = 0; i < 50; ++i) {
				Upvote thread = new Upvote(cookies);
				exec.execute(thread);
			}
			exec.shutdown();
		} catch (Exception e) {
		}
	}

	static void getReply() {
		try {
			ExecutorService exec = Executors.newCachedThreadPool();
			for (int i = 0; i < 50; ++i) {
				Reply thread = new Reply(cookies);
				exec.execute(thread);
			}
			exec.shutdown();
		} catch (Exception e) {
		}

	}

	static void getCollectionsID() {
		try {
			ExecutorService exec = Executors.newCachedThreadPool();
			for (int i = 0; i < 50; ++i) {
				CollectionsID thread = new CollectionsID(cookies);
				exec.execute(thread);
			}
		} catch (Exception e) {
		}
	}

	static void getCollecting() {
		try {
			ExecutorService exec = Executors.newCachedThreadPool();
			for (int i = 0; i < 50; ++i) {
				Collecting thread = new Collecting(cookies);
				exec.execute(thread);
			}
		} catch (Exception e) {
		}
	}

	public static void main(String args[]) {
		cookies = Cookies.getCookie();
		prepare();
		// getPeople();

		getPeopleFromDB();
		// getNetwork();

		// getUserInfo();
		// getAnswer();
		// getQuestion();
		getAnswerQuestionFromDB();

		//getAnswerFromDB();
		//getUpvote();
		// getUpvoteFromDB();
		// getReply();
		// getReplyFromDB();

		// getCollectionsID();

		// getCollectionsIDFromDB();
		// getCollecting();
		// getCollectingFromDB();
	}

}
