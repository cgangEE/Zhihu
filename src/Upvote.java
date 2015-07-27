import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

	String getAjaxFromUrl(String url){
		String ret = null;
		
		return ret;
	}
	
	void getUpvoteFromAjax(String url, String userName, String id, int cnt) {
		String urlUpvote = "http://www.zhihu.com/answer/"+id+"/voters_profile";
		for (int i=0; i<(cnt+9)/10; ++i){
			urlUpvote = urlUpvote + "?offset="+(i*10);
			String html = getAjaxFromUrl(urlUpvote);
			
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
			// break;
		}
	}

}