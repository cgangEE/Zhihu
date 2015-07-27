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

public class Question extends Thread {
	Map<String, String> cookies;
	Set<Integer> questionSet = null;

	Question(Map<String, String> cookies) {
		this.cookies = cookies;
		questionSet = Collections.synchronizedSet(new HashSet<Integer>());
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
	
	void addToDB(Integer question, String userName){
		if (questionSet.contains(question)) return;
		questionSet.add(question);
		
		BasicDBObject bean = new BasicDBObject();
		bean.put("userName", userName);
		bean.put("question", question);
		
		boolean finded = false;
		
		while (true) {
			try {
				finded = !new DAO().find("question", bean).isEmpty();
				break; 
			} catch (Exception e){}
		}
		
		if (!finded)
			while (true){
				try{
					new DAO().insert("question", bean);
					break;
				} catch (Exception e){}
			}
	}
	
	int getQuestionFromDoc(Document doc, String userName){
		Elements elements = doc.getElementsByClass("question_link");
		int ret = elements.size();
		for (Element element:elements){
			
			ArrayList<Integer> list = Answer.getNumber(element.attr("href"));
			//System.out.println(element.html());
			//System.out.println(element.attr("href"));
			//System.out.println(list.get(0));
			addToDB(list.get(0), userName);
		}
		
		return ret;
	}
	
	void getQuestionFromUser(String userName){
		String url = "http://www.zhihu.com/people/" + userName + "/asks";
		for (int i = 1;; ++i){
			Document doc = getDocFromUrl(url + "?page=" + i);
			int ret = getQuestionFromDoc(doc, userName);
			if (ret == 0) break;
		}
	}
	
	@Override
	public void run() {
		String userName = null;
		while (!MyThread.queue.isEmpty()) {
			while (true){
				try {
					userName = MyThread.queue.remove();
					break;
				} catch (Exception e){}
			}
			getQuestionFromUser(userName);
		}
	}
	
	
}
