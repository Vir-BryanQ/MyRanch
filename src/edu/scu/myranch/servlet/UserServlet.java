package edu.scu.myranch.servlet;

import com.alibaba.fastjson.JSON;
import com.sun.mail.smtp.DigestMD5;
import edu.scu.myranch.service.Acounts;
import edu.scu.myranch.utils.DBUtils;
import edu.scu.myranch.utils.Emails;
import edu.scu.myranch.utils.encryption.Sha256;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Random;
import java.util.ResourceBundle;

class User {
    private String id;
    private String username;

    public User() {
    }

    public User(String id, String username) {
        this.id = id;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}

@WebServlet({"/user/passwdlogin", "/user/sendemail", "/user/emaillogin",
        "/user/sendemail2", "/user/register", "/user/getUserInfo",
        "/user/getSession", "/user/showUserInfo", "/user/changeInfo"
})
public class UserServlet extends HttpServlet {
    private static String userDataDir = Thread.currentThread().getContextClassLoader().getResource("UserData").getPath();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if ("/user/passwdlogin".equals(servletPath)) {
            doPassWordLogin(request, response);
        } else if ("/user/sendemail".equals(servletPath)) {
            doSendEmail(request, response);
        } else if ("/user/emaillogin".equals(servletPath)) {
            doEmailLogin(request, response);
        } else if ("/user/sendemail2".equals(servletPath)) {
            doSendEmail2(request, response);
        } else if ("/user/register".equals(servletPath)) {
            doRegister(request,response);
        } else if ("/user/getUserInfo".equals(servletPath)) {
            doGetUserInfo(request, response);
        } else if ("/user/getSession".equals(servletPath)) {
            doGetSession(request, response);
        } else if ("/user/showUserInfo".equals(servletPath)) {
            try {
                doShowUserInfo(request,response);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if ("/user/changeInfo".equals(servletPath)) {
            try {
                try {
                    doChangeInfo(request,response);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void doGetSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        out.print(session == null ? "1" : "0");
    }

    private void doGetUserInfo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null) {
            String id = (String) session.getAttribute("id");
            String username = (String) session.getAttribute("username");
            if (id != null && username != null) {
                out.print(JSON.toJSONString(new User(id, username)));
            }
        }
    }


    protected void doPassWordLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        String id = request.getParameter("id");
        String password = request.getParameter("password");

        if (id != null && password != null) {
            Connection conn = DBUtils.getConnection();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "select * from UserInfo where id = ? and passWd = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, id);
                ps.setString(2, Sha256.getSHA256(password));
                rs = ps.executeQuery();
                if (rs.next()) {
                    HttpSession session = request.getSession();
                    String curDir = userDataDir + "/" + id;
                    session.setAttribute("curDir", curDir);
                    session.setAttribute("rootDir", curDir);
                    session.setAttribute("id", id);
                    session.setAttribute("username", rs.getString("userName"));
                    out.print("");
                } else {
                    out.print("账号或密码错误");
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } finally {
                DBUtils.close(conn, ps, rs);
            }
        }
    }

    protected void doSendEmail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        ResourceBundle bundle = ResourceBundle.getBundle("Resources.email");
        String host = bundle.getString("host");
        String from = bundle.getString("from");
        String user = bundle.getString("user");
        String auth = bundle.getString("auth");
        String contentType = bundle.getString("contentType");

        String email = request.getParameter("email");

        Connection conn = DBUtils.getConnection();
        PreparedStatement ps = null;
        String sql = "select * from UserInfo where email = ?";
        ResultSet rs = null;
        boolean emailExist = false;
        String id = "";
        String username = "";
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            rs = ps.executeQuery();
            emailExist = rs.next();
            if (emailExist) {
                id = rs.getString("id");
                username = rs.getString("username");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtils.close(conn, ps, rs);
        }

        if (emailExist) {
            String vericode = Integer.toHexString(new Random().nextInt(0x1000000 - 0x100000) + 0x100000);
            try {
                Emails.sendMail(host, from, email, user, auth, "MyRanch登录验证",
                        "您正在登录[MyRanch" + id + "], 验证码：" + vericode, contentType);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }

            HttpSession session = request.getSession();
            session.setAttribute("vericode", vericode);
            session.setAttribute("email", email);
            session.setAttribute("id", id);
            session.setAttribute("username", username);

            out.print("验证码已发送");
        } else {
            out.print("该邮箱未注册或者是一个无效邮箱");
        }

    }

    protected void doEmailLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        String vericode = (session == null ? null : (String) session.getAttribute("vericode"));
        String email = (session == null ? null : (String) session.getAttribute("email"));
        String id = (session == null ? null : (String) session.getAttribute("id"));
        String username = (session == null ? null : (String) session.getAttribute("username"));
        String vericodeProvided = request.getParameter("vericode");
        String emailProvided = request.getParameter("email");

        if (vericode != null && email != null && vericode.equals(vericodeProvided) && email.equals(emailProvided)) {
            String curDir = userDataDir + "/" + id;
            session.setAttribute("curDir", curDir);
            session.setAttribute("rootDir", curDir);
            session.setAttribute("username", username);
            session.setAttribute("id",id);
            out.print("");
        } else {
            out.print("邮箱或验证码错误");
        }
    }

