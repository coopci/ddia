package coopci.ddia.gateway;

import java.io.IOException;
import java.util.List;

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

public class BroadcastApplication extends WebSocketApplication {
    private final Broadcaster broadcaster;

    public BroadcastApplication(Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }
//    @Override
//    public void onConnect(WebSocket socket) {
//    	
//    }
    @Override
    public WebSocket createSocket(ProtocolHandler handler,
                                  HttpRequestPacket requestPacket,
                                  WebSocketListener... listeners) {
        final DefaultWebSocket ws =
                (DefaultWebSocket) super.createSocket(handler,
                requestPacket, listeners);

        // ws.setBroadcaster(broadcaster);
        return ws;
    }
    @Override
    public List<String> getSupportedProtocols(List<String> subProtocol) {
        return subProtocol;
    }
    
    

	public ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper;
	}
	
	
	public void onRequest(WebSocket socket, Request request) {
		if (request.cmd.equals(Request.REQUEST_CMD_LOGIN)) {
			String sessid = request.args.get(Request.REQUEST_ARGNAME_SESSID);
			if (sessid == null || sessid.isEmpty()) {
				socket.send("sessid is required.");
			} else {
				// TODO 找 sessid对应的 uid。
				// TODO 如果找到 ，则调用 this.engine.getSubscriber.subscribe(uid);
				
				
			}
			
		}
	}
    @Override
    public void onMessage(WebSocket socket, String data) {
        socket.broadcast(getWebSockets(), "broadcast: " + data);
        socket.send("echo: " + data);
        
        try {
			Request request = getObjectMapper().readValue(data, Request.class);
			onRequest(socket, request);
			
			
			
			
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