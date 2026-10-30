(function () {
    'use strict';

    var ME_URL = '/api/me';
    var LOGIN_URL = '/login.html';
    var LOGOUT_URL = '/logout';

    var home = document.getElementById('home');

    /**
     * 统一解析响应体：失败分类以 errorType 与 code 为准，不依赖 HTTP 状态码分支
     */
    function fetchJson(url, init) {
        return fetch(url, init).then(function (response) {
            return response.text().then(function (text) {
                try {
                    return JSON.parse(text);
                } catch (error) {
                    throw new Error('响应不是 JSON（HTTP ' + response.status + '）');
                }
            });
        });
    }

    function goToLogin() {
        // 用 replace 而不是 assign：未登录时停留在首页不是有效状态，
        // 不该把它压进历史，否则用户在登录页点后退又会回到这里再跳一次
        window.location.replace(LOGIN_URL);
    }

    function render(user) {
        document.getElementById('me-username').textContent = user.username || '-';
        document.getElementById('me-authorities').textContent = (user.authorities || []).join('、') || '（无）';
        home.hidden = false;
    }

    function checkLoginState() {
        return fetchJson(ME_URL).then(function (result) {
            if (result && result.success === true) {
                render(result.data || {});
                return;
            }
            goToLogin();
        }).catch(function () {
            // 读不到结果就按未登录处理，不把用户留在一个空白首页上
            goToLogin();
        });
    }

    function logout() {
        var button = document.getElementById('logout-button');
        button.disabled = true;
        fetchJson(LOGOUT_URL, {method: 'POST'}).then(function () {
            window.location.replace(LOGIN_URL);
        }).catch(function () {
            window.location.replace(LOGIN_URL);
        });
    }

    document.getElementById('logout-button').addEventListener('click', logout);
    checkLoginState();
}());
