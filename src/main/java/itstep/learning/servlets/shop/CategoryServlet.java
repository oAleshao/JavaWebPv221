package itstep.learning.servlets.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.shop.CategoryDao;
import itstep.learning.models.formmodels.CategoryModel;
import itstep.learning.rest.RestService;
import itstep.learning.services.files.FileService;
import itstep.learning.services.formparse.FormParseResult;
import itstep.learning.services.formparse.FormParseService;
import org.apache.commons.fileupload.FileItem;

import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class CategoryServlet extends HttpServlet {
    private final RestService restService;
    private final FormParseService formParseService;
    private final FileService fileService;
    private final CategoryDao categoryDao;

    @Inject
    public CategoryServlet(CategoryDao categoryDao, RestService restService, FileService fileService, @Named("formParse") FormParseService formParseService) {
        this.categoryDao = categoryDao;
        this.restService = restService;
        this.fileService = fileService;
        this.formParseService = formParseService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        restService.setRestResponse(resp, categoryDao.getAll());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FormParseResult formParseResult = formParseService.parse(req);

        String slug = formParseResult.getFields().get("category_slug");
        if (slug != null && slug.isEmpty()) {
            restService.sendRestError(resp, "category_slug-Slug already used");
            return;
        }


        String name = formParseResult.getFields().get("category_name");
        if (name == null || name.isEmpty()) {
            restService.sendRestError(resp, "category_name-Name is required");
            return;
        }

        String description = formParseResult.getFields().get("category_description");
        if (description == null || description.isEmpty()) {
            restService.sendRestError(resp, "category_description-Description is required");
            return;
        }

        String uploadedName = null;
        FileItem avatar = formParseResult.getFiles().get("category_img");
        if (avatar.getSize() > 0) {
            try {
                uploadedName = fileService.upload(avatar);
            } catch (Exception e) {
                restService.sendRestError(resp, e.getMessage());
                return;
            }
        } else {
            restService.sendRestError(resp, "category_img-Image is required");
            return;
        }


        restService.setRestResponse(resp,
                categoryDao.add(new CategoryModel()
                        .setName(name)
                        .setDescription(description)
                        .setSlug(slug)
                        .setSavedFileName(uploadedName)
                ));
    }
}
