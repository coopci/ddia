package coopci.ddia.gateway;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpClientUtil {
	public static byte[] get(String url, HashMap<String, String> args) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> entry: args.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append( URLEncoder.encode(key, "UTF-8") );	
			sb.append( "=" );
			sb.append( URLEncoder.encode(value, "UTF-8") );
			sb.append( "&" );
		}
        try {
            HttpGet httpget = new HttpGet(url + "?" + sb.toString());
            System.out.println("Executing request " + httpget.getRequestLine());
            // Create a custom response handler
            ResponseHandler<byte[]> responseHandler = new ResponseHandler<byte[]>() {

                @Override
                public byte[] handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    
                    HttpEntity entity = response.getEntity();
                    
                    return entity != null ? EntityUtils.toByteArray(entity) : null;
                    
//                    if (status >= 200 && status < 300) {
//                        HttpEntity entity = response.getEntity();
//                        return entity != null ? EntityUtils.toString(entity) : null;
//                    } else {
//                        throw new ClientProtocolException("Unexpected response status: " + status);
//                    }
                }

            };
            byte[] responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            // System.out.println(responseBody);
            return responseBody;
        } finally {
            httpclient.close();
        }
	}
	
	
	
	public static byte[] post(String url, HashMap<String, String> args) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> entry: args.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append( URLEncoder.encode(key, "UTF-8") );	
			sb.append( "=" );
			sb.append( URLEncoder.encode(value, "UTF-8") );
			sb.append( "&" );
		}
        try {
        	HttpPost httppost = new HttpPost(url);
            System.out.println("Executing request " + httppost.getRequestLine());
            httppost.setEntity(new StringEntity(sb.toString()));
            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            ResponseHandler<byte[]> responseHandler = new ResponseHandler<byte[]>() {
                @Override
                public byte[] handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toByteArray(entity) : null;
                }
            };
            byte[] responseBody = httpclient.execute(httppost, responseHandler);
            System.out.println("----------------------------------------");
            // System.out.println(responseBody);
            return responseBody;
        } finally {
            httpclient.close();
        }
	}
	
}
