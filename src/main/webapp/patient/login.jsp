<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Đăng nhập bệnh nhân</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css"></head><body>
<div class="container"><div class="card form-card"><h2>Đăng nhập bệnh nhân</h2>
<p>Vui lòng đăng nhập trước khi đăng ký lịch khám.</p><br>
<form action="${pageContext.request.contextPath}/patient-login" method="post">
<div class="form-group"><label>Email</label><input type="email" name="email" required></div>
<div class="form-group"><label>Mật khẩu</label><input type="password" name="password" required></div>
<button class="btn">Đăng nhập</button> <a class="btn btn-outline" href="register.jsp">Đăng ký</a>
</form></div></div></body></html>