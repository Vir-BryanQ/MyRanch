<%@ page import="java.sql.Connection" %>
<%@ page import="edu.scu.myranch.utils.DBUtils" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="edu.scu.myranch.servlet.GroupServlet" %>
<%@ page import="java.util.Base64" %>
<%@ page import="java.sql.SQLException" %>
<%@ page contentType="text/html; charset=UTF-8"  %>

<%
    String goodId = request.getParameter("id");
    String userId = "";
    String username = "";
    String productName = "";
    String productPrice = "";
    String productDesc = "";
    String base64 = "";

    Connection conn = DBUtils.getConnection();
    String sql = "select * from TradeList where id = ?";
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
        ps = conn.prepareStatement(sql);
        ps.setString(1, goodId);
        rs = ps.executeQuery();
        if (rs.next()) {
            String md5 = rs.getString("imageFile");
            FileInputStream fis = new FileInputStream(GroupServlet.descImgPath + "/" + md5);

            base64 = Base64.getEncoder().encodeToString(fis.readAllBytes());
            userId = rs.getString("userId");
            username = rs.getString("username");
            productName = rs.getString("productName");
            productPrice = rs.getString("productPrice");
            productDesc = rs.getString("productDesc");

            fis.close();
        }
    } catch (SQLException e) {
        throw new RuntimeException(e);
    } finally {
        DBUtils.close(conn, ps, rs);
    }

