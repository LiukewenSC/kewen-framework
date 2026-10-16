(function () {
    'use strict';

    var STATE_PARAMETER = 'kewenLoginState';
    var OPTIONS_URL = '/api/login/options';
    var ME_URL = '/api/me';
    var LOGOUT_URL = '/logout';

    var elements = {
        notice: document.getElementById('notice'),
        loggedOut: document.getElementById('logged-out'),
        loggedIn: document.getElementById('logged-in'),
        form: document.getElementById('password-form'),
        username: document.getElementById('username'),
        password: document.getElementById('password'),
        formError: document.getElementById('form-error'),
        submit: document.getElementById('submit-button'),
        ssoList: document.getElementById('sso-list'),
        meUsername: document.getElementById('me-username'),
        meAuthorities: document.getElementById('me-authorities'),
        logout: document.getElementById('logout-button')
    };

    var passwordLoginUrl = '/login';

    /**
     * 读掉回跳带回来的结果标记并把它从地址栏剥离，避免用户收藏或前进后退时重复提示。
     * 页面是否已登录只认 /api/me 的结果，这个标记仅用于提示文案。
     */
    function consumeLoginState() {
        var params = new URLSearchParams(window.location.search);
        if (!params.has(STATE_PARAMETER)) {
            return null;
        }
        var state = params.get(STATE_PARAMETER);
        params.delete(STATE_PARAMETER);
        var query = params.toString();
        window.history.replaceState({}, document.title,
            window.location.pathname + (query ? '?' + query : '') + window.location.hash);
        return state;
    }

    /**
     * 取回 JSON 响应体。后端的状态码语义不统一（403、401、以及 HTTP 200 携带业务码 401 都有），
     * 所以这里不看 response.ok，一律解析响应体，由调用方按 success 字段判断。
     */
    function fetchJson(url, init) {
        return fetch(url, init).then(function (response) {
            return response.text().then(function (text) {
                try {
                    return JSON.parse(text);
                } catch (error) {
                    throw new Error('响应不是 JSON（HTTP ' + response.status + '）：' + text.slice(0, 120));
                }
            });
        });
    }

    function showNotice(text) {
        if (!text) {
            elements.notice.hidden = true;
            return;
        }
        elements.notice.textContent = text;
        elements.notice.hidden = false;
    }

    function showLoggedOutView() {
        elements.loggedOut.hidden = false;
        elements.loggedIn.hidden = true;
    }

    function showLoggedInView(user) {
        elements.meUsername.textContent = user.username || '-';
        elements.meAuthorities.textContent = (user.authorities || []).join('、') || '（无）';
        elements.loggedOut.hidden = true;
        elements.loggedIn.hidden = false;
    }

    /**
     * 按接口返回的条数渲染单点登录入口。按钮不写死任何一个身份源。
     */
    function renderSsoEntries(entries) {
        elements.ssoList.textContent = '';
        if (!entries || entries.length === 0) {
            var empty = document.createElement('p');
            empty.className = 'hint';
            empty.textContent = '当前没有配置单点登录身份源';
            elements.ssoList.appendChild(empty);
            return;
        }
        entries.forEach(function (entry) {
            var button = document.createElement('button');
            button.type = 'button';
            button.className = 'sso-button';
            button.textContent = entry.displayName;
            button.title = entry.protocol + ' · ' + entry.registrationId;
            // 单点登录必须由浏览器顶层导航发起：授权跳转与身份源页面都要求顶层上下文，
            // 用 fetch 走这一步拿回来的是身份源的 HTML，且会话 Cookie 的归属也会乱。
            button.addEventListener('click', function () {
                window.location.assign(entry.loginUrl);
            });
            elements.ssoList.appendChild(button);
        });
    }

    /**
     * 拉取登录方式清单，据此决定账号密码表单的字段名与单点登录按钮
     */
    function loadOptions() {
        return fetchJson(OPTIONS_URL).then(function (result) {
            if (!result || result.success !== true) {
                elements.ssoList.textContent = '';
                showNotice('登录方式读取失败：' + (result && result.message ? result.message : '未知错误'));
                return;
            }
            var data = result.data;
            if (data.password && data.password.loginUrl) {
                passwordLoginUrl = data.password.loginUrl;
            }
            if (data.password) {
                elements.username.name = data.password.usernameParameter || 'username';
                elements.password.name = data.password.passwordParameter || 'password';
            }
            renderSsoEntries(data.sso);
        }).catch(function (error) {
            elements.ssoList.textContent = '';
            showNotice('登录方式读取失败：' + error.message);
        });
    }

    function submitPasswordLogin(event) {
        event.preventDefault();
        elements.formError.hidden = true;
        elements.submit.disabled = true;

        var body = {};
        body[elements.username.name] = elements.username.value;
        body[elements.password.name] = elements.password.value;

        // Content-Type 必须精确是 application/json，一个字符都不能多：
        // 后端的 JsonLoginFilter 用 equals 比较该头，带 charset 参数会被当成表单解析，
        // 于是读不到用户名密码，最终以"凭据错误"失败，现场很难看懂。
        var headers = {'Content-Type': 'application/json'};

        fetchJson(passwordLoginUrl, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(body)
        }).then(function (result) {
            if (result && result.success === true) {
                elements.password.value = '';
                return refreshLoginState('登录成功');
            }
            elements.formError.textContent = (result && result.message)
                ? result.message : '登录失败（' + (result && result.code) + '）';
            elements.formError.hidden = false;
        }).catch(function (error) {
            elements.formError.textContent = error.message;
            elements.formError.hidden = false;
        }).then(function () {
            elements.submit.disabled = false;
        });
    }

    function logout() {
        elements.logout.disabled = true;
        fetchJson(LOGOUT_URL, {method: 'POST'}).then(function (result) {
            showNotice(result && result.success === true ? '已退出登录' : '退出失败：' + (result && result.message));
            return refreshLoginState(null);
        }).catch(function (error) {
            showNotice('退出请求失败：' + error.message);
            return refreshLoginState(null);
        }).then(function () {
            elements.logout.disabled = false;
        });
    }

    /**
     * 用 /api/me 判定登录态并渲染对应视图；它是唯一的登录态来源，
     * 因此页面刷新后仍然正确，也不依赖登录响应体是否还留在内存里
     */
    function refreshLoginState(noticeText) {
        return fetchJson(ME_URL).then(function (result) {
            if (result && result.success === true) {
                showLoggedInView(result.data || {});
                showNotice(noticeText);
                return;
            }
            showLoggedOutView();
            if (noticeText) {
                showNotice(noticeText);
            }
        }).catch(function () {
            showLoggedOutView();
        });
    }

    elements.form.addEventListener('submit', submitPasswordLogin);
    elements.logout.addEventListener('click', logout);

    var returnedState = consumeLoginState();
    if (returnedState === 'success') {
        showNotice('单点登录完成');
    } else if (returnedState === 'failure') {
        showNotice('单点登录未能完成，请重试或改用账号密码登录');
    }

    loadOptions().then(function () {
        return refreshLoginState(returnedState === 'success' ? '单点登录完成' : null);
    });
}());
