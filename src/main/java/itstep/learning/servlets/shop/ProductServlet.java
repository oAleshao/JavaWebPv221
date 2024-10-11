package itstep.learning.servlets.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.shop.ProductDao;
import itstep.learning.dal.dto.shop.Product;
import itstep.learning.rest.RestMetaData;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestResponseStatus;
import itstep.learning.rest.RestService;
import itstep.learning.services.files.FileService;
import itstep.learning.services.formparse.FormParseResult;
import itstep.learning.services.formparse.FormParseService;
import org.apache.commons.fileupload.FileItem;

import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class ProductServlet extends HttpServlet {

    private final RestService restService;
    private final FormParseService formParseService;
    private final FileService fileService;
    private final ProductDao productDao;
    private RestResponse restResponse;

    @Inject
    public ProductServlet(RestService restService, @Named("formParse") FormParseService formParseService, FileService fileService
    , ProductDao productDao) {
        this.restService = restService;
        this.formParseService = formParseService;
        this.fileService = fileService;
        this.productDao = productDao;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        restResponse = new RestResponse();
        restResponse.setMeta(
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

        restService.setRestResponse(resp, restResponse.setData("Missing one of the required parameters: 'id' or 'categoryId'"));
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

        String slug = res.getFields().get("product_slug");
        if(slug != null && !slug.isEmpty()) {
            slug = slug.trim();
            if(slug.isEmpty() || !productDao.isSlugFree(slug)){
                throw new Exception("product_slug-Slug: " + slug + " already used");
            }
            product.setSlug(slug);
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

    private void getProductByCategoryId(String categoryId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UUID categoryUuid;
        try { categoryUuid = UUID.fromString(categoryId);  }
        catch (IllegalArgumentException ignored) {
            restService.sendRestError(resp, "categoryId-Invalid");
            return;
        }
        restService.setRestResponse(resp, restResponse.setData(productDao.getAll(categoryUuid)));
    }

    private void getProductById(String productId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Product product = productDao.getProductByIdOrSlug( productId );
        if( product != null ) {
            restService.sendRest( resp,
                    restResponse
                            .setStatus( 200  )
                            .setData( product )
            );
        }
        else {
            restService.sendRest( resp,
                    restResponse
                            .setStatus( 404 )
                            .setData( "Product not found: " + productId ) );
        }
    }
}
