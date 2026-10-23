document.querySelectorAll('.patient-auth .toggle-password').forEach(function (button) {
    button.setAttribute('aria-pressed', 'false');
    button.addEventListener('click', function () {
        var input = document.getElementById(button.getAttribute('aria-controls'));
        if (!input) return;
        var show = input.type === 'password';
        input.type = show ? 'text' : 'password';
        button.textContent = show ? 'Ẩn' : 'Hiện';
        button.setAttribute('aria-pressed', String(show));
        button.setAttribute('aria-label', (show ? 'Ẩn' : 'Hiện') + (input.id === 'confirm-password' ? ' mật khẩu nhập lại' : ' mật khẩu'));
    });
});
