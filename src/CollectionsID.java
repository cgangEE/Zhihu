import java.util.ArrayList;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;

public class CollectionsID extends Thread {
	Map<String, String> cookies;

	public CollectionsID(Map<String, String> cookies) {
		this.cookies = cookies;
	}

	Document getDocFromUrl(String url) {
		Document doc = null;
		while (true) {
			try {
				doc = Jsoup.connect(url).cookies(cookies).get();
				break;
			} catch (Exception e) {
			}
		}
		return doc;
	}

	ArrayList<Integer> getNumbers(String s) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		int tmp = 0;
		boolean flag = false;
		for (int i = 0; i < s.length() + 1; ++i) {
			if (i < s.length() && s.charAt(i) >= '0' && s.charAt(i) <= '9') {
				flag = true;
				tmp = tmp * 10 + (s.charAt(i) - '0');
			} else if (flag) {
				flag = false;
				ret.add(tmp);
				tmp = 0;
			}
		}
		return ret;
	}

	int getCntFromUrl(String url, String uerName) {
		int cnt = 0;
		Document doc = getDocFromUrl(url);
		Element element = doc.getElementsByClass("profile-navbar").first();
		if (element == null)
			return 0;

		// System.out.println(element.text());

		ArrayList<Integer> list = getNumbers(element.text());
		if (list.size() < 5)
			return 0;
		return list.get(3);
	}

	void addToDB(String collectionId) {
		BasicDBObject bean = new BasicDBObject();
		bean.put("id", collectionId);
		while (true) {
			try {
				new DAO().insert("collectionId", bean);
				break;
			} catch (Exception e) {
			}
		}
	}

	void getCollectionsFromUrl(String url, String userName) {
		Document doc = getDocFromUrl(url);
		Elements elements = doc.getElementsByClass("zm-profile-fav-item-title");
		for (Element element : elements) {
			String collectionId = element.attr("href");
			collectionId = collectionId.substring(12, collectionId.length());
			addToDB(collectionId);
		}
	}

	void getCollectionsFromUserName(String userName) {
		String url = "http://www.zhihu.com/people/" + userName + "/collections";
		Document doc = getDocFromUrl(url);
		int cnt = getCntFromUrl(url, userName);

		//System.out.println(cnt);
		for (int i = 1; i <= (cnt + 19) / 20; ++i) {
			String urlAndPage = url + "?page=" + String.valueOf(i);
			getCollectionsFromUrl(urlAndPage, userName);
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

			if (userName.compareTo("cgangee") == 0)
				continue;
			getCollectionsFromUserName(userName);
			System.out.println(userName);

		}

	}

}
