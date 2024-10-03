package itstep.learning.servlets.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.shop.ProductDao;
import itstep.learning.dal.dto.shop.Product;
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
import java.text.SimpleDateFormat;
import java.util.UUID;

@Singleton
public class ProductServlet extends HttpServlet {

    private final RestService restService;
    private final FormParseService formParseService;
    private final FileService fileService;
    private final ProductDao productDao;

    @Inject
    public ProductServlet(RestService restService, @Named("formParse") FormParseService formParseService, FileService fileService
    , ProductDao productDao) {
        this.restService = restService;
        this.formParseService = formParseService;
        this.fileService = fileService;
        this.productDao = productDao;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getAttribute("Claims.Sid") == null) {
            restService.sendRestError(resp, "Unauthorized. Token empty or rejected");
            return;
        }

        try {
            Product product = getModelFromRequest(req);
            product = productDao.add(product);
            if(product == null) {
                restService.sendRestError(resp, "Something went wrong. Check server logs");
            }
            else {
                restService.setRestResponse(resp, product);
            }
        }catch (Exception ex){
            restService.sendRestError(resp, ex.getMessage());
        }
    }

    private Product getModelFromRequest(HttpServletRequest request) throws Exception {
        Product product = new Product();
        FormParseResult res = formParseService.parse(request);

        product.setSlug(res.getFields().get("product_slug"));
        if(product.getSlug() != null && !product.getSlug().isEmpty() && !productDao.isSlugFree(product.getSlug())){
            throw new Exception("product_slug-Slug already used");
        }

        try {
            product.setCategoryId(UUID.fromString(res.getFields().get("product_category")));
        }catch (IllegalArgumentException ignored){
            throw new Exception("product_category-Missing or empty or incorrect required field");
        }

        try {
            product.setPrice(Double.parseDouble(res.getFields().get("product_price")));
        }catch (IllegalArgumentException ignored){
            throw new Exception("product_price-Price is required");
        }

        product.setName(res.getFields().get("product_name"));
        if (product.getName() == null || product.getName().isEmpty()) {
            throw new Exception("product_name-Product name is required");
        }

        product.setDescription(res.getFields().get("product_description"));
        if (product.getName() == null || product.getName().isEmpty()) {
            throw new Exception("product_description-Product description is required");
        }


        FileItem avatar = res.getFiles().get("product_img");
        if (avatar.getSize() > 0) {
            try {
                product.setImageUrl(fileService.upload(avatar));
            }catch (Exception ignored){
                throw new Exception("product_img-You can add only .png .jpg .svg");
            }
        }
        else {
            throw new Exception("product_img-Avatar is required");
        }

        return product;
    }


}
