<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>MyRanch注册</title>
</head>
<body>
<form action="/MyRanch/user/register" method="post">
    userName:<input name="userName" type="text">  <br>
    passWd:<input name="passWd">  <br>
    email<input name="email" type="email">
    <input type="submit" value="发送验证码" name="sendemail"/> <br>
    <input type="text" name="vericode"> <br>
    <button type="submit">注册</button>
</form>
<%--    <%--%>
<%--        if((boolean)request.getAttribute("success")){--%>
<%--            out.print("alert('验证码已发送', 'success', 10000)");--%>
<%--        }--%>
<%--//        out.flush();--%>
<%--    %>--%>
</body>
</html>