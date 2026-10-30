(function () {
    'use strict';

    var ACCOUNTS = {
        admin: {password: '123456'},
        admin2: {password: '123456'},
        subordinate: {password: '123456'},
        outsider: {password: '123456'}
    };

    /**
     * 身份 × 端点真值表。期望值只由种子里的角色、部门与授权行决定，
     * 与 sql/seed-data.sql、verify.sh、README 三处保持同一批用例。
     */
    var ROWS = [
        {
            id: '1',
            call: 'GET /currentUser',
            layer: '登录态',
            method: 'GET',
            path: '/currentUser',
            basis: '登录成功；权限在登录时从 RBAC 装载',
            expected: {
                admin: {code: 200, username: 'admin'},
                admin2: {code: 200, username: 'admin2'},
                subordinate: {code: 200, username: 'subordinate'},
                outsider: {code: 200, username: 'outsider'}
            },
            read: readCurrentUser
        },
        {
            id: '2',
            call: 'GET /test/checkMenu',
            layer: '菜单（继承父授权）',
            method: 'GET',
            path: '/test/checkMenu',
            basis: '/test 行授权 ROLE__1；方法行 auth_type=2 继承父级',
            expected: {
                admin: {code: 200},
                admin2: {code: 403},
                subordinate: {code: 403},
                outsider: {code: 403}
            },
            read: readCode
        },
        {
            id: '3',
            call: 'GET /test/checkMenuNone',
            layer: '菜单（无授权）',
            method: 'GET',
            path: '/test/checkMenuNone',
            basis: '方法行 auth_type=1 且没有任何授权行',
            expected: {
                admin: {code: 403},
                admin2: {code: 403},
                subordinate: {code: 403},
                outsider: {code: 403}
            },
            read: readCode
        },
        {
            id: '4',
            call: 'GET /test/dataRange',
            layer: '数据范围',
            method: 'GET',
            path: '/test/dataRange',
            basis: 'testrange 槽位只把 data_id=1 授权给 ROLE__1',
            expected: {
                admin: {code: 200, total: 1},
                admin2: {code: 200, total: 0},
                subordinate: {code: 200, total: 0},
                outsider: {code: 200, total: 0}
            },
            read: readRange
        },
        {
            id: '5',
            call: 'POST /test/dataEdit',
            layer: '行级数据操作',
            method: 'POST',
            path: '/test/dataEdit',
            body: {id: 1, name: '验证台只读调用'},
            basis: 'testedit 槽位只把 data_id=1 授权给 ROLE__1；方法体不写数据',
            expected: {
                admin: {code: 200},
                admin2: {code: 403},
                subordinate: {code: 403},
                outsider: {code: 403}
            },
            read: readCode
        },
        {
            id: '6',
            call: 'GET /deptInherit/rooms',
            layer: '部门继承',
            method: 'GET',
            path: '/deptInherit/rooms',
            basis: '菜单行挂 RELATIONDEPT__2（总部的严格子孙，不含总部自己）',
            expected: {
                admin: {code: 403},
                admin2: {code: 403},
                subordinate: {code: 200},
                outsider: {code: 403}
            },
            read: readCode
        }
    ];

    var state = {username: null};

    var elements = {
        console: document.getElementById('console'),
        currentUser: document.getElementById('current-user'),
        currentAuthorities: document.getElementById('current-authorities'),
        account: document.getElementById('account'),
        switchButton: document.getElementById('switch-button'),
        runButton: document.getElementById('run-button'),
        logoutButton: document.getElementById('logout-button'),
        summary: document.getElementById('summary'),
        notice: document.getElementById('notice'),
        body: document.getElementById('matrix-body')
    };

    /**
     * 统一解析响应体：失败分类以 errorType 与 code 为准，页面只按 success / code 判断，
     * 不依赖 HTTP 状态码分支。
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

    function codeOf(result) {
        if (!result) {
            return -1;
        }
        if (typeof result.code === 'number') {
            return result.code;
        }
        return result.success === true ? 200 : -1;
    }

    function readCode(result) {
        return {code: result && result.success === true ? 200 : codeOf(result)};
    }

    function readRange(result) {
        if (result && result.success === true) {
            var rows = Array.isArray(result.data) ? result.data : [];
            return {code: 200, total: rows.length};
        }
        return {code: codeOf(result)};
    }

    function readCurrentUser(result) {
        if (result && result.success === true) {
            return {code: 200, username: result.data && result.data.username};
        }
        return {code: codeOf(result)};
    }

    function describe(value) {
        if (!value) {
            return '未定义';
        }
        if (value.error) {
            return '请求失败：' + value.error;
        }
        var parts = [];
        if (value.code !== undefined) {
            parts.push('code=' + value.code);
        }
        if (value.total !== undefined) {
            parts.push(value.total + ' 行');
        }
        if (value.username !== undefined) {
            parts.push('用户=' + value.username);
        }
        return parts.join(' · ');
    }

    function matches(expected, actual) {
        if (!expected || !actual) {
            return false;
        }
        if (expected.code !== actual.code) {
            return false;
        }
        if (expected.total !== undefined && expected.total !== actual.total) {
            return false;
        }
        if (expected.username !== undefined && expected.username !== actual.username) {
            return false;
        }
        return true;
    }

    function cell(text, className) {
        var td = document.createElement('td');
        if (className) {
            td.className = className;
        }
        td.textContent = text;
        return td;
    }

    function renderRows(username) {
        elements.body.textContent = '';
        ROWS.forEach(function (row) {
            var tr = document.createElement('tr');
            tr.appendChild(cell(row.id));
            tr.appendChild(cell(row.call, 'mono'));
            tr.appendChild(cell(row.layer));

            var expectedTd = cell(describe(row.expected[username]), 'mono');
            expectedTd.id = 'expected-' + row.id;
            tr.appendChild(expectedTd);

            var actualTd = cell('-', 'mono');
            actualTd.id = 'actual-' + row.id;
            tr.appendChild(actualTd);

            var resultTd = cell('-', 'result');
            resultTd.id = 'result-' + row.id;
            tr.appendChild(resultTd);

            tr.appendChild(cell(row.basis, 'basis'));
            elements.body.appendChild(tr);
        });
    }

    function applyUser(user) {
        state.username = user.username;
        var authorities = (user.authorities || []).map(function (authority) {
            return authority && authority.authority ? authority.authority : authority;
        });
        elements.currentUser.textContent = (user.nickName || user.name || user.username)
            + '（' + user.username + '）';
        elements.currentAuthorities.textContent = authorities.join('、') || '（无权限）';
        elements.account.value = user.username;
    }

    function fetchCurrentUser() {
        return fetchJson('/currentUser').then(function (result) {
            if (!result || result.success !== true) {
                return null;
            }
            return result.data || {};
        });
    }

    function setBusy(busy) {
        elements.runButton.disabled = busy;
        elements.switchButton.disabled = busy;
        elements.logoutButton.disabled = busy;
        elements.account.disabled = busy;
    }

    function showNotice(text) {
        elements.notice.textContent = text;
        elements.notice.hidden = false;
    }

    function hideNotice() {
        elements.notice.hidden = true;
        elements.notice.textContent = '';
    }

    function updateSummary(pass, fail) {
        elements.summary.hidden = false;
        elements.summary.textContent = '通过 ' + pass + ' / 共 ' + (pass + fail);
        elements.summary.className = 'summary ' + (fail === 0 ? 'pass' : 'fail');
    }

    function updateRow(row, expected, actual, ok, result) {
        var actualText = describe(actual);
        if (result && result.message && actual.code !== 200) {
            actualText += ' · ' + result.message;
        }
        document.getElementById('actual-' + row.id).textContent = actualText;
        var resultCell = document.getElementById('result-' + row.id);
        resultCell.textContent = ok ? '通过' : '不通过';
        resultCell.className = 'result ' + (ok ? 'pass' : 'fail');
    }

    function runRow(row, username) {
        var init = {method: row.method};
        if (row.body) {
            init.headers = {'Content-Type': 'application/json'};
            init.body = JSON.stringify(row.body);
        }
        return fetchJson(row.path, init).then(function (result) {
            var expected = row.expected[username];
            var actual = row.read(result);
            var ok = matches(expected, actual);
            updateRow(row, expected, actual, ok, result);
            return ok;
        }).catch(function (error) {
            updateRow(row, row.expected[username], {error: error.message}, false, null);
            return false;
        });
    }

    function runAll() {
        var username = state.username;
        if (!username) {
            return Promise.resolve();
        }
        hideNotice();
        renderRows(username);
        setBusy(true);
        var pass = 0;
        var fail = 0;
        var chain = Promise.resolve();
        ROWS.forEach(function (row) {
            chain = chain.then(function () {
                return runRow(row, username).then(function (ok) {
                    if (ok) {
                        pass++;
                    } else {
                        fail++;
                    }
                    updateSummary(pass, fail);
                });
            });
        });
        return chain.then(function () {
            setBusy(false);
            updateSummary(pass, fail);
            if (fail === 0) {
                showNotice('当前身份 ' + username + ' 的全部用例通过');
            } else {
                showNotice('当前身份 ' + username + ' 有 ' + fail + ' 条用例与期望不一致');
            }
        });
    }

    function switchIdentity() {
        var target = elements.account.value;
        if (!ACCOUNTS[target]) {
            return;
        }
        if (target === state.username) {
            runAll();
            return;
        }
        setBusy(true);
        hideNotice();
        fetchJson('/logout', {method: 'POST'}).catch(function () {
            // 会话可能已失效；继续尝试登录即可
        }).then(function () {
            return fetchJson('/login', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({username: target, password: ACCOUNTS[target].password})
            });
        }).then(function (result) {
            if (!result || result.success !== true) {
                throw new Error((result && result.message) || '登录失败');
            }
            return fetchCurrentUser();
        }).then(function (user) {
            if (!user) {
                throw new Error('切换后读取当前用户失败');
            }
            applyUser(user);
            return runAll();
        }).catch(function (error) {
            showNotice('切换身份失败：' + error.message);
            window.location.replace('/login.html');
        });
    }

    function logout() {
        setBusy(true);
        fetchJson('/logout', {method: 'POST'}).catch(function () {
            // 会话失效也按已退出处理
        }).then(function () {
            window.location.replace('/login.html');
        });
    }

    elements.switchButton.addEventListener('click', switchIdentity);
    elements.runButton.addEventListener('click', runAll);
    elements.logoutButton.addEventListener('click', logout);

    fetchCurrentUser().then(function (user) {
        if (!user) {
            window.location.replace('/login.html');
            return null;
        }
        elements.console.hidden = false;
        applyUser(user);
        return runAll();
    }).catch(function () {
        window.location.replace('/login.html');
    });
}());
