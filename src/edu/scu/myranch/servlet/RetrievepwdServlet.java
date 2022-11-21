package edu.scu.myranch.servlet;


import edu.scu.myranch.utils.DBUtils;
import edu.scu.myranch.utils.Emails;
import edu.scu.myranch.utils.encryption.Sha256;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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

@WebServlet({"/user/retrievepasswd", "/user/sendemail3"})
public class RetrievepwdServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        String servlet = req.getServletPath();

        if("/user/retrievepasswd".equals(servlet)){
            try {
                doSetNewPasswd(req,resp);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        } else if ("/user/sendemail3".equals(servlet)) {
            doSendeEmail3(req, resp);
        }
    }
    protected void doSetNewPasswd(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, NoSuchAlgorithmException {
        HttpSession session = request.getSession();
        String vericode = (session == null ? null : (String) session.getAttribute("vericode"));
        String email = (session == null ? null : (String) session.getAttribute("email"));
        Connection conn = DBUtils.getConnection();
        PreparedStatement ps = null;
        String sql = "select * from UserInfo where email = ?";
        ResultSet rs = null;
        String oldpasswd = "";
        try{
            ps = conn.prepareStatement(sql);
            ps.setString(1,email);
            rs = ps.executeQuery();
            boolean emailexist = rs.next();
            if(emailexist){
                oldpasswd = rs.getString("passWd");
            }
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }finally {
            DBUtils.close(conn, ps, rs);
        }
        String vericodeProvided = request.getParameter("vericode");
        String emailProvided = request.getParameter("email");
        String newPasswd = request.getParameter("password");

        String newPasswdEncrypted = Sha256.getSHA256(newPasswd);
        Connection conn1 = DBUtils.getConnection();
        PreparedStatement ps1 = null;
        String sql1 = "update UserInfo set passWd = ? where email = ?";
        ResultSet rs1 = null;
        boolean retrieveSuccess = false;
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");
        if(vericode.equals(vericodeProvided) && email.equals(emailProvided)) {
            try{
                ps1 = conn1.prepareStatement(sql1);
                ps1.setString(1, newPasswdEncrypted);
                ps1.setString(2, email);
                if (ps1.executeUpdate() > 0) {
                    out.print("修改密码成功");
                } else {
                    out.print("修改密码失败，内部错误");
                }
            }catch(SQLException e){
                throw new RuntimeException(e);
            }finally {
                DBUtils.close(conn1, ps1, rs1);
            }
        } else {
            out.print("邮箱或验证码错误");
        }
    }

    protected void doSendeEmail3(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
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
                Emails.sendMail(host, from, email, user, auth, "MyRanch找回密码验证",
                        "您正在找回[MyRanch：" + id + "]的密码, 验证码：" + vericode, contentType);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
            HttpSession session = request.getSession();
            session.setAttribute("vericode", vericode);
            session.setAttribute("email", email);

            out.print("验证码已发送");
        } else {
            out.print("该邮箱未注册或者是一个无效邮箱");
        }

    }
}
