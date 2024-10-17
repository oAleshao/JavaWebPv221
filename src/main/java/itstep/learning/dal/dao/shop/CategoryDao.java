package itstep.learning.dal.dao.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dto.shop.Category;
import itstep.learning.models.formmodels.CategoryModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class CategoryDao {
    private final Connection connection;
    private final Logger logger;

    @Inject
    public CategoryDao(Connection connection, Logger logger) {
        this.connection = connection;
        this.logger = logger;
    }

    public Category getProductByIdOrSlug(String id){
        if(id == null || id.isEmpty()){
            return null;
        }
        String sql = "SELECT * FROM categories WHERE ";
        try {
            UUID.fromString(id);
            sql += "category_id = ?";

        }catch (IllegalArgumentException ignored) {
            sql += "category_slug = ?";
        }

        try(PreparedStatement prep = connection.prepareStatement(sql)){
            prep.setString(1, id);
            ResultSet rs = prep.executeQuery();
            if(rs.next()) {
                return new Category(rs);
            }
        }catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql , ex);
        }
        return null;
    }

    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories c WHERE c.delete_dt IS NULL";
        try (Statement statement = connection.createStatement()) {
            ResultSet res =  statement.executeQuery(sql);
            while (res.next()) {
                categories.add(new Category(res));
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, e.getMessage() + " -- " + sql, e);
            return null;
        }
        return categories;
    }

    public boolean isSlugFree(String slug) {
        String sql = "SELECT COUNT(*) FROM categories c WHERE c.category_slug = ?";
        try(PreparedStatement prep = connection.prepareStatement(sql)) {
            prep.setString(1, slug);
            ResultSet rs = prep.executeQuery(sql);
            if(rs.next()) {
                return rs.getInt(1) == 0;
            }
        }catch (SQLException e) {
            logger.log(Level.WARNING, e.getMessage() + " -- " + sql, e);
        }
        return false;
    }

    public boolean installTables() {
        String sql = "CREATE TABLE IF NOT EXISTS categories (" +
                "category_id CHAR(36) PRIMARY KEY DEFAULT(UUID())," +
                "name VARCHAR(128) NOT NULL," +
                "image_url VARCHAR(512) NOT NULL," +
                "description TEXT NOT NULL," +
                "delete_dt DATETIME NULL," +
                "category_slug VARCHAR(64) NULL," +
                "UNIQUE(category_slug))" +
                ") ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            return true;
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
            return false;
        }

    }

    public Category add(CategoryModel model) {
        if (model == null) {
            return null;
        }
        Category category = new Category()
                .setName(model.getName())
                .setDescription(model.getDescription())
                .setImageUrl(model.getSavedFileName())
                .setSlug(model.getSlug())
                .setId(UUID.randomUUID());

        String sql = "INSERT INTO categories(category_id, name, description, image_url, category_slug) VALUES (?,?,?,?,?)";
        try (PreparedStatement prop = connection.prepareStatement(sql)) {
            prop.setString(1, category.getId().toString());
            prop.setString(2, category.getName());
            prop.setString(3, category.getDescription());
            prop.setString(4, category.getImageUrl());
            prop.setString(5, category.getSlug());
            prop.executeUpdate();
            return category;
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
            return null;
        }


    }
}
