package edu.scu.myranch.service;

import edu.scu.myranch.utils.DBUtils;
import edu.scu.myranch.utils.encryption.Sha256;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Objects;
import java.util.Random;

public class Acounts {
    public static String register(String userName, String passwd, String email, int userType) throws SQLException, UnsupportedEncodingException, NoSuchAlgorithmException {
        Connection conn = DBUtils.getConnection();
        String sql = "select email from UserInfo where email = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            DBUtils.close(conn, ps, rs);
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

        // create a repo for the new user
        String userDataDir = Thread.currentThread().getContextClassLoader().getResource("UserData").getPath();
        String repoPath = userDataDir + "/" + id;
        File file = new File(repoPath);
        file.mkdir();

        // provide a sample group for the new user
        File file1 = new File(repoPath + "/sample");
        file1.mkdir();

        DBUtils.close(conn, ps, rs);
        return (res == 0 ? null : id);
    }

    public static boolean loginWithUserId(String userId, String passwd) throws UnsupportedEncodingException, NoSuchAlgorithmException, SQLException {
        Connection conn = DBUtils.getConnection();
        String sql = "select * from UserInfo where id = ? and passWd = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps = conn.prepareStatement(sql);
        ps.setString(1, userId);
        ps.setString(2, Sha256.getSHA256(passwd));
        ResultSet rs = ps.executeQuery();
        DBUtils.close(conn, ps, rs);
        return rs.next();
    }


}