%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
    <style>
        * {
            margin: 0;
            padding: 0;
        }
        body {
            background: #EEDFCC;
        }
        /*顶部样式*/
        .first_part {
            box-shadow: 0 3px 10px 0 rgb(0 0 0 / 6%);
            width: 100%;
            /*1200px */
            height: 42px;

            background: white;

            /* 69px */
            /* margin: auto; */
        }

        .logo {
            height: 62px;
            float: left;
            line-height: 10px;
            font-size: 16px;
            padding-top: 2px;
        }

        img {
            height: 40px;
            vertical-align: middle;
        }

        .user_touxiang {
            float: right;
            margin-right: 5px;
            margin-top: 1px;
            width: 40px;
            height: 40px;
            background: url(/MyRanch/img/user.jpg);
            background-size: 40px 40px;
            background-position: -83px -40px;
            border-radius: 50%;
        }

        .user_name {
            height: 62px;
            float: right;
            margin-right: 20px;
            line-height: 43px;
        }
        /*商品描述*/
        .product-detail-nav{
            position: relative;
            background: white;
            width: 60%;
            margin-top: 10px;
            height: 60px;
            margin-left: 20%;
            border-radius: 30px;
        }
        .product-detail-nav img {
            float: left;
            margin-right: 5px;
            margin-top: 8px;
            margin-left: 10px;
            width: 40px;
            height: 40px;
            border-radius: 50%;
        }
        .product-detail-nav span {
            font-size: 18px;
            height: 100%;
            line-height: 54px;
            margin-left: 10px;
        }
        .product-detail-nav .nav-connection{
            width: 90px;
            height: 30px;
            float: right;
            position: absolute;
            top: 23%;
            right: 6%;
            border-radius: 12px;
            font-weight: bold;
            border: 0;
            color: #ffffff;
            background-color: #3bb4f2;
        }

        .product-detail-nav .nav-connection:hover{
            background-color: #E9524A;
            cursor: pointer;
        }

        .second_part {
            position: relative;
            background: white;
            width: 60%;
            margin-top: 10px;
            height: 600px;
            margin-left: 20%;
            border-radius: 50px;
        }

        .product-detail-img{
            position: absolute;
            width: 40%;
            height: 100%;
            background-color: rgba(0,0,0,.02);
        }
        .product-detail-img img {
            width: 80%;
            height: 40%;
            position: absolute;
            top: 7%;
            left: 10%;
        }
        .product-detail-describe{
            position: absolute;
            width: 50%;
            height: 100%;
            left: 45%;
        }
        .describe-name{
            position: absolute;
            font-weight: 700;
            top: 7%;

        }
        .describe-price {
            position: absolute;
            width: 100%;
            top: 15%;
            height: 32px;
            font-size: 18px;
            color: #FF4F00;
            background: #FFF2E8;
        }
        .describe-price .describe-price-1{
            color: #999;
            font-size: 12px;
            height: 100%;
            line-height: 32px;
            text-align: left;
            float: left;
            width: 20%;
            margin: 0 0 0 8px;
        }
        .describe-price .describe-price-2{
            font-size: 24px;
            line-height: 30px;
            font-weight: bolder;
        }
        .describe-count {
            position: absolute;
            width: 100%;
            top: 50%;
            height: 32px;
            font-size: 18px;
        }
        .describe-count .describe-count-1{
            color: #999;
            font-size: 12px;
            height: 100%;
            line-height: 32px;
            text-align: left;
            float: left;
            width: 12%;
            margin: 0 0 0 8px;
        }
        .describe-count .count-jian {
            position: absolute;
            width: 6%;
            height: 30px;
            border-radius: 5px 0 0 5px;
            margin-top: 0px;
            font-size: large;
            border: 0;
            font-weight: 700;
            left: 14%;
            /*color: #cccccc;*/
            background-color: rgba(0,0,0,.06);
        }
        .describe-count .count-jia {
            position: absolute;
            width: 6%;
            height: 30px;
            border-radius: 0 5px 5px 0;
            margin-top: 0px;
            font-size: large;
            border: 0;
            font-weight: 700;
            background-color: rgba(0,0,0,.06);
            left: 34%;
        }
        .describe-count .buy-count{
            position: absolute;
            border: 0;
            width: 12%;
            height: 30px;
            top: 2%;
            left: 21%;
            background-color: rgba(0,0,0,.06);
            text-align: center;
            font-weight: 700;
            font-size: 16px;
        }
        .describe-buy {
            position: absolute;
            width: 100%;
            top: 60%;
            height: 32px;
            font-size: 18px;
        }
        .describe-buy .buy-now {
            width: 50%;
            height: 40px;
            background: rgb(255, 119, 0);
            text-align: center;
            font-size: 20px;
            font-weight: bolder;
            color: white;
            border: 0;
            border-radius: 20px;
        }

        .describe-buy .buy-now:hover {
            background-color: #59c837;
            cursor: pointer;
        }

        /*遮罩层*/
        .container{
            position: fixed;
            width: 500px;
            height: 400px;
            background-color: white;
            position: fixed;
            top: 0;
            bottom: 0;
            right: 0;
            left: 0;
            margin: auto;
            border-radius: 15px;
            z-index: 5;
            display: none;
        }
        .container img {
            position: absolute;
            /*width: 80%;*/
            /*height: 100%;*/
            left: 10%;
        }
        .container #mask-cancel{
            position: absolute;
            width: 40px;
            height: 40px;
            text-align: center;
            top: -1%;
            right: 0%;
            font-weight: bolder;
            line-height: 40px;
            cursor: pointer;
        }

        .product-desc {
            position: absolute;
            width: 100%;
            top: 25%;
            height: 120px;
            font-size: 10px;
            background-color: #EDFCED;
            color: #7e57c1;
            padding: 10px;
        }

        .product-desc .product-desc-1 {
            word-break: break-all;
        }
    </style>
