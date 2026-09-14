<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Đăng ký</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css"></head><body>
<div class="container"><div class="card form-card"><h2>Đăng ký tài khoản</h2>
<form action="${pageContext.request.contextPath}/patient-register" method="post">
<div class="form-group"><label>Họ và tên</label><input name="fullName" required></div>
<div class="form-group"><label>Email</label><input type="email" name="email" required></div>
<div class="form-group"><label>Mật khẩu</label><input type="password" name="password" required></div>
<div class="form-group"><label>Số điện thoại</label><input name="phone"></div>
<button class="btn">Đăng ký</button></form></div></div></body></html>