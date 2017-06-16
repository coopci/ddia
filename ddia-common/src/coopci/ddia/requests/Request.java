package coopci.ddia.requests;
import java.util.HashMap;

// websocket用的请求
public class Request {
	public static String REQUEST_CMD_LOGIN = "login";
	public static String REQUEST_ARGNAME_SESSID = "sessid";
	
	
	
	public String cmd;
	public HashMap<String, String> args;
}
