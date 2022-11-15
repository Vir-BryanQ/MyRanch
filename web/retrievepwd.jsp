<%@ page contentType="text/html; charset=UTF-8" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MyRanch</title>
    <style>
        :root {
            --white: #e9e9e9;
            --gray: #333;
            --blue: #0367a6;
            --lightblue: #008997;
            --button-radius: 0.7rem;
            --max-width: 758px;
            --max-height: 600px;
            font-size: 16px;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, "Open Sans", "Helvetica Neue", sans-serif;
        }

        body {
            align-items: center;
            background-color: var(--white);
            background-image: linear-gradient(to right, #fbc2eb, #a6c1ee);
            background-attachment: fixed;
            background-position: center;
            background-repeat: no-repeat;
            background-size: cover;
            display: grid;
            height: 100vh;
            place-items: center;
        }

        .form__title {
            font-weight: 300;
            margin: 0;
            margin-bottom: 1.25rem;
        }

        .link {
            color: var(--gray);
            font-size: 0.9rem;
            margin: 1.5rem 0;
            text-decoration: none;
        }

        .link1 {
            position: absolute;
            font-size: 0.9rem;
            right: -66%;
            top: 25%;
        }

        .container {
            background-color: var(--white);
            border-radius: var(--button-radius);
            box-shadow: 0 0.9rem 1.7rem rgba(0, 0, 0, 0.25), 0 0.7rem 0.7rem rgba(0, 0, 0, 0.22);
            height: var(--max-height);
            max-width: var(--max-width);
            overflow: hidden;
            position: relative;
            width: 100%;
        }

        .container__form {
            height: 100%;
            position: absolute;
            top: 0;
            transition: all 0.6s ease-in-out;
        }

        .container--signup {
            left: 0;
            opacity: 0;
            width: 32%;
            z-index: 1;
        }

        .container.right-panel-active .container--signup {
            animation: show 0.6s;
            opacity: 1;
            transform: translateX(100%);
            z-index: 5;
        }

        .btn {
            background-color: var(--blue);
            background-image: linear-gradient(90deg, var(--blue) 0%, var(--lightblue) 74%);
            border-radius: 20px;
            border: 1px solid var(--blue);
            color: var(--white);
            cursor: pointer;
            font-size: 0.8rem;
            font-weight: bold;
            letter-spacing: 0.1rem;
            padding: 0.9rem 4rem;
            text-transform: uppercase;
        }

        .form>.btn {
            margin-top: 1.5rem;
        }

        .btn:active {
            transform: scale(0.95);
        }

        .btn:focus {
            outline: none;
        }

        .form {
            background-color: var(--white);
            display: flex;
            align-items: center;
            justify-content: center;
            flex-direction: column;
            padding: 0 3rem;
            height: 100%;
            text-align: center;
        }

        .input {
            background-color: #fff;
            border: none;
            padding: 0.9rem 0.9rem;
            margin: 0.5rem 0;
            width: 200%;
        }
        .iframe {
            display: none;
        }
    </style>

</head>

<body>
<div class="container right-panel-active">
    <div class="container__form container--signup">
        <form action="/MyRanch/user/retrievepasswd" method="post" class="form" id="form1">
            <h2 class="form__title">找回密码</h2>
            <input type="email" placeholder="邮箱" class="input" name="email" id="email"/>
            <input type="submit" class="link1" value="发送验证码" name="sendemail" formtarget="iframe" onclick="return ClickHandler();"/>
            <input type="text" placeholder="验证码" class="input" name="vericode" id="vericode"/>
            <input type="password" placeholder="重置密码" class="input" name="password" id="password"/>
            <input type="password" placeholder="确认密码" class="input" name="confirm_password" id="confirm_password"/>
            <a href="\MyRanch\login.jsp" class="link">返回登录页面</a>
            <input type="submit" class="btn" value="确认" onclick="return ClickHandler1();" formtarget="iframe"/>
        </form>
    </div>
    <iframe name="iframe" class="iframe"></iframe>
</div>

<script>
    // <%--const fistForm = document.getElementById("form1");
            // const secondForm = document.getElementById("form2"); --%>
    const password = document.getElementById("password");
    const confirm_password = document.getElementById("confirm_password");
    const email = document.getElementById("email");
    const vericode = document.getElementById("vericode");

    // <%-- fistForm.addEventListener("submit", (e) => e.preventDefault());
            //  secondForm.addEventListener("submit", (e) => e.preventDefault()); --%>

    function ClickHandler() {
        if (email.value == ''){
            alert('邮箱不能为空');
            return false;
        } else {
            return true;
        }
    }

    function ClickHandler1() {
        if (password.value == '') {
            alert('密码不能为空');
            return false;
        } else if (confirm_password.value == '') {
            alert('请确认您的密码');
            return false;
        } else if (password.value != confirm_password.value) {
            alert('两次密码输入不一致');
            return false;
        } else if (password.value.length < 6) {
            alert('输入的密码太短，长度应在6-16位之间');
            return false;
        } else if (password.value.length > 16) {
            alert('输入的密码太长，长度应在6-16位之间');
            return false;
        } else if (email.value == '') {
            alert('邮箱不能为空');
            return false;
        } else if (vericode.value == '') {
            alert('请输入验证码');
            return false;
        } else {
            return true;
        }
    }
    <%
              Boolean emailExist = (Boolean) request.getAttribute("emailExist");
              if (emailExist != null) {
                if (emailExist) {
                  out.print("alert('验证码已发送', 'success', 10000)");
                } else {
                  out.print("alert('无效邮箱', 'error', 10000)");
                }
              }

              Boolean vericodeIsRight = (Boolean) request.getAttribute("vericodeIsRight");
              if (vericodeIsRight != null && !vericodeIsRight) {
                  out.print("alert('验证码错误')");
              }

              Boolean retrieveSuccess = (Boolean) request.getAttribute("retrieveSuccess");
                if (retrieveSuccess != null && !retrieveSuccess) {
                    out.print("alert('验证码错误')");
                }
            %>
</script>
</body>

</html>