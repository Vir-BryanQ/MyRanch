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
            right: -60%;
            top: 50%;
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
    </style>

</head>

<body>
<div class="container right-panel-active">
    <div class="container__form container--signup">
        <div class="form">
            <h2 class="form__title">账号注册</h2>
            <input type="text" placeholder="用户名" class="input" name="username" id="username"/>
            <input type="password" placeholder="密码" class="input" name="password" id="password"/>
            <input type="password" placeholder="确认密码" class="input" name="confirm_password" id="confirm_password"/>
            <input type="email" placeholder="邮箱" class="input" name="email" id="email"/>
            <button class="link1" id="sendemail">发送验证码</button>
            <input type="text" placeholder="验证码" class="input" name="vericode" id="vericode"/>
            <a href="\MyRanch\login.jsp" class="link">已有账号 去登陆</a>
            <input type="button" class="btn" id="register" value="注册">
        </div>
    </div>
</div>

<script>
    window.onload = function () {
        const username = document.getElementById("username");
        const password = document.getElementById("password");
        const confirm_password = document.getElementById("confirm_password");
        const email = document.getElementById("email");
        const vericode = document.getElementById("vericode");

        document.getElementById("sendemail").onclick = function () {
            if (username.value == '') {
                alert('用户名不能为空');
            } else if (password.value == '') {
                alert('密码不能为空');
            } else if (confirm_password.value == '') {
                alert('请确认您的密码');
            } else if (password.value != confirm_password.value) {
                alert('两次密码输入不一致');
            } else if (password.value.length < 6) {
                alert('输入的密码太短，长度应在6-16位之间');
            } else if (password.value.length > 16) {
                alert('输入的密码太长，长度应在6-16位之间');
            } else if (email.value == ''){
                alert('邮箱不能为空');
            } else {
                var xhr = new XMLHttpRequest();
                xhr.onreadystatechange = function () {
                    if (xhr.readyState == 4 && xhr.status == 200) {
                        alert(xhr.responseText);
                    }
                }
                xhr.open("POST", "/MyRanch/user/sendemail2", true);
                xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                xhr.send("email=" + document.getElementById("email").value);
            }
        }

        document.getElementById("register").onclick = function () {
            if (username.value == '') {
                alert('用户名不能为空');
            } else if (password.value == '') {
                alert('密码不能为空');
            } else if (confirm_password.value == '') {
                alert('请确认您的密码');
            } else if (password.value != confirm_password.value) {
                alert('两次密码输入不一致');
            } else if (password.value.length < 6) {
                alert('输入的密码太短，长度应在6-16位之间');
            } else if (password.value.length > 16) {
                alert('输入的密码太长，长度应在6-16位之间');
            } else if (email.value == ''){
                alert('邮箱不能为空');
            } else if (vericode.value == '') {
                alert('请输入验证码');
            } else {
                var xhr = new XMLHttpRequest();
                xhr.onreadystatechange = function () {
                    if (xhr.readyState == 4 && xhr.status == 200) {
                        alert(xhr.responseText);
                    }
                }
                xhr.open("POST", "/MyRanch/user/register", true);
                xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                xhr.send("username=" + document.getElementById("username").value + "&" +
                        "password=" + document.getElementById("password").value + "&" +
                        "email=" + document.getElementById("email").value + "&" +
                        "vericode=" + document.getElementById("vericode").value);
            }
        }
    }

</script>
</body>

</html>