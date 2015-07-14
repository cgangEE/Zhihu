import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class User {
	int agree, thank, collection, share; // achievement

	int question, answer, article, collected, edit; // engagement

	int following, follower;

	String id, name, gender, place, educate, work;

	int columns, topics;

	List<String> col = null;
	List<String> top = null;

	void setFollow(String s) {
		boolean flag = false;
		int tmp = 0, idx = 0;
		s = s + " ";
		for (int i = 0; i < s.length(); ++i) {
			if (s.charAt(i) >= '0' && s.charAt(i) <= '9') {
				tmp = tmp * 10 + (s.charAt(i) - '0');
				flag = true;
			} else {
				if (flag) {
					if (idx == 0)
						following = tmp;
					else
						follower = tmp;
					tmp = 0;
					++idx;
					flag = false;
				}
			}
		}
	}

	void setEngagement(String s) {
		boolean flag = false;
		int tmp = 0, idx = 0;
		s = s + " ";
		for (int i = 0; i < s.length(); ++i) {
			if (s.charAt(i) >= '0' && s.charAt(i) <= '9') {
				tmp = tmp * 10 + (s.charAt(i) - '0');
				flag = true;
			} else {
				if (flag) {
					if (idx == 0)
						question = tmp;
					else if (idx == 1)
						answer = tmp;
					else if (idx == 2)
						article = tmp;
					else if (idx == 3)
						collected = tmp;
					else
						edit = tmp;
					tmp = 0;
					++idx;
					flag = false;
				}
			}
		}
	}

	void setAchievement(String s) {
		boolean flag = false;
		int tmp = 0, idx = 0;
		s = s + " ";
		for (int i = 0; i < s.length(); ++i) {
			if (s.charAt(i) >= '0' && s.charAt(i) <= '9') {
				tmp = tmp * 10 + (s.charAt(i) - '0');
				flag = true;
			} else {
				if (flag) {
					if (idx == 0)
						agree = tmp;
					else if (idx == 1)
						thank = tmp;
					else if (idx == 2)
						collection = tmp;
					else
						share = tmp;
					tmp = 0;
					++idx;
					flag = false;
				}
			}
		}
	}

	void out() {
		System.out.print(id + "\t" + name + "\t" + work + "\t" + place + "\t"
				+ gender + "\t" + educate + "\t" + question + "\t" + answer
				+ "\t" + article + "\t" + collected + "\t" + edit + "\t"
				+ columns);

		for (String c : col)
			System.out.print("," + c);

		System.out.print("\t" + topics);
		for (String c : top)
			System.out.print("," + c);

		System.out.println("\t" + following + "\t" + follower + "\t" + agree
				+ "\t" + thank + "\t" + collection + "\t" + share);
	}
}

public class UserInfo extends Thread {
	Map<String, String> cookies;

	UserInfo(Map<String, String> cookies) {
		this.cookies = cookies;
	}

	int getNum(String s) {
		int ret = 0;
		for (int i = 0; i < s.length(); ++i)
			if (s.charAt(i) >= '0' && s.charAt(i) <= '9')
				ret = ret * 10 + (s.charAt(i) - '0');
		return ret;
	}

	public void getInfo(String userName) {
		Document doc = null;
		while (true) {
			try {
				doc = Jsoup
						.connect(
								MyThread.urlHome + MyThread.urlPeople
										+ userName + MyThread.urlAbout)
						.cookies(cookies).get();
				break;
			} catch (Exception e) {

			}
		}

		User user = new User();
		user.id = userName;

		Elements elements = doc.getElementsByClass("zm-profile-module-desc");
		Element element = elements.first();
		user.setAchievement(element.text());

		elements = doc.getElementsByClass("profile-navbar");
		user.setEngagement(elements.first().text());

		elements = doc.getElementsByClass("zm-profile-side-following");
		user.setFollow(elements.first().text());

		user.name = doc.getElementsByClass("zm-profile-section-name").first()
				.text();
		user.name = user.name.substring(0, user.name.length() - 6);

		user.gender = "unknown";
		if (doc.getElementsByClass("icon-profile-female").size() > 0)
			user.gender = "female";
		else if (doc.getElementsByClass("icon-profile-male").size() > 0)
			user.gender = "male";

		int idx = 0;
		elements = doc.getElementsByClass("zm-profile-module-desc");
		for (Element elem : elements) {
			if (idx == 1)
				user.work = elem.text();
			else if (idx == 2)
				user.place = elem.text();
			else if (idx == 3)
				user.educate = elem.text();
			++idx;
		}

		Elements elems = doc.getElementsByClass("zm-profile-side-section");
		elements = doc.getElementsByClass("zm-profile-side-columns");

		idx = 0;
		if (elements.size() > 0) {
			user.columns = getNum(elems.get(idx).text());
			++idx;
		}

		elements = doc.getElementsByClass("zm-profile-side-topics");
		if (elements.size() > 0) {
			user.topics = getNum(elems.get(idx).text());
		}

		user.top = new ArrayList<String>();
		user.col = new ArrayList<String>();

		TopicUtil.getTopicFromUrl(userName, cookies, user.top);
		TopicUtil.getColumnFromUrl(userName, cookies, user.col);
		sema.acquireUninterruptibly();
		user.out();
		sema.release();

	}

	static Semaphore sema = new Semaphore(1);
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

			getInfo(userName);
		}
	}

}
