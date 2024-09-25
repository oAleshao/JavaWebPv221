package itstep.learning.dal.dao;

import com.google.inject.Inject;
import itstep.learning.dal.dto.Token;
import itstep.learning.dal.dto.User;
import itstep.learning.services.hash.HashService;

import javax.inject.Named;
import java.sql.*;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenDao {
    private final Connection connection;
    private final Logger logger;

    @Inject
    public TokenDao(Connection connection, Logger logger) {
        this.connection = connection;
        this.logger = logger;
    }

    public Token create(User user) {
        Token token = new Token();
        token.setTokenId(UUID.randomUUID());
        token.setUserId(user.getId());
        token.setIat(new Date(System.currentTimeMillis()));
        token.setExp(new Date(System.currentTimeMillis() + 1000 * 60 * 5));

        String sql = "INSERT INTO tokens (token_id, user_id, exp, iat) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, token.getTokenId().toString());
            ps.setString(2, token.getUserId().toString());
            ps.setTimestamp(3, new Timestamp(token.getExp().getTime()));
            ps.setTimestamp(4, new Timestamp(token.getIat().getTime()));
            ps.executeUpdate();

            return token;
        } catch (SQLException ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
            return null;
        }

    }

    public boolean installTables() {
        String sql = "CREATE TABLE IF NOT EXISTS tokens (" +
                "token_id CHAR(36) PRIMARY KEY DEFAULT(UUID())," +
                "user_id CHAR(36) NOT NULL," +
                "exp DATETIME NULL," +
                "iat DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
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
