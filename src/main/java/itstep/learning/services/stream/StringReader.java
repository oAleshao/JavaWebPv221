package itstep.learning.services.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface StringReader {
    String read(InputStream inputStream) throws IOException;
    Map<String, Object> getDataFromFile(String filename) throws IOException;
}