</head>
<body>
    <div class="first_part">
        <!--logo-->
        <div class="logo">
            <img src="/MyRanch/img/myranch1.jpg" alt="MYRANCH"> MYRANCH
        </div>
        <!-- user_名称 -->
        <div class="user_name" id="username"></div>
        <!-- user_头像 -->
        <a href="#">
            <div class="user_touxiang"></div>
        </a>
    </div>
    <div class="product-detail-nav">
        <img src="/MyRanch/img/user.jpg">
        <span style="color: #7e57c1"><% out.print(username + ": "); %></span>
        <span style="color: #E9524A">欢迎光临我的店铺~~</span>
        <button class="nav-connection" id="contact-button">联系客服</button>
    </div>
    <div class="second_part">
        <div class="product-detail-img">
            <%
                out.print("<img src=\"data:image/png;base64," + base64 + "\" style=\"max-height: 240px; max-width: 333px; height: auto; width: auto;\">");
            %>
        </div>
        <div class="product-detail-describe">
            <div class="describe-name">
                <%
                    out.print(productName);
                %>
            </div>
            <div class="describe-price">
                <span class="describe-price-1">价格</span>¥<span class="describe-price-2"><% out.print(productPrice); %></span>
            </div>
            <div class="product-desc">
                <span class="product-desc-1"><% out.print(productDesc); %></span>
            </div>
            <div class="describe-count">
                <span class="describe-count-1">数量</span>
                <button class="count-jian">-</button>
                <input type="text" id="buy-count" class="buy-count" value="1" onchange="change()">
                <button class="count-jia">+</button>
            </div>
            <div class="describe-buy">
                <button class="buy-now" id="buy-now">立即购买</button>
            </div>
        </div>
    </div>
    <div class="container" id = "mask">
        <div class="erweima">
            <img src="" id="payCode" style="height: auto; max-width: 400px; max-height: 300px;">
        </div>
        <div id="mask-cancel">×</div>
    </div>
</body>
<script>
    var jia = document.querySelector('.count-jia');
    var jian = document.querySelector('.count-jian');
    var buy_count = document.querySelector('.buy-count');
    var allprice = document.querySelector('.describe-price-2');
    var unitprice = allprice.innerHTML;
    var buynow = document.querySelector('.buy-now')
    var container = document.querySelector('.container')
    var mask_cancle = document.getElementById('mask-cancel')
    var second_part = document.querySelector('.second_part')
    var product_detail_nav =document.querySelector('.product-detail-nav')
    function forbidden(){
        jian.disabled=true;
        jian.style.color='#cccccc';
    }
    function start(){
        jian.disabled=false;
        jian.style.color='black';
    }
    function change(){
        allprice.innerHTML=Math.floor(unitprice*buy_count.valueOf().value*100)/100;
        if (buy_count.valueOf().value<=1){
            forbidden();
        }
        if (buy_count.valueOf().value>1){
            start();
        }
    }
    if (buy_count.valueOf().value<=1){
        forbidden();
    }
    jia.addEventListener('click',function (){
        buy_count.valueOf().value++;
        allprice.innerHTML=Math.floor(unitprice*buy_count.valueOf().value*100)/100;
        start();
    })
    jian.addEventListener('click',function (){
        buy_count.valueOf().value--;
        allprice.innerHTML=Math.floor(unitprice*buy_count.valueOf().value*100)/100;
        if (buy_count.valueOf().value<=1){
            forbidden();
        }
    })
    buy_count.addEventListener('click',function (){
        allprice.innerHTML=Math.floor(unitprice*buy_count.valueOf().value*100)/100
    })
    buynow.addEventListener('click',function (){
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4 && xhr.status === 200) {
                if (xhr.responseText !== '') {
                    let payCodeImg = document.getElementById('payCode');
                    payCodeImg.src = 'data:image/png;base64,' + xhr.responseText;

                    container.style.display='block';
                    second_part.style.opacity = "0.3";
                    product_detail_nav.style.opacity = "0.3";
                }
            }
        }
        xhr.open("POST", "/MyRanch/user/getPayCode");
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.send("id=" + <% out.print(userId); %>);
    })

    mask_cancle.addEventListener('click',function (){
        container.style.display='none';
        second_part.style.opacity = "1";
        product_detail_nav.style.opacity = "1";
    })

    document.getElementById('contact-button').onclick = function () {
        window.location.href = "/MyRanch/chat.html";
    }
</script>
</html>