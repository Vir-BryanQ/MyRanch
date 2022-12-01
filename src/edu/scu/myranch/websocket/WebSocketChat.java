package edu.scu.myranch.websocket;

import com.alibaba.fastjson.JSON;
import edu.scu.myranch.utils.MD5Utils;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class FromMessage {
    private String receiver;
    private String type;
    private String date;
    private String content;

    public FromMessage() {
    }

    public FromMessage(String receiver, String type, String date, String content) {
        this.receiver = receiver;
        this.type = type;
        this.date = date;
        this.content = content;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FromMessage that = (FromMessage) o;
        return Objects.equals(receiver, that.receiver) && Objects.equals(type, that.type) && Objects.equals(date, that.date) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiver, type, date, content);
    }

    @Override
    public String toString() {
        return "FromMessage{" +
                "receiver='" + receiver + '\'' +
                ", type='" + type + '\'' +
                ", date='" + date + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}

class ToMessage {
    private String sender;
    private String senderName;
    private String type;
    private String date;
    private String content;

    public ToMessage() {
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public ToMessage(String sender, String senderName, String type, String date, String content) {
        this.sender = sender;
        this.senderName = senderName;
        this.type = type;
        this.date = date;
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToMessage toMessage = (ToMessage) o;
        return Objects.equals(sender, toMessage.sender) && Objects.equals(senderName, toMessage.senderName) && Objects.equals(type, toMessage.type) && Objects.equals(date, toMessage.date) && Objects.equals(content, toMessage.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, senderName, type, date, content);
    }

    @Override
    public String toString() {
        return "ToMessage{" +
                "sender='" + sender + '\'' +
                ", senderName='" + senderName + '\'' +
                ", type='" + type + '\'' +
                ", date='" + date + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}

class OnlineUser {
    private String id;
    private String username;

    public OnlineUser() {
    }

    public OnlineUser(String id, String username) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnlineUser that = (OnlineUser) o;
        return Objects.equals(id, that.id) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return "OnlineUser{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}

@ServerEndpoint(value = "/chat", configurator = GetHttpSessionConfigurator.class)
public class WebSocketChat {

    private static Map<String, WebSocketChat> onlineUsers = new ConcurrentHashMap<>();

    private HttpSession httpSession;
    private Session session;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        onlineUsers.put((String) httpSession.getAttribute("id"), this);

        // 获取所有在线用户
        Set<String> keySet = onlineUsers.keySet();
        ArrayList<OnlineUser> users = new ArrayList<>();
        for (String key : keySet) {
            WebSocketChat wsc = onlineUsers.get(key);
            String id = (String) wsc.httpSession.getAttribute("id");
            String username = (String) wsc.httpSession.getAttribute("username");
            users.add(new OnlineUser(id, username));
        }

        // 向所有用户广播
        ToMessage toMessage = new ToMessage((String) httpSession.getAttribute("id"), (String) httpSession.getAttribute("username"),
                "system-broadcast", new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format(new Date()), JSON.toJSONString(users));
        broadcast(toMessage);
    }

    private static void broadcast(ToMessage toMessage) {
        String toMessageJSONStr = JSON.toJSONString(toMessage);
        Set<String> keySet = onlineUsers.keySet();
        for (String key : keySet) {
            try {
                onlineUsers.get(key).session.getBasicRemote().sendText(toMessageJSONStr);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        try {
            session.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        onlineUsers.remove((String) httpSession.getAttribute("id"));

        // 获取所有在线用户
        Set<String> keySet = onlineUsers.keySet();
        ArrayList<OnlineUser> users = new ArrayList<>();
        for (String key : keySet) {
            WebSocketChat wsc = onlineUsers.get(key);
            String id = (String) wsc.httpSession.getAttribute("id");
            String username = (String) wsc.httpSession.getAttribute("username");
            users.add(new OnlineUser(id, username));
        }

        // 向所有用户广播
        ToMessage toMessage = new ToMessage((String) httpSession.getAttribute("id"), (String) httpSession.getAttribute("username"),
                "system-broadcast", new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format(new Date()), JSON.toJSONString(users));
        broadcast(toMessage);
    }

    private FromMessage fromMessage;
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        FromMessage fromMessage = JSON.parseObject(message, FromMessage.class);
        this.fromMessage = fromMessage;

        if ("text".equals(fromMessage.getType())) {
            ToMessage toMessage = new ToMessage((String) httpSession.getAttribute("id"), (String) httpSession.getAttribute("username"),
                    fromMessage.getType(), fromMessage.getDate(), fromMessage.getContent());
            String toMessageJSONStr = JSON.toJSONString(toMessage);

            WebSocketChat receiver =  onlineUsers.get(fromMessage.getReceiver());
            if (receiver != null) {
                session.getBasicRemote().sendText(toMessageJSONStr);
                if (!((String) httpSession.getAttribute("id")).equals(receiver.httpSession.getAttribute("id"))) {
                    receiver.session.getBasicRemote().sendText(toMessageJSONStr);
                }
            }
        }
    }

    private byte[] buffer = {};
    @OnMessage
    public void onMessage(byte[] bytes, Session session, boolean finish) throws IOException {
        byte[] tmp = new byte[buffer.length + bytes.length];
        int j = 0;
        for (int i = 0; i < buffer.length; i++, j++) {
            tmp[j] = buffer[i];
        }
        for (int i = 0; i < bytes.length; i++, j++) {
            tmp[j] = bytes[i];
        }
        buffer = tmp;

        if (finish) {
            /* 传输图片分为两步：
                * 1. 传输存储了图片发送者id和图片MD5的描述头
                * 2. 传输图片
                * 接收方需要计算图片的MD5并在图片描述头缓冲区中找到与图片相对应的描述头以确定图片的发送者等信息
            * */
            String md5 = MD5Utils.byteArrayToMD5(buffer);
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            buffer = new byte[0];

            ToMessage toMessage = new ToMessage((String) httpSession.getAttribute("id"), (String) httpSession.getAttribute("username"),
                    fromMessage.getType(), fromMessage.getDate(), md5);
            String toMessageJSONStr = JSON.toJSONString(toMessage);
            WebSocketChat receiver = onlineUsers.get(fromMessage.getReceiver());
            if (receiver != null) {
                session.getBasicRemote().sendText(toMessageJSONStr);
                session.getBasicRemote().sendBinary(byteBuffer);
                if (!((String) httpSession.getAttribute("id")).equals(receiver.httpSession.getAttribute("id"))) {
                    receiver.session.getBasicRemote().sendText(toMessageJSONStr);
                    receiver.session.getBasicRemote().sendBinary(byteBuffer);
                }
            }
        }
    }
}
