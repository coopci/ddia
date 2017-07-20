package coopci.ddia.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface IGatewayEngine {
	public ObjectMapper getObjectMapper();
	String getMicroserviceHttpPrefix(String serviceName, String partKey);
	String getMicroserviceHttpPrefix(String serviceName, long partKey);
}
