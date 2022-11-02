package edu.scu.myranch.utils;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class Emails {
    /* 各个参数的含义：
    *   host: 邮件服务器的地址
    *   from: 邮件发送者的邮箱
    *   to: 邮件接收者的邮箱
    *   user: 邮件发送者的用户名
    *   passwd: 邮件发送者的密码，注意这里的密码是指邮箱的第三方授权码，不同邮箱的授权码不同，可以自行搜索怎么获取某个邮箱的授权码
    *   subject: 邮件主题
    *   content: 邮件内容
    *   contentType: 邮件类型
    *
    *
    *  以下是使用该方法的一个例子：
    * public static void main(String[] args) {
        try {
            sendMail("smtp.qq.com", "2296566898@qq.com", "3438178879@qq.com", "2296566898", "xjvszfmouepaebbj", "MyRanch 登录验证", "验证码：979727", "text/html;charset=gbk");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
     }
    * */
    public static void sendMail (
            String host,
            String from,
            String to,
            String user,
            String passwd,
            String subject,
            String content,
            String contentType ) throws MessagingException {

        Properties prop = new Properties();
        prop.setProperty("mail.transport.protocol", "smtp");
        prop.setProperty("mail.auth", "true");
        Session session = Session.getInstance(prop);

        MimeMessage msg = new MimeMessage(session);
        msg.setSubject(subject);
        msg.setFrom(new InternetAddress(from));

        msg.setContent(content, contentType);
        msg.saveChanges();

        Transport transport = session.getTransport();
        transport.connect(host, 25, user, passwd);
        transport.sendMessage(msg, InternetAddress.parse(to));
        transport.close();
    }

}
