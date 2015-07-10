import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Zhihu {

	public static void main(String args[]) {
		Map<String, String> cookies = Cookies.getCookie();
		try {
			MyThread.queue = new ArrayBlockingQueue<String>(1000000);
			MyThread.queue.put("cgangee");

			ExecutorService exec = Executors.newCachedThreadPool();

			for (int i = 0; i < 300; ++i) {
				MyThread thread = new MyThread(cookies);
				exec.execute(thread);
			}
			exec.shutdown();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