    protected  void doSendEmail2(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        ResourceBundle bundle = ResourceBundle.getBundle("Resources.email");
        String host = bundle.getString("host");
        String from = bundle.getString("from");
        String user = bundle.getString("user");
        String auth = bundle.getString("auth");
        String contentType = bundle.getString("contentType");

        String email = request.getParameter("email");

        Connection conn = DBUtils.getConnection();
        PreparedStatement ps = null;
        String sql = "select * from UserInfo where email = ?";
        ResultSet rs = null;
        boolean emailExist = false;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            rs = ps.executeQuery();
            emailExist = rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtils.close(conn, ps, rs);
        }

        if (!emailExist) {
            String vericode = Integer.toHexString(new Random().nextInt(0x1000000 - 0x100000) + 0x100000);
            try {
                Emails.sendMail(host, from, email, user, auth, "MyRanch注册验证",
                        "您正在注册MyRanch账号, 验证码：" + vericode, contentType);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }

            HttpSession session = request.getSession();
            session.setAttribute("vericode", vericode);
            session.setAttribute("email", email);

            out.print("验证码已发送");
        } else {
            out.print("该邮箱已被注册");
        }
    }

    protected  void doRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        HttpSession session = request.getSession(false);
        String vericode = (session == null ? null : (String) session.getAttribute("vericode"));
        String vericodeProvided = request.getParameter("vericode");
        String emailProvided = request.getParameter("email");

        String userId = null;
        if (vericode!=null && vericodeProvided!=null && email.equals(emailProvided) && vericode.equals(vericodeProvided)) {
            try {
                try {
                    userId = Acounts.register(username,password,email,0);
                    if (userId == null) {
                        out.print("注册失败，该邮箱已被注册");
                    } else {
                        out.print("注册成功，您的用户id为：" + userId);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            out.print("邮箱或验证码错误");
        }
    }

    protected void doShowUserInfo(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null) {
            String userId = (String) session.getAttribute("id");
//            System.out.println(userId);
            Connection conn = DBUtils.getConnection();
            String sql = "select * from userinfo where id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                String userName = rs.getString("userName");
                String email = rs.getString("email");
                String region = rs.getString("region");
                String gender = rs.getString("gender");
                String phone = rs.getString("phone");
                showUser showUser = new showUser(userId, userName, email, phone, region, gender);
                String jsonStr = JSON.toJSONString(showUser);
                out.print(jsonStr);
            }
            DBUtils.close(conn,ps,rs);
        }
    }

    protected void doChangeInfo(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException, NoSuchAlgorithmException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null) {
            String newMessage = request.getParameter("newMessage");
            String userId = (String) session.getAttribute("id");
            String type = request.getParameter("type");
//            System.out.println(newMessage);
//            System.out.println(userId);
//            System.out.println(type);
            Connection conn = DBUtils.getConnection();
            String sql = null;
            PreparedStatement ps = null;
            int i = 0;
            if (type.equals("0")) {
                sql = "UPDATE userInfo SET `userName` = " + "'" + newMessage + "'" + " WHERE `id` = " + userId;
//                ps.setString(1,userId);
                ps = conn.prepareStatement(sql);
                i = ps.executeUpdate();
            } else if (type.equals("1")) {
                sql = "UPDATE userInfo SET `phone` = " + "'" + newMessage + "'" + " WHERE `id` = " + userId;
//                ps.setString(1,userId);
                ps = conn.prepareStatement(sql);
                i = ps.executeUpdate();
            } else if (type.equals("2")) {
                sql = "UPDATE userInfo SET `gender` = " + "'" + newMessage + "'" + " WHERE `id` = " + userId;
//                ps.setString(1,userId);
                ps = conn.prepareStatement(sql);
                i = ps.executeUpdate();
            } else if (type.equals("3")) {
                sql = "UPDATE userInfo SET `region` = " + "'" + newMessage + "'" + " WHERE `id` = "+ userId;
//                ps.setString(1,userId);
                ps = conn.prepareStatement(sql);
                i = ps.executeUpdate();
            } else if (type.equals("4")) {
                sql = "UPDATE userInfo SET `passWd` = " + "'" + Sha256.getSHA256(newMessage) + "'" + " WHERE `id` = "+ userId;
//                ps.setString(1,userId);
                ps = conn.prepareStatement(sql);
                i = ps.executeUpdate();
            }

            if (i > 0) {
                out.print("0");
            }

            DBUtils.close(conn,ps,null);
        }
    }
}

class showUser {
    private String userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String userRegion;
    private String userGender;

    public showUser() {
    }

    public showUser(String userId, String userName, String userEmail, String userPhone, String userRegion, String userGender) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.userRegion = userRegion;
        this.userGender = userGender;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserRegion() {
        return userRegion;
    }

    public void setUserRegion(String userRegion) {
        this.userRegion = userRegion;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        showUser showUser = (showUser) o;
        return Objects.equals(userId, showUser.userId) && Objects.equals(userName, showUser.userName) && Objects.equals(userEmail, showUser.userEmail) && Objects.equals(userPhone, showUser.userPhone) && Objects.equals(userRegion, showUser.userRegion) && Objects.equals(userGender, showUser.userGender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, userName, userEmail, userPhone, userRegion, userGender);
    }

    @Override
    public String toString() {
        return "showUser{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userPhone='" + userPhone + '\'' +
                ", userRegion='" + userRegion + '\'' +
                ", userGender='" + userGender + '\'' +
                '}';
    }
}
