package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.services.files.FileService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class DownloadServlet extends HttpServlet {
    private final FileService fileService;
    private final Logger logger;

    @Inject
    public DownloadServlet(FileService fileService, Logger logger) {
        this.fileService = fileService;
        this.logger = logger;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try(InputStream fileReadStream = fileService.download(req.getPathInfo())){
            resp.setContentType("application/octet-stream");
            //resp.setHeader("Content-Disposition", "attachment; filename=\"" + req.getRequestURI() + "\"");

            //resp.setContentType("image/png");
            // piping
            OutputStream outputStream = resp.getOutputStream();
            byte[] buffer = new byte[4096];
            int length;
            while ((length = fileReadStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }catch (Exception ex){
            logger.log(Level.WARNING, ex.getMessage(), ex);
            resp.setStatus( 404 );
        }
    }
}

/* http://localhost:8080/Java221/file/123?x=10
 * req.getRequestURL()      http://localhost:8080/Java221/file/
 * req.getContextPath()     /Java221
 * req.getRequestURL()      /file
 * req.getPathInfo()        /123
 * req.getQueryString()     x=10
 *
 * */