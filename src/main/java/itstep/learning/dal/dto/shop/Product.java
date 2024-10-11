package itstep.learning.dal.dto.shop;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class Product {
    private UUID id;
    private UUID categoryId;
    private String name;
    private String slug; // Part of url adres - for advertisement | user can understand what looking for
    private double price;
    private String description;
    private String imageUrl;
    private Date deleteDt;

    public Product() {}

    public Product(ResultSet resultSet) throws SQLException {
        this.setId(UUID.fromString(resultSet.getString("product_id")))
                .setCategoryId(UUID.fromString(resultSet.getString("category_id")))
                .setSlug(resultSet.getString("product_slug"))
                .setDescription(resultSet.getString("product_description"))
                .setName(resultSet.getString("product_name"))
                .setImageUrl(resultSet.getString("product_image_url"))
                .setPrice(resultSet.getDouble("product_price"));
        Timestamp timestamp = resultSet.getTimestamp("delete_dt");
        if (timestamp != null) {
            this.setDeleteDt(new Date(timestamp.getTime()));
        }
    }

    public UUID getId() {
        return id;
    }
    public Product setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getSlug() {
        return slug;
    }
    public Product setSlug(String slug) {
        this.slug = slug;
        return this;
    }

    public UUID getCategoryId() {
        return categoryId;
    }
    public Product setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public String getName() {
        return name;
    }
    public Product setName(String name) {
        this.name = name;
        return this;
    }

    public double getPrice() {
        return price;
    }
    public Product setPrice(double price) {
        this.price = price;
        return this;
    }

    public String getDescription() {
        return description;
    }
    public Product setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public Product setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public Date getDeleteDt() {
        return deleteDt;
    }
    public Product setDeleteDt(Date deleteDt) {
        this.deleteDt = deleteDt;
        return this;
    }

}
