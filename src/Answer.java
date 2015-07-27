import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;

public class Answer extends Thread {
	static Set<String> answerSet = null;

	static ArrayList<Integer> getNumber(String s) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		boolean flag = false;
		Integer tmp = 0;

		for (int i = 0; i < s.length() + 1; ++i) {
			if (i < s.length() && s.charAt(i) >= '0' && s.charAt(i) <= '9') {
				tmp = tmp * 10 + (s.charAt(i) - '0');
				flag = true;
			} else if (flag) {
				ret.add(tmp);
				flag = false;
				tmp = 0;
			}
		}
		return ret;
	}

	Document getDocFromUrl(String url) {
		Document doc = null;
		while (true) {
			try {
				doc = Jsoup.connect(url).cookies(cookies).get();
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return doc;
	}

	void addToDB(Integer question, Integer answer, String userName) {
		String s = question + " " + answer;
		if (answerSet.contains(s))
			return;
		answerSet.add(s);

		BasicDBObject bean = new BasicDBObject();
		bean.put("userName", userName);
		bean.put("question", question);
		bean.put("answer", answer);

		boolean finded = false;

		while (true) {
			try {
				finded = !new DAO().find("answer", bean).isEmpty();
				break;
			} catch (Exception e) {
			}
		}

		if (!finded)
			while (true) {
				try {
					new DAO().insert("answer", bean);
					break;
				} catch (Exception e) {
				}
			}
	}

	int getAnswerFromDoc(Document doc, String userName) {
		Elements elements = doc.getElementsByClass("question_link");
		int ret = elements.size();

		for (Element element : elements) {
			// System.out.println(element.html());
			// System.out.println(element.attr("href"));
			ArrayList<Integer> list = getNumber(element.attr("href"));
			addToDB(list.get(0), list.get(1), userName);
		}
		return ret;
	}

	void getAnswerFromUser(String userName) {
		String url = "http://www.zhihu.com/people/" + userName + "/answers";
		for (int i = 1;; ++i) {
			Document doc = getDocFromUrl(url + "?page=" + i);
			int cnt = getAnswerFromDoc(doc, userName);
			if (cnt == 0)
				break;
		}
	}

	@Override
	public void run() {
		String userName = null;
		while (!MyThread.queue.isEmpty()) {
			while (true) {
				try {
					userName = MyThread.queue.remove();
					break;
				} catch (Exception e) {
				}
			}
			getAnswerFromUser(userName);
		}
	}

	Map<String, String> cookies;

	Answer(Map<String, String> cookies) {
		this.cookies = cookies;
		answerSet = Collections.synchronizedSet(new HashSet<String>());
	}
}
