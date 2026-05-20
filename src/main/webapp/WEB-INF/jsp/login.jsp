<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>로그인 - 방탈출 예약</title>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Noto Sans KR', sans-serif;
            background: #0a0e27;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 40px 20px;
        }
        .container { max-width: 420px; width: 100%; }
        header { text-align: center; margin-bottom: 40px; }
        h1 { font-size: 2rem; font-weight: 700; color: #ffffff; margin-bottom: 8px; }
        .subtitle { font-size: 0.95rem; color: #8b93b0; }
        .form-card {
            background: #151932;
            border: 1px solid #1f2547;
            border-radius: 12px;
            padding: 36px;
        }
        .form-group { margin-bottom: 20px; }
        .form-group label { display: block; margin-bottom: 8px; color: #c5cae9; font-size: 0.9rem; font-weight: 500; }
        .form-group input {
            width: 100%;
            padding: 12px 16px;
            background: #0a0e27;
            border: 1px solid #1f2547;
            border-radius: 8px;
            font-size: 0.95rem;
            color: #ffffff;
            font-family: 'Noto Sans KR', sans-serif;
            transition: all 0.2s ease;
        }
        .form-group input:focus { outline: none; border-color: #667eea; }
        .btn {
            width: 100%;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 13px;
            border: none;
            border-radius: 8px;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            font-family: 'Noto Sans KR', sans-serif;
            transition: all 0.2s ease;
            margin-top: 8px;
        }
        .btn:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3); }
        .error-msg {
            display: none;
            background: rgba(245, 87, 108, 0.1);
            border: 1px solid #f5576c;
            color: #f5576c;
            padding: 12px 16px;
            border-radius: 8px;
            font-size: 0.9rem;
            margin-bottom: 20px;
        }
        .back-link { text-align: center; margin-top: 20px; }
        .back-link a { color: #8b93b0; font-size: 0.9rem; text-decoration: none; }
        .back-link a:hover { color: #c5cae9; }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>로그인</h1>
            <p class="subtitle">방탈출 예약 서비스</p>
        </header>
        <div class="form-card">
            <div class="error-msg" id="errorMsg">이메일 또는 비밀번호가 올바르지 않습니다.</div>
            <form id="loginForm">
                <div class="form-group">
                    <label for="email">이메일</label>
                    <input type="email" id="email" placeholder="이메일을 입력하세요" required>
                </div>
                <div class="form-group">
                    <label for="password">비밀번호</label>
                    <input type="password" id="password" placeholder="비밀번호를 입력하세요" required>
                </div>
                <button type="submit" class="btn">로그인</button>
            </form>
        </div>
        <div class="back-link"><a href="/">← 메인으로</a></div>
    </div>

    <script>
        document.getElementById('loginForm').addEventListener('submit', function(e) {
            e.preventDefault();
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            fetch('/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            })
            .then(response => {
                if (response.ok) {
                    const redirect = new URLSearchParams(window.location.search).get('redirect') || '/';
                    window.location.href = redirect;
                } else {
                    document.getElementById('errorMsg').style.display = 'block';
                }
            })
            .catch(() => {
                document.getElementById('errorMsg').style.display = 'block';
            });
        });
    </script>
</body>
</html>
