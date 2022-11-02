package edu.scu.myranch.user;

import edu.scu.myranch.utils.encryption.Sha256;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Random;

public class Acounts {
    public static String register(String userName, String passwd, String email, int userType) throws SQLException, UnsupportedEncodingException, NoSuchAlgorithmException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/MyRanch", "root", "mysqlpasswd");

            String sql = "select email from UserInfo where email = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return null;
            }

            // This is a very simple algorithm to generate a new id. It can be improved when it is necessary.
            Random random = new Random();
            sql = "select id from UserInfo where id = ?";
            ps = conn.prepareStatement(sql);
            String id;
            do {
                id = String.valueOf(random.nextLong(7000000000L) + 3000000000L);
                ps.setString(1, id);
                rs = ps.executeQuery();
            } while (rs.next());

            // Obviously, the password must be encrypted before it's stored.
            sql = "insert into UserInfo values (" + ("'" + id + "'") + ", " +
                                                    ("'" + userName + "'") + ", " +
                                                    ("'" + Sha256.getSHA256(passwd) + "'") + ", " +
                                                    ("'" + email + "'") + ", " +
                                                    ("'" + userType + "'")  + ")";
            ps = conn.prepareStatement(sql);
            int res = ps.executeUpdate();
            if (res == 0) {
                return null;
            }

            return id;

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static boolean loginWithUserId(String userId, String passwd) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/MyRanch", "root", "mysqlpasswd");

            String sql = "select * from UserInfo where id = ? and passWd = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            ps.setString(2, Sha256.getSHA256(passwd));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
