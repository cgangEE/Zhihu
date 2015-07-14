import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;

public class TopicUtil {
	static ArrayBlockingQueue<String> queue = null;

	static boolean getXsrf(Document doc, StringBuffer _xsrf) {
		Elements elements = doc.getElementsByAttributeValue("name", "_xsrf");
		if (elements.isEmpty())
			return false;

		Element element = elements.first();
		_xsrf.append(element.attr("value"));

		return true;
	}

	static int getTopicFromDoc(Document doc, String userName, List<String> col) {
		int ret = 0;

		String html = doc.html();
		html = html.substring(52, html.length());
		html = StringEscapeUtils.unescapeJava(html);
		html = html.replaceAll("\\&quot;", "");

		html = html.replaceAll("\\\\", "");
		html = html.replaceAll("&lt;", "<");
		html = html.replaceAll("&gt;", ">");
		doc = Jsoup.parse(html);

		Elements elements = doc.select("a strong");
		for (Element elem : elements) {
			String topic = elem.text();
			if (topic.length() == 0)
				continue;

			col.add(topic);
			++ret;
		}
		return ret;
	}

	static void getTopicFromUrl(String userName, Map<String, String> cookies,
			List<String> col) {
		Document doc = null;
		StringBuffer _xsrf = new StringBuffer();

		while (true) {
			try {
				doc = Jsoup
						.connect(
								MyThread.urlHome + MyThread.urlPeople
										+ userName + MyThread.urlTopics)
						.cookies(cookies).get();
				break;
			} catch (Exception e) {
			}
		}

		if (!getXsrf(doc, _xsrf))
			return;

		for (int offset = 0;; offset += 20) {
			while (true) {
				try {
					doc = Jsoup
							.connect(
									MyThread.urlHome + MyThread.urlPeople
											+ userName + MyThread.urlTopics)
							.timeout(10000)
							.data("start", "0", "offset",
									new Integer(offset).toString(), "_xsrf",
									_xsrf.toString()).method(Method.POST)
							.cookies(cookies).ignoreContentType(true).post();
					break;
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}

			if (getTopicFromDoc(doc, userName, col) == 0)
				break;

		}
	}

	static boolean getHashidAndXsrf(Document doc, StringBuffer hash_id,
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

	
	static int getColumnFromDoc(Document doc, String userName, List<String> col) {
		int ret=0;
		Elements elements = doc.select("a strong");
		for (Element elem:elements){
			++ret;
			col.add(elem.text());
		}
		return ret;
	}
	
	
	static void getColumnFromUrl(String userName, Map<String, String> cookies,
			List<String> col) {
		Document doc = null;
		StringBuffer _xsrf = new StringBuffer();
		StringBuffer hash_id = new StringBuffer();
		while (true) {
			try {
				doc = Jsoup
						.connect(
								MyThread.urlHome + MyThread.urlPeople
										+ userName + "/columns/followed")
						.cookies(cookies).get();
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (!getHashidAndXsrf(doc, hash_id, _xsrf))
			return;

		for (int offset = 0;; offset += 20) {
			while (true) {
				try {
					JSONObject jsonobj = new JSONObject();

					try {
						jsonobj.put("offset", offset);
						jsonobj.put("limit", 20);
						jsonobj.put("hash_id", hash_id);
					} catch (Exception e) {
						e.printStackTrace();
					}

					doc = Jsoup
							.connect(
									"http://www.zhihu.com/node/ProfileFollowedColumnsListV2")
							.timeout(10000)
							.data("method", "next", "_xsrf", _xsrf.toString(),
									"params", jsonobj.toString())
							.method(Method.POST).cookies(cookies).get();
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (getColumnFromDoc(doc, userName, col) == 0)
				break;

		}
	}


	

	static int getPeopleFromDoc(Document doc, String userName, int type) {
		// System.out.println(doc.html());
		int ret = 0;
		Elements links = doc.getElementsByTag("a");
		for (Element link : links) {
			String url = link.attr("href");

			if (url.matches("^.*people" + ".*")) {
				String peopleName = null;
				if (type == 1)
					peopleName = url.substring(11, url.length() - 1);
				else
					peopleName = url.substring(8, url.length());

				if (peopleName.contains("/"))
					continue;
				if (!MyThread.peopleSet.contains(peopleName)) {
					++ret;
					System.out.println(peopleName);
					MyThread.peopleSet.add(peopleName);
					MyThread.queue.add(peopleName);

					BasicDBObject bean = new BasicDBObject();
					bean.put("name", peopleName);

					boolean finded = false;

					while (true) {
						try {
							finded = !new DAO().find("people", bean).isEmpty();
							break;
						} catch (Exception e) {
						}
					}

					if (!finded)
						while (true) {
							try {
								new DAO().insert("people", bean);
								break;
							} catch (Exception e) {
							}
						}
				}
			}
		}

		return ret;
	}

	public static void getPeopleFromTopic(Map<String, String> cookies)
			throws Exception {

		while (!queue.isEmpty()) {
			String topic = queue.take();
			Document doc = null;

			while (true) {
				try {
					doc = Jsoup
							.connect(
									MyThread.urlHome + MyThread.urlTopic
											+ topic + MyThread.urlFollowers)
							.timeout(10000).cookies(cookies).get();
					break;
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}

			StringBuffer _xsrf = new StringBuffer();
			if (!getXsrf(doc, _xsrf))
				return;

			int ret = getPeopleFromDoc(doc, topic, 0);

			// System.out.println(ret);
			String start = null;

			Elements elements = doc.getElementsByClass("zm-person-item");
			Element element = elements.last();
			start = element.attr("id");
			start = start.substring(3, start.length());

			// System.out.println(start);

			for (int offset = 40;; offset += 20) {
				while (true) {
					try {
						doc = Jsoup
								.connect(
										"http://www.zhihu.com/topic/" + topic
												+ "/followers")
								.timeout(10000)
								.data("offset", new Integer(offset).toString(),
										"start", start, "_xsrf",
										_xsrf.toString()).userAgent("Mozilla")
								.ignoreContentType(true).maxBodySize(0)
								.cookies(cookies).post();
						String html = doc.body().toString();
						html = html.substring(28, html.length());

						html = html.replaceAll("\\&quot;", "");

						doc = Jsoup.parse(html);

						break;
					} catch (Exception e) {

					}
				}

				ret = getPeopleFromDoc(doc, topic, 1);
				if (ret == 0)
					break;

				elements = doc.getElementsByClass("\\zm-person-item\\");
				element = elements.last();
				start = element.attr("id");
				start = start.substring(4, start.length() - 1);

			}

		}

	}

}
