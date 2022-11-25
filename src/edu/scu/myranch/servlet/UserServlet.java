package edu.scu.myranch.servlet;

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
import java.util.Random;
import java.util.ResourceBundle;

@WebServlet({"/user/passwdlogin", "/user/sendemail", "/user/emaillogin", "/user/sendemail2", "/user/register"})
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
}
