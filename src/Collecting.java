import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;

public class Collecting extends Thread {
	static ArrayBlockingQueue<String> idQueue = null;
	Map<String, String> cookies;

	Collecting(Map<String, String> cookies) {
		this.cookies = cookies;
	}

	Document getDocFromUrl(String url) {
		Document doc = null;
		while (true) {
			try {
				doc = Jsoup.connect(url).cookies(cookies).get();
				break;
			} catch (Exception e) {
			//	e.printStackTrace();
			}
		}
		return doc;
	}

	void addToDB(String userName, String peopleName) {
		BasicDBObject bean = new BasicDBObject();
		bean.put("A", userName);
		bean.put("B", peopleName);

		while (true) {
			try {
				new DAO().insert("collecting", bean);
				break;
			} catch (Exception e) {
			}
		}
	}

	int getCollectingFromUrl(String urlAndPage, String id, String userName) {
		Document doc = getDocFromUrl(urlAndPage);
		Elements elements = doc
				.getElementsByClass("zm-item-answer-author-wrap");

		int ret = elements.size();

		for (Element element : elements) {
			Element ele = element.getElementsByTag("a").first();
			if (ele == null)
				continue;
			String peopleName = ele.attr("href");
			peopleName = peopleName.substring(8, peopleName.length());

			// System.out.println(peopleName);

			if (MyThread.peopleSet.contains(peopleName))
				addToDB(userName, peopleName);
		}
		//System.out.println(ret);
		return ret;
	}

	void getCollectingFromId(String id, int cnt, String userName) {
		String url = "http://www.zhihu.com/collection/" + id + "?page=";

		for (int i = 1;; ++i) {
			String urlAndPage = url + String.valueOf(i);
			int collectCnt = getCollectingFromUrl(urlAndPage, id, userName);
			if (collectCnt == 0)
				break;
		}
	}

	static int gao = 0;
	@Override
	public void run() {
		String id = null;
		int cnt = 0;
		String userName = null;
		while (!idQueue.isEmpty()) {
			while (true) {
				try {
					String tmp = idQueue.remove();
					String list[] = tmp.split(" ");
					id = list[0];
					cnt = Integer.valueOf(list[1]);
					userName = list[2];
					break;
				} catch (Exception e) {
				}
			}

			getCollectingFromId(id, cnt, userName);
			
			System.out.println((++gao) + " "+ id + " " + cnt + " " + userName);

		}
	}

}
