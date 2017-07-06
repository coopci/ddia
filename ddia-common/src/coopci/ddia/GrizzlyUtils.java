package coopci.ddia;

import java.io.IOException;

import org.glassfish.grizzly.http.server.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import coopci.ddia.Result;

public class GrizzlyUtils {

	public static void writeJson(Response response, Result result) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		String content = objectMapper.writeValueAsString(result);
		response.setStatus(result.code);
		response.setContentType("application/json;charset=utf-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		// response.getWriter().write(content);
		response.getNIOOutputStream().write(content.getBytes("utf-8"));;
		return;
	}
}
