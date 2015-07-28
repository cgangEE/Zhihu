import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;

public class Upvote extends Thread {
	Map<String, String> cookies;
	static ArrayBlockingQueue<String> answerQueue = null;

	public Upvote(Map<String, String> cookies) {
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

	String getAjaxFromUrl(String url) {
		/*
		 * String ret = null; HttpClient client = null; boolean flag = true;
		 * while (flag) { try { client = new DefaultHttpClient();
		 * HttpClientParams.setCookiePolicy(client.getParams(),
		 * CookiePolicy.BROWSER_COMPATIBILITY); HttpGet get = new HttpGet(url);
		 * HttpResponse response = client.execute(get); HttpEntity entity =
		 * response.getEntity(); ret = EntityUtils.toString(entity); flag =
		 * false; } catch (Exception e) { e.printStackTrace(); } finally {
		 * client.getConnectionManager().shutdown(); } } return ret;
		 */

		String ret = null;
		while (true)
			try {
				Document doc = Jsoup.connect(url).cookies(cookies)
						.ignoreContentType(true).get();
				ret = doc.html();
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		return ret;

	}

	void addToDB(String peopleName, String userName){
		BasicDBObject bean = new BasicDBObject();
		bean.put("A", peopleName);
		bean.put("B", userName);
		while (true){
			try{
				new DAO().insert("upvote", bean);
				break;
			} catch (Exception e){}
		}
	}
	
	void getUpvoteFromHtml(String html, String url, String urlUpvote,
			String userName, int cnt) {

		int start = 0;
		int end = 0;
		int peopleCnt = 0;
	
		while (true) {
			start = html.indexOf("/people/", end);
			
			if (start == -1)
				break;
			end = html.indexOf("\\", start);
			
			if (html.charAt(start-1)=='m') continue;
			String peopleName = html.substring(start + 8, end);
			
			if (!peopleName.contains("/")){
				//System.out.println(peopleName);
				if (MyThread.peopleSet.contains(peopleName))
					addToDB(peopleName, userName);
			}
		}

	}

	void getUpvoteFromAjax(String url, String userName, String id, int cnt) {
		String urlUpvoted = "http://www.zhihu.com/answer/" + id
				+ "/voters_profile";
		for (int i = 0; i < (cnt + 9) / 10; ++i) {
			String urlUpvote = urlUpvoted + "?offset=" + (i * 10);
			String html = getAjaxFromUrl(urlUpvote);

			getUpvoteFromHtml(html, url, urlUpvote, userName, cnt);
		}
	}

	void getUpvoteFromAnswer(String userName, String question, String answer) {
		if (userName.compareTo("cgangee") == 0)
			return;

		String url = "http://www.zhihu.com/question/" + question + "/answer/"
				+ answer;
		Document doc = getDocFromUrl(url);
		Elements elements = doc.getElementsByClass("zm-item-answer");
		String id = elements.first().attr("data-aid");
		elements = elements.first().getElementsByClass("count");
		Element element = elements.first();
		int cnt = Integer.valueOf(element.text());

		if (cnt > 0)
			getUpvoteFromAjax(url, userName, id, cnt);
	}

	@Override
	public void run() {
		String userName = null;
		String question = null;
		String answer = null;
		while (!answerQueue.isEmpty()) {
			while (true) {
				try {
					String tmp = answerQueue.remove();
					String s[] = tmp.split(" ");
					userName = s[0];
					question = s[1];
					answer = s[2];

					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			getUpvoteFromAnswer(userName, question, answer);
			System.out.println(userName);
		}
	}

}