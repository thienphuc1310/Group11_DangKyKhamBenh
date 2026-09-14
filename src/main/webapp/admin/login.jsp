<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Admin</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css"></head><body>
<div class="container"><div class="card form-card"><h2>Đăng nhập Admin</h2>
<form action="${pageContext.request.contextPath}/admin-login" method="post">
<div class="form-group"><label>Tài khoản</label><input name="username" required></div>
<div class="form-group"><label>Mật khẩu</label><input type="password" name="password" required></div>
<button class="btn">Đăng nhập</button></form></div></div></body></html>