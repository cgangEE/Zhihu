import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Cookies {
	static String getXsrfValue() throws Exception {

		Document doc = Jsoup.connect(MyThread.urlHome).get();
		Elements forms = doc.getElementsByTag("form");

		for (Element form : forms) {
			Elements inputs = form.getElementsByTag("input");
			for (Element input : inputs) {
				return input.val();
			}
			break;
		}
		return null;
	}

	static Map<String, String> getCookieFromWeb() throws Exception {
		String xsrfValue = getXsrfValue();

		Connection.Response res = Jsoup
				.connect(MyThread.urlHome + MyThread.urlLogin)
				.timeout(10000)
				.data("_xsrf", xsrfValue, "email", "cgangee@gmail.com",
						"password", "Alical", "rememberme", "y")
				.method(Method.POST).execute();

		Map<String, String> loginCookies = res.cookies();
		Document doc = res.parse();
		System.out.println(doc.title());

		return loginCookies;
	}

	static void saveToFile(Map<String, String> cookies, File fCookies)
			throws Exception {
		FileOutputStream outStream = new FileOutputStream(fCookies);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				outStream);
		objectOutputStream.writeObject(cookies);
		outStream.close();
	}

	static Map<String, String> loadFromFile(File fCookies) throws Exception {
		Map<String, String> cookies = null;

		FileInputStream inputStream = new FileInputStream(fCookies);
		ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		cookies = (Map<String, String>) objectInputStream.readObject();

		return cookies;
	}

	static Map<String, String> getCookie() {
		Map<String, String> cookies = null;

		while (true) {
			try {
				File fCookies = new File("cookies");
				if (!fCookies.exists()) {
					cookies = getCookieFromWeb();
					saveToFile(cookies, fCookies);
				} else {
					cookies = loadFromFile(fCookies);
				}
				break;
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}

		return cookies;
	}

}
