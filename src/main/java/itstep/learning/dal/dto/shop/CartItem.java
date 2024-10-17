package itstep.learning.dal.dto.shop;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CartItem {
    private UUID cartId;
    private UUID productId;
    private int quantity;
    //private double price;

    private Product product;
    
    
    public CartItem() {
        
    }

    public CartItem(ResultSet rs) throws SQLException {
        this.setCartId(UUID.fromString(rs.getString("cart_id")));
        this.setProductId(UUID.fromString(rs.getString("product_id")));
        this.setQuantity(rs.getInt("quantity"));
        try {
            product = new Product(rs);
        }catch (Exception ignored) {

        }
    }


    public UUID getCartId() {
        return cartId;
    }

    public CartItem setCartId(UUID cartId) {
        this.cartId = cartId;
        return this;
    }

    public UUID getProductId() {
        return productId;
    }

    public CartItem setProductId(UUID productId) {
        this.productId = productId;
        return this;
    }

    public int getQuantity() {
        return quantity;
    }

    public CartItem setQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public Product getProduct() {
        return product;
    }

    public CartItem setProduct(Product product) {
        this.product = product;
        return this;
    }
}
