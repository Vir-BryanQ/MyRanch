package edu.scu.myranch.servlet;

import edu.scu.myranch.utils.DBUtils;
import edu.scu.myranch.utils.Emails;
import edu.scu.myranch.utils.encryption.Sha256;
import jakarta.servlet.RequestDispatcher;
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

@WebServlet({"/user/passwdlogin", "/user/emaillogin"})
public class UserServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String servletPath = request.getServletPath();

        if ("/user/passwdlogin".equals(servletPath)) {
            doPassWordLogin(request, response);
        } else if ("/user/emaillogin".equals(servletPath)) {
            if (request.getParameter("sendemail") != null) {
                doSendEmail(request, response);
            } else {
                doEmailLogin(request, response);
            }
        }
    }


    protected void doPassWordLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        String password = request.getParameter("password");

        boolean success = false;
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
                success = rs.next();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } finally {
                DBUtils.close(conn, ps, rs);
            }
        }


        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");
        if (success) {
            out.print("<h1>Success Page</h1>");
        } else {
            out.print("<h1>Failed to login in</h1>");
            out.print("<a href=\"\\MyRanch\\login.jsp\">返回登录页面</a>");
        }
    }

    protected void doSendEmail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ResourceBundle bundle = ResourceBundle.getBundle("Resources.email");
        String host = bundle.getString("host");
        String from = bundle.getString("from");
        String user = bundle.getString("user");
        String auth = bundle.getString("auth");
        String contentType = bundle.getString("contentType");

        String email = request.getParameter("email");

        System.out.println(email);

        Connection conn = DBUtils.getConnection();
        PreparedStatement ps = null;
        String sql = "select * from UserInfo where email = ?";
        ResultSet rs = null;
        boolean emailExist = false;
        String id = "";
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            rs = ps.executeQuery();
            emailExist = rs.next();
            if (emailExist) {
                id = rs.getString("id");
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
        }

        request.setAttribute("emailExist", emailExist);
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    protected void doEmailLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String vericode = (session == null ? null : (String) session.getAttribute("vericode"));
        String email = (session == null ? null : (String) session.getAttribute("email"));
        String vericodeProvided = request.getParameter("vericode");
        String emailProvided = request.getParameter("email");

        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");
        if (vericode != null && email != null && vericode.equals(vericodeProvided) && email.equals(emailProvided)) {
            out.print("<h1>Success Page</h1>");
        } else {
            out.print("<h1>Failed to login in</h1>");
            out.print("<a href=\"\\MyRanch\\login.jsp\">返回登录页面</a>");
        }
    }
}
