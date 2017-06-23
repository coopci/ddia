package coopci.ddia;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;


public class Result {
	public static int CODE_OK = 200;
	public int code = 200;
	public String msg = "OK";
	
	public void setError(int _c, String _m) {
		this.code = _c;
		this.msg = _m;
	}
}
