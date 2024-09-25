package itstep.learning.services.files;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.services.stream.StringReader;
import org.apache.commons.fileupload.FileItem;

import java.io.*;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class LocalFileService implements FileService {

    private final String uploadPath;

    @Inject
    public LocalFileService(StringReader stringReader) {

        Map<String, String> ini = new HashMap<>();
        try( InputStream rs = this
                .getClass()
                .getClassLoader()
                .getResourceAsStream("file.ini")
        ) {
            String[] lines = stringReader.read(rs).split("\n");
            for(String line : lines) {
                String[] parts = line.split("=");
                ini.put( parts[0].trim(), parts[1].trim() );
            }
        }
        catch (IOException ex) {
            System.err.println( ex.getMessage() );
            throw new RuntimeException( ex );
        }

        this.uploadPath = ini.get("upload_path");
    }

    @Override
    public String upload(FileItem fileItem) throws Exception {
        String formFile = fileItem.getName();
        // відокремлюємо розширення
        int dotPossition = formFile.lastIndexOf('.');
        String extension = formFile.substring(dotPossition);
        System.out.println(extension);
        if(extension.equals(".png") || extension.equals(".jpg") || extension.equals(".svg")) {
            String fileName;
            File file;
            do {
                fileName = UUID.randomUUID().toString() + extension;
                file = new File(this.uploadPath, fileName);
            } while (file.exists());

            try {
                fileItem.write(file);

            } catch (Exception ex) {
                return null;
            }
            return fileName;
        }
        else {
            throw new Exception("user_avatar-You can add only .png .jpg .svg");
        }
    }

    @Override
    public InputStream download(String filename) throws IOException {
        File file = new File(this.uploadPath, filename);
        if (file.isFile() && file.canRead()) {
            return Files.newInputStream(file.toPath());
        }
        return null;
    }
}
