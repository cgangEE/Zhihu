import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * JavaScript escape/unescape ±àÂëµÄ Java ÊµÏÖ author jackyz keep this copyright info
 * while using this method by free
 */
public class Escape {
	static public String unescape(String src) {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("javascript");
		String ret = null;

		try {
			 ret = (String) engine.eval("unescape(" + src + ");");
		} catch (ScriptException e) {
			System.out.println(src);
			e.printStackTrace();
			System.out.println(ret);
		}
		
		return ret;
	}
}