import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;

public class MyThread extends Thread {
	Map<String, String> cookies;
	static String urlHome = "http://www.zhihu.com";
	static String urlLogin = "/login";
	static String urlPeople = "/people/";
	static String urlTopic = "/topic/";
	static String urlFollowees = "/followees";
	static String urlFollowers = "/followers";
	static String urlFolloweesNext = "/ProfileFolloweesListV2";
	static String urlFollowersNext = "/ProfileFollowersListV2";
	static String urlNode = "/node";
	static String peoplePatter = urlHome + urlPeople;

	static ArrayBlockingQueue<String> queue = null;
	static Set<String> peopleSet = Collections
			.synchronizedSet(new HashSet<String>());

	/*
	 * static double falsePositiveProbability = 0.0002; static int expectedSize
	 * = 3000000; static BloomFilter<String> bloomFilter = new
	 * BloomFilter<String>( falsePositiveProbability, expectedSize);
	 */

	MyThread(Map<String, String> cookies) {
		this.cookies = cookies;
	}

	public void run() {
		scrabWeb(cookies);
	}

	static final Semaphore semp = new Semaphore(1);

	int getPeopleNamesFromDoc(Document doc, String type, String userName) {
		int ret = 0;
		Elements links = doc.getElementsByTag("a");
		for (Element link : links) {
			String url = link.attr("abs:href");
			if (url.matches("^" + peoplePatter + ".*")) {
				String peopleName = url.substring(peoplePatter.length(),
						url.length());
				if (!peopleName.matches(".*/.*")) {
					if (peopleName.compareTo("edit") == 0)
						continue;
					++ret;

					if (peopleSet.contains(peopleName)) continue;
					peopleSet.add(peopleName);
					
					BasicDBObject bean = new BasicDBObject();
					bean.put("peopleId", peopleName);
					
					/*
					 * if (bloomFilter.contains(peopleName)) { boolean flag =
					 * false; while (true) { try { flag = (!new
					 * DAO().find("people", bean) .isEmpty()); break; } catch
					 * (Exception e) { } } if (flag) continue; } else
					 * bloomFilter.add(peopleName);
					 */

					while (true) {
						try {
							DAO dao = new DAO();
							dao.insert("people", bean);
							break;
						} catch (Exception e) {

						}
					}

					while (true) {
						try {
							queue.add(peopleName);
							break;
						} catch (Exception e) {
							// e.printStackTrace();
						}
					}
				}

			}

		}

		return ret;
	}

	boolean getHashidAndXsrf(Document doc, StringBuffer hash_id,
			StringBuffer _xsrf) {
		Elements elements = doc.select("div.zh-general-list");

		if (elements.isEmpty())
			return false;

		Element element = elements.first();
		String str = element.attr("data-init");
		int pos = str.indexOf("hash_id");
		str = str.substring(pos + 11, pos + 43);
		hash_id.append(str);

		elements = doc.getElementsByAttributeValue("name", "_xsrf");
		if (elements.isEmpty())
			return false;

		element = elements.first();
		_xsrf.append(element.attr("value"));

		return true;
	}

	void getPeopleNameFromUrl(String userName, String page, String pageNext,
			String type, Map<String, String> cookies) {
		Document doc = null;
		while (true) {
			try {
				doc = Jsoup.connect(urlHome + urlPeople + userName + page)
						.cookies(cookies).get();
				break;
			} catch (Exception e) {
			}
		}
		// getPeopleNamesFromDoc(doc, type, userName);

		StringBuffer hash_id = new StringBuffer();
		StringBuffer _xsrf = new StringBuffer();

		if (!getHashidAndXsrf(doc, hash_id, _xsrf))
			return;

		for (int offset = 0;; offset += 20) {

			JSONObject jsonobj = new JSONObject();

			try {
				jsonobj.put("offset", offset);
				jsonobj.put("order_by", "created");
				jsonobj.put("hash_id", hash_id);
			} catch (Exception e) {
				e.printStackTrace();
			}

			while (true) {
				try {
					doc = Jsoup
							.connect(urlHome + urlNode + pageNext)
							.timeout(10000)
							.data("method", "next", "_xsrf", _xsrf.toString(),
									"params", jsonobj.toString())
							.method(Method.POST).cookies(cookies).get();
					break;
				} catch (Exception e) {
				}
			}
			if (getPeopleNamesFromDoc(doc, type, userName) == 0)
				break;
		}
	}

	void scrabWeb(Map<String, String> cookies) {
		String userName = null;
		while (true) {
			while (true) {
				try {
					userName = queue.remove();
					break;
				} catch (Exception e) {
					// e.printStackTrace();

				}
			}

			getPeopleNameFromUrl(userName, urlFollowees, urlFolloweesNext,
					"ee", cookies);
			getPeopleNameFromUrl(userName, urlFollowers, urlFollowersNext,
					"er", cookies);
		}

	}
}