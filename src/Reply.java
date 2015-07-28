import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;

import com.mongodb.BasicDBObject;

public class Reply extends Thread {
	Map<String, String> cookies;

	public Reply(Map<String, String> cookies) {
		this.cookies = cookies;
	}

	Document getDocFromUrl(String url) {
		Document doc = null;
		while (true) {
			try {
				doc = Jsoup.connect(url).cookies(cookies).get();
				break;
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
		return doc;
	}

	void addToDB(String peopleName, String userName) {
		BasicDBObject bean = new BasicDBObject();
		bean.put("A", peopleName);
		bean.put("B", userName);
		while (true) {
			try {
				new DAO().insert("reply", bean);
				break;
			} catch (Exception e) {
			}
		}
	}

	void getReplyFromDoc(Document doc, String userName, String url,
			String urlReply) {
		//System.out.println(url);
		
		Elements elements = doc.getElementsByClass("zm-item-link-avatar");
		for (Element element : elements) {
			String peopleName = element.attr("href");
			if (peopleName.length()<8) continue;
			peopleName = peopleName.substring(8, peopleName.length());
			
			//System.out.println(peopleName);
			
			if (peopleName.compareTo(userName) != 0
					&& MyThread.peopleSet.contains(peopleName))
				addToDB(peopleName, userName);
		}
	}

	void getReplyFromAnswer(String userName, String question, String answer) {
		if (userName.compareTo("cgangee") == 0)
			return;
		String url = "http://www.zhihu.com/question/" + question + "/answer/"
				+ answer;
		Document doc = getDocFromUrl(url);
		Elements elements = doc.getElementsByClass("zm-item-answer");
		String id = elements.first().attr("data-aid");

		elements = elements.first().getElementsByClass("toggle-comment");

		if (elements.first().text().compareTo("Ìí¼ÓÆÀÂÛ") == 0)
			return;
		//System.out.println(elements.first().text());
		String urlReply = "http://www.zhihu.com/node/AnswerCommentBoxV2?params=%7B\"answer_id\"%3A\""
				+ id + "\"%2C\"load_all\"%3Atrue%7D";
		doc = getDocFromUrl(urlReply);
		getReplyFromDoc(doc, userName, url, urlReply);
	}

	@Override
	public void run() {
		String userName = null;
		String question = null;
		String answer = null;
		while (!Upvote.answerQueue.isEmpty()) {
			while (true) {
				try {
					String tmp = Upvote.answerQueue.remove();
					String s[] = tmp.split(" ");
					userName = s[0];
					question = s[1];
					answer = s[2];
					break;
				} catch (Exception e) {
				}
			}
			getReplyFromAnswer(userName, question, answer);
			System.out.println(userName);
			
		}

	}

}
