package coopci.ddia.gateway;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.Broadcaster;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import coopci.ddia.requests.Request;
import coopci.ddia.results.UserInfosResult;

public class GatewayApplication extends WebSocketApplication {


    public GatewayApplication() {
        
    }

    @Override
    public void onClose(WebSocket socket, DataFrame frame) {
        super.onClose(socket, frame);
        DDIAWebSocket ddiaWebSocket = (DDIAWebSocket) socket;
        
        ddiaWebSocket.logClose();
    }

    @Override
    public WebSocket createSocket(ProtocolHandler handler,
                                  HttpRequestPacket requestPacket,
                                  WebSocketListener... listeners) {
        final DDIAWebSocket ws = new DDIAWebSocket(handler,
                requestPacket, listeners);

        return ws;
    }
    
//    
//    @Override
//    public WebSocket createSocket(ProtocolHandler handler,
//                                  HttpRequestPacket requestPacket,
//                                  WebSocketListener... listeners) {
//        final DefaultWebSocket ws =
//                (DefaultWebSocket) super.createSocket(handler,
//                requestPacket, listeners);
//
//        // ws.setBroadcaster(broadcaster);
//        return ws;
//    }
    
    
    @Override
    public List<String> getSupportedProtocols(List<String> subProtocol) {
        return subProtocol;
    }
    
    

	public ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper;
	}
	
	Engine engine = null;
	public void onRequest(DDIAWebSocket socket, Request request) {
		if (request.cmd.equals(Request.REQUEST_CMD_LOGIN)) {
			String sessid = request.args.get(Request.REQUEST_ARGNAME_SESSID);
			socket.setSessid(sessid);
			if (sessid == null || sessid.isEmpty()) {
				socket.send("sessid is required.");
			} else {
				long uid;
				try {
					uid = this.getUidBySessid(sessid);
					if (uid > 0) {
						this.subscribe(uid, socket);
						this.notifyLogin(uid);
						socket.setUid(uid);
						socket.send("login ok.");
					} else {
						socket.send("login failed. (" + uid + ")");
					}
				} catch (InvalidKeyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalBlockSizeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BadPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	public long getUidBySessid(String sessid) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		if (this.engine == null)
			return -2;
		long uid = this.engine.getUidFromSessid(sessid);
		return uid;
	}
	// 向系统的消息总线通知用户登录事件，以便各个微服务能够监听到。
	public void notifyLogin(long uid) {
		// TODO 向系统的消息总线通知用户登录事件，以便各个微服务能够监听到。
	}
	public void subscribe(long uid, WebSocket socket) {
		// TODO 调用 this.engine.getSubscriber.subscribe(uid);
		
	}
    @Override
    public void onMessage(WebSocket socket, String data) {
        
        
        try {
			Request request = getObjectMapper().readValue(data, Request.class);
			onRequest((DDIAWebSocket)socket, request);
			
			
			
			
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
}