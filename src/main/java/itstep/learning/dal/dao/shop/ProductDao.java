package itstep.learning.dal.dao.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dto.shop.Category;
import itstep.learning.dal.dto.shop.Product;
import itstep.learning.models.formmodels.CategoryModel;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class ProductDao {
    private final Connection connection;
    private final Logger logger;


    @Inject
    public ProductDao(Connection connection, Logger logger) {
        this.connection = connection;
        this.logger = logger;
    }

    public Product getProductByIdOrSlug(String id) {
        if(id == null || id.isEmpty()){
            return null;
        }
        String sql = "SELECT * FROM products WHERE ";
        try {
            UUID.fromString(id);
            sql += "product_id = ?";

        }catch (IllegalArgumentException ignored) {
            sql += "product_slug = ?";
        }

        try(PreparedStatement prep = connection.prepareStatement(sql)){
            prep.setString(1, id);
            ResultSet rs = prep.executeQuery();
            if(rs.next()) {
                return new Product(rs);
            }
        }catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql , ex);
        }
        return null;
    }

    public boolean isSlugFree(String slug) {
        String sql = "SELECT COUNT(*) FROM products p WHERE p.product_slug = ?";
        try(PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.setString(1, slug);
            ResultSet rs = prep.executeQuery();
            if(rs.next()) {
                return rs.getInt(1) == 0;
            }
        }catch (SQLException e) {
            logger.log(Level.WARNING, e.getMessage() + " -- " + sql, e);
        }
        return false;
    }

    public List<Product> getAll(UUID categoryid){
        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products WHERE category_id = ? AND delete_dt IS NULL";
        try(PreparedStatement prop = connection.prepareStatement(sql)) {
            prop.setString(1, categoryid.toString());
            ResultSet rs = prop.executeQuery();
            while(rs.next()) {
                products.add(new Product(rs));
            }
            rs.close();
        }catch (SQLException e) {
            logger.log(Level.WARNING, e.getMessage() + " -- " + sql, e);
        }
        return products;
    }

    public boolean installTables() {
        String sql = "CREATE TABLE IF NOT EXISTS products (" +
                "product_id CHAR(36) PRIMARY KEY DEFAULT(UUID())," +
                "category_id CHAR(36) NOT NULL," +
                "product_name VARCHAR(128) NOT NULL," +
                "product_image_url VARCHAR(512) NOT NULL," +
                "product_description TEXT NULL," +
                "product_price FLOAT NOT NULL," +
                "product_slug VARCHAR(64) NULL," +
                "delete_dt DATETIME NULL" +
                ") ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            return true;
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
            return false;
        }

    }

    public Product add(Product product) {
        if (product.getId() == null) {
            product.setId(UUID.randomUUID());
        }

        String sql = "INSERT INTO products(product_id, category_id, product_name, product_description, product_image_url, product_price, product_slug) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement prop = connection.prepareStatement(sql)) {
            prop.setString(1, product.getId().toString());
            prop.setString(2, product.getCategoryId().toString());
            prop.setString(3, product.getName());
            prop.setString(4, product.getDescription());
            prop.setString(5, product.getImageUrl());
            prop.setDouble(6, product.getPrice());
            prop.setString(7, product.getSlug());
            prop.executeUpdate();
            return product;
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
            return null;
        }


    }

}
