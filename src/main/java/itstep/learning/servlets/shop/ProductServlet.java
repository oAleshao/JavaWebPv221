package itstep.learning.servlets.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.shop.CategoryDao;
import itstep.learning.dal.dao.shop.ProductDao;
import itstep.learning.dal.dto.shop.Category;
import itstep.learning.dal.dto.shop.Product;
import itstep.learning.rest.*;
import itstep.learning.services.cacheMaster.CacheMaster;
import itstep.learning.services.files.FileService;
import itstep.learning.services.formparse.FormParseResult;
import itstep.learning.services.formparse.FormParseService;
import itstep.learning.services.stream.StringReader;
import org.apache.commons.fileupload.FileItem;

import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class ProductServlet extends RestServlet {

    private final FormParseService formParseService;
    private final FileService fileService;
    private final ProductDao productDao;
    private final CategoryDao categoryDao;
    private final CacheMaster cacheMaster;
    private int maxAge;

    @Inject
    public ProductServlet(@Named("formParse") FormParseService formParseService, FileService fileService
            , ProductDao productDao, CategoryDao categoryDao, CacheMaster cacheMaster) {
        this.formParseService = formParseService;
        this.fileService = fileService;
        this.productDao = productDao;
        this.categoryDao = categoryDao;
        this.cacheMaster = cacheMaster;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.restResponse = new RestResponse();
        super.restResponse.setMeta(
                new RestMetaData()
                        .setUri("/shop/products")
                        .setMethod(req.getMethod())
                        .setLocale("uk-UA")
                        .setServerTime(new Date())
                        .setName("Product API")
                        .setAcceptMethods(new String[]{"GET", "POST"})
        );
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        this.maxAge = cacheMaster.getMaxAge("product");

        String productId = req.getParameter("id");
        if (productId != null) {
            Map<String, Object> params = new HashMap<>();
            params.put("id", productId);
            this.restResponse.getMeta().setParams(params);
            getProductById(productId, req, resp);
            return;
        }


        String categoryId = req.getParameter("categoryId");
        if (categoryId != null) {
            Map<String, Object> params = new HashMap<>();
            params.put("categoryId", categoryId);
            this.restResponse.getMeta().setParams(params);
            getProductByCategoryId(categoryId, req, resp);
            return;
        }

        super.sendRest(400, "Missing one of the required parameters: 'id' or 'categoryId'");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getAttribute("Claims.Sid") == null) {
            super.sendRest(401, "Unauthorized. Token empty or rejected");
            return;
        }

        try {
            Product product = getModelFromRequest(req);
            product = productDao.add(product);
            if (product == null) {
                super.sendRest(500, "Something went wrong. Check server logs");
            } else {
                super.sendRest(200, product);
            }
        } catch (Exception ex) {
            super.sendRest(422, ex.getMessage());
        }
    }

    private Product getModelFromRequest(HttpServletRequest request) throws Exception {
        Product product = new Product();
        FormParseResult res = formParseService.parse(request);

        String slug = res.getFields().get("product_slug");
        if (slug != null && !slug.isEmpty()) {
            slug = slug.trim();
            if (slug.isEmpty() || !productDao.isSlugFree(slug)) {
                throw new Exception("product_slug-Slug: " + slug + " already used");
            }
            product.setSlug(slug);
        }


        String categoryId = res.getFields().get("product_category");
        Category category = categoryDao.getProductByIdOrSlug(categoryId);
        if (category == null) {
            throw new Exception("product_category-Missing or empty or incorrect required field");
        }
        product.setCategoryId(category.getId());

        try {
            product.setPrice(Double.parseDouble(res.getFields().get("product_price")));
        } catch (IllegalArgumentException ignored) {
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
            } catch (Exception ignored) {
                throw new Exception("product_img-You can add only .png .jpg .svg");
            }
        } else {
            throw new Exception("product_img-Avatar is required");
        }

        return product;
    }

    private void getProductByCategoryId(String categoryId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Category category = categoryDao.getProductByIdOrSlug(categoryId);
        if (category == null) {
            super.sendRest(404, "categoryId-Invalid");
            return;
        }
        super.sendRest(200, productDao.getAll(category.getId()), this.maxAge);
    }

    private void getProductById(String productId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Product product = productDao.getProductByIdOrSlug(productId);
        if (product != null) {
            super.sendRest(200, product, this.maxAge);
        } else {
            super.sendRest(404, "Product not found: " + productId);
        }
    }
}
