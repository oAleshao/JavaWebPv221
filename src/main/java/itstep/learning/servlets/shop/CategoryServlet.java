package itstep.learning.servlets.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.shop.CategoryDao;
import itstep.learning.models.formmodels.CategoryModel;
import itstep.learning.rest.RestMetaData;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestServlet;
import itstep.learning.services.cacheMaster.CacheMaster;
import itstep.learning.services.files.FileService;
import itstep.learning.services.formparse.FormParseResult;
import itstep.learning.services.formparse.FormParseService;
import org.apache.commons.fileupload.FileItem;

import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@Singleton
public class CategoryServlet extends RestServlet {
    private final FormParseService formParseService;
    private final FileService fileService;
    private final CategoryDao categoryDao;
    private final CacheMaster cacheMaster;
    private int maxAge;

    @Inject
    public CategoryServlet(CategoryDao categoryDao, FileService fileService, @Named("formParse") FormParseService formParseService,
                           CacheMaster cacheMaster) {
        this.categoryDao = categoryDao;
        this.fileService = fileService;
        this.formParseService = formParseService;
        this.cacheMaster = cacheMaster;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        super.restResponse = new RestResponse();
        super.restResponse.setMeta(
                new RestMetaData()
                        .setUri("/shop/categories")
                        .setMethod(req.getMethod())
                        .setLocale("uk-UA")
                        .setServerTime(new Date())
                        .setName("Category API")
                        .setAcceptMethods(new String[]{"GET", "POST"})
        );
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.maxAge = cacheMaster.getMaxAge("category");
        super.sendRest(200, categoryDao.getAll(), this.maxAge);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FormParseResult formParseResult = formParseService.parse(req);

        String slug = formParseResult.getFields().get("category_slug");
        if (slug != null && slug.isEmpty()) {
            super.sendRest(422, "category_slug-Slug already used");
            return;
        }


        String name = formParseResult.getFields().get("category_name");
        if (name == null || name.isEmpty()) {
            super.sendRest(422, "category_name-Name is required");
            return;
        }

        String description = formParseResult.getFields().get("category_description");
        if (description == null || description.isEmpty()) {
            super.sendRest(422, "category_description-Description is required");
            return;
        }

        String uploadedName = null;
        FileItem avatar = formParseResult.getFiles().get("category_img");
        if (avatar.getSize() > 0) {
            try {
                uploadedName = fileService.upload(avatar);
            } catch (Exception e) {
                super.sendRest(422, e.getMessage());
                return;
            }
        } else {
            super.sendRest(422, "category_img-Image is required");
            return;
        }


        super.sendRest(200,
                categoryDao.add(new CategoryModel()
                        .setName(name)
                        .setDescription(description)
                        .setSlug(slug)
                        .setSavedFileName(uploadedName)
                ));
    }
}
