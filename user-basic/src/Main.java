
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import coopci.ddia.LoginResult;

public class Main {
	public static void main(String[] argv) {
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			LoginResult res = new LoginResult();
			String content = objectMapper.writeValueAsString(res);
			System.out.println(content);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
	}
	
}
