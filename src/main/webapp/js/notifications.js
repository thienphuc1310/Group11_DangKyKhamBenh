(function () {
    'use strict';
    var script = document.currentScript;
    var url = script && script.dataset.notificationUrl;
    if (!url) return;
    var loading = false;
    async function refresh() {
        if (document.hidden || loading) return;
        loading = true;
        try {
            var response = await fetch(url, {headers: {'Accept': 'application/json'}, cache: 'no-store'});
            if (!response.ok || response.redirected) return;
            var result = await response.json();
            document.querySelectorAll('[data-notification-count]').forEach(function (badge) {
                badge.textContent = result.unread > 0 ? '(' + result.unread + ')' : '';
            });
        } catch (error) { /* Trang vẫn sử dụng được khi chưa tải được số thông báo. */ }
        finally { loading = false; }
    }
    refresh();
    window.setInterval(refresh, 30000);
    document.addEventListener('visibilitychange', refresh);
})();
