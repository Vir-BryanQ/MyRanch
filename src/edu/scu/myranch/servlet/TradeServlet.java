package edu.scu.myranch.servlet;

import com.alibaba.fastjson.JSON;
import edu.scu.myranch.utils.DBUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import java.util.ResourceBundle;

class Good {
    private String id;
    private String userId;
    private String username;
    private String productName;
    private String productPrice;
    private String productDesc;
    private String base64;

    public Good() {
    }

    public Good(String id, String userId, String username, String productName, String productPrice, String productDesc, String base64) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productDesc = productDesc;
        this.base64 = base64;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Good good = (Good) o;
        return Objects.equals(id, good.id) && Objects.equals(userId, good.userId) && Objects.equals(username, good.username) && Objects.equals(productName, good.productName) && Objects.equals(productPrice, good.productPrice) && Objects.equals(productDesc, good.productDesc) && Objects.equals(base64, good.base64);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, username, productName, productPrice, productDesc, base64);
    }

    @Override
    public String toString() {
        return "Good{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", productName='" + productName + '\'' +
                ", productPrice='" + productPrice + '\'' +
                ", productDesc='" + productDesc + '\'' +
                ", base64='" + base64 + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }
}

@WebServlet({"/trade/getAllGoods"})
public class TradeServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if ("/trade/getAllGoods".equals(servletPath)) {
            try {
                doGetAllGoods(request, response);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void doGetAllGoods(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null) {
            ArrayList<Good> goods = new ArrayList<>();

            Connection conn = DBUtils.getConnection();
            String sql = "select * from TradeList";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String md5 = rs.getString("imageFile");
                FileInputStream fis = new FileInputStream(GroupServlet.descImgPath + "/" + md5);
                String base64 = Base64.getEncoder().encodeToString(fis.readAllBytes());
                fis.close();

                goods.add(new Good(rs.getString("id"), rs.getString("userId"), rs.getString("username"),
                                    rs.getString("productName"), rs.getString("productPrice"), rs.getString("productDesc"),
                                    base64));
            }

            String json = JSON.toJSONString(goods);
            out.print(json);

            DBUtils.close(conn, ps, rs);
        }
    }
}
