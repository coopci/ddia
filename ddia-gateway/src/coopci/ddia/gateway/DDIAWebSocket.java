package coopci.ddia.gateway;

import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;

public class DDIAWebSocket extends DefaultWebSocket {

	public DDIAWebSocket(ProtocolHandler protocolHandler,
			HttpRequestPacket request, WebSocketListener[] listeners) {
		super(protocolHandler, request, listeners);
	}
	
	public String getSessid() {
		return sessid;
	}
	public void setSessid(String sessid) {
		this.sessid = sessid;
	}
	public long getUid() {
		return uid;
	}
	public void setUid(long uid) {
		this.uid = uid;
	}

	String sessid = "";
	long uid = -1;

	public void logClose() {
		StringBuilder sb = new StringBuilder();
		sb.append("onClose: ");
		sb.append("sessid[");
		sb.append(this.sessid);
		sb.append("] ");
		
		sb.append("uid[");
		sb.append(this.uid);
		sb.append("] ");
		
		
		System.out.println(sb.toString());
	}
}
