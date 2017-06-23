import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;


public class Main {
	public static class Simple {
	    public int x = 1;
	    public int y = 2;
	}
	public static void main(String[] args) throws Exception {
		
		ObjectMapper xmlMapper = new XmlMapper();
		Simple value = xmlMapper.readValue("<Simple><x>1</x><y>2</y></Simple>", Simple.class);
		System.out.println("value.x: " + value.x);
	}
}
