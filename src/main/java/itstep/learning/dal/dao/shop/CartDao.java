package itstep.learning.dal.dao.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dto.shop.CartItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class CartDao {

    private final Connection connection;
    private final Logger logger;

    @Inject
    public CartDao(Connection connection, Logger logger) {
        this.connection = connection;
        this.logger = logger;
    }

    public List<CartItem> getCart(String userId) {
        UUID uuid;
        try {
            uuid = UUID.fromString(userId);
        } catch (Exception ignored) {
            return null;
        }

        String sql = "SELECT * FROM carts c " +
                " JOIN cart_items ci ON c.cart_id = ci.cart_id " +
                " JOIN products p ON p.product_id = ci.product_id " +
                " WHERE c.user_id= ? AND c.close_dt IS NULL AND c.is_canceled = 0";
        try (PreparedStatement prop = connection.prepareStatement(sql)) {
            prop.setString(1, uuid.toString());
            ResultSet rs = prop.executeQuery();
            List<CartItem> cartItems = new ArrayList<>();
            while (rs.next()) {
                cartItems.add(new CartItem(rs));
            }
            return cartItems;
        } catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
            return null;
        }

    }

    public UUID getLastCart(UUID userId) {

        String sql = "SELECT cart_id FROM carts c WHERE c.user_id= ? AND c.close_dt IS NULL AND c.is_canceled = 0";
        try (PreparedStatement prop = connection.prepareStatement(sql)) {
            prop.setString(1, userId.toString());
            ResultSet rs = prop.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString("cart_id"));
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
            return null;
        }
        return null;
    }

    public boolean setNewCart(String userId, String tmpId) {
        UUID userUuid, tmpUuid;
        try {
            userUuid = UUID.fromString(userId);
            tmpUuid = UUID.fromString(tmpId);
        } catch (Exception ignored) {
            return false;
        }

        System.out.println(userUuid + " " + tmpUuid);

        String sql = "UPDATE carts SET user_id = ? WHERE user_id = ?";
        try(PreparedStatement prop = connection.prepareStatement(sql)) {
            prop.setString(1, userUuid.toString());
            prop.setString(2, tmpUuid.toString());
            prop.executeUpdate();
            return true;
        }catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
        }
        return  false;
    }

    public boolean closeCart(UUID cartId, boolean isCanceled) {
        String sql = "UPDATE carts SET close_dt = CURRENT_TIMESTAMP, is_canceled = ? WHERE cart_id = ?";
        try (PreparedStatement prop = connection.prepareStatement(sql)) {
            prop.setInt(1, isCanceled ? 1 : 0);
            prop.setString(2, cartId.toString());
            prop.executeUpdate();
            return true;
        } catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage() + " -- " + sql, ex);
            return false;
        }
    }

    public boolean update(UUID cartId, UUID productId, int delta) throws Exception {
        if (cartId == null || productId == null || delta == 0) {
            return false;
        }

        int count = 0;
        String sql = "SELECT quantity FROM cart_items c WHERE c.cart_id = ? AND c.product_id = ?";
        try (PreparedStatement prop = connection.prepareStatement(sql)) {
            prop.setString(1, cartId.toString());
            prop.setString(2, productId.toString());
            ResultSet rs = prop.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            } else {
                return false;
            }

        } catch (SQLException e) {
            logger.log(Level.WARNING, e.getMessage() + " -- " + sql, e);
            throw new Exception();
        }

        count += delta;
        if (count < 0) return false;
        if (count == 0) {
            sql = "DELETE FROM cart_items WHERE cart_id = ? AND product_id = ?";
        } else {
            sql = "UPDATE cart_items SET quantity = ? WHERE cart_id = ? AND product_id = ?";
        }

        try (PreparedStatement prop = connection.prepareStatement(sql)) {
            if (count == 0) {
                prop.setString(1, cartId.toString());
                prop.setString(2, productId.toString());
            } else {
                prop.setInt(1, count);
                prop.setString(2, cartId.toString());
                prop.setString(3, productId.toString());

            }
            prop.executeUpdate();

            return true;
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage() + " -- " + sql, e);
            throw new Exception();
        }
    }

    public boolean add(UUID userId, UUID productId, int quantity) {
        UUID cartId = null;
        String sql = "SELECT c.cart_id FROM carts c WHERE c.user_id = ? AND c.close_dt IS NULL";
        try (PreparedStatement prop = connection.prepareStatement(sql)) {
            prop.setString(1, userId.toString());
            ResultSet rs = prop.executeQuery();
            if (rs.next()) {
                cartId = UUID.fromString(rs.getString(1));
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, e.getMessage() + " -- " + sql, e);
            return false;
        }

        if (cartId == null) {
            cartId = UUID.randomUUID();
            sql = "INSERT INTO carts (cart_id, user_id) VALUES (?, ?)";
            try (PreparedStatement prop = connection.prepareStatement(sql)) {
                prop.setString(1, cartId.toString());
                prop.setString(2, userId.toString());
                prop.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, e.getMessage() + " -- " + sql, e);
                return false;
            }
        }

        int count = 0;
        sql = "SELECT quantity FROM cart_items c WHERE c.cart_id = ? AND c.product_id = ?";
        try (PreparedStatement prop = connection.prepareStatement(sql)) {
            prop.setString(1, cartId.toString());
            prop.setString(2, productId.toString());
            ResultSet rs = prop.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            logger.log(Level.WARNING, e.getMessage() + " -- " + sql, e);
            return false;
        }

        if (count == 0) {
            sql = "INSERT INTO cart_items (quantity, cart_id, product_id) VALUES (?, ?, ?)";
        } else {
            sql = "UPDATE cart_items SET quantity = ? WHERE cart_id = ? AND product_id = ?";
        }

        try (PreparedStatement prop = connection.prepareStatement(sql)) {
            prop.setInt(1, count + quantity);
            prop.setString(2, cartId.toString());
            prop.setString(3, productId.toString());
            prop.executeUpdate();
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage() + " -- " + sql, e);
            return false;
        }
        return true;
    }

    public boolean installTables() {
        String sql = "CREATE TABLE IF NOT EXISTS carts (" +
                "cart_id CHAR(36) PRIMARY KEY DEFAULT(UUID())," +
                "user_id CHAR(36) NOT NULL," +
                "open_dt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "close_dt DATETIME NULL," +
                "is_canceled TINYINT NOT NULL DEFAULT 0" +
                ") ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
            return false;
        }

        sql = "CREATE TABLE IF NOT EXISTS cart_items (" +
                "cart_id CHAR(36) NOT NULL," +
                "product_id CHAR(36) NOT NULL," +
                "quantity INT UNSIGNED NOT NULL DEFAULT 1," +
                "PRIMARY KEY (cart_id, product_id)" +
                ") ENGINE=InnoDB, DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            return true;
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
            return false;
        }

    }
}
