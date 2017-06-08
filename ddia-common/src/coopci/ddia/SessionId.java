package coopci.ddia;

public class SessionId {

	public long uid;
	
	
	
	
	
	public String salt;
	
	public String toString() {
		String ret = Long.toString(uid) + "-" + salt;
		return ret;
	}
	
	public void parse(String str) {
		String[] fields = str.split("-");
		this.uid = Long.parseLong(fields[0]);
		this.salt = fields[1];
	}
}

