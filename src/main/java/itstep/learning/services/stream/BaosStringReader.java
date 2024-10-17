package itstep.learning.services.stream;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BaosStringReader implements StringReader{

    @Override
    public String read(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[4096];
        try(
                ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
                BufferedInputStream bis = new BufferedInputStream(inputStream)) {
            int len;
            while((len = bis.read(buffer)) != -1){
                byteBuilder.write(buffer, 0, len);
            }

            return byteBuilder.toString(StandardCharsets.UTF_8.name());
        }
    }

    @Override
    public Map<String, Object> getDataFromFile(String filename) throws IOException {
        try {
            Map<String, Object> ini = new HashMap<>();

            try (InputStream rs = this.getClass().getClassLoader().getResourceAsStream(filename)) {
                String[] lines = this.read(rs).split("\n");
                for (String line : lines) {
                    String[] parts = line.split("=");
                    ini.put(parts[0].trim(), parts[1].trim());
                }

            } catch (IOException ignore) {
                return null;
            }

            return ini;
        }catch (Exception ex){
            return null;
        }

    }
}
