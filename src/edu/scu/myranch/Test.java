package edu.scu.myranch;

import edu.scu.myranch.service.Acounts;

import java.io.UnsupportedEncodingException;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class Test {
    public static void main(String[] args) {
        try {
            Acounts.register("qhh", "123456", "2296566898@qq.com", 0);
            Acounts.register("gqh", "123456", "2634612924@qq.com", 0);
            Acounts.register("frd", "123456", "1875742195@qq.com", 0);
            Acounts.register("zwh", "123456", "3190616388@qq.com", 0);
            Acounts.register("lky", "123456", "2521033587@qq.com", 0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
