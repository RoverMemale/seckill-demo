// 全局变量
let token = null;
let userId = null;
let pollingIntervals = {};

// 显示消息提示
function showMessage(msg, type = 'info') {
    const msgDiv = document.getElementById('message');
    msgDiv.textContent = msg;
    msgDiv.className = `message ${type}`;
    msgDiv.style.display = 'block';
    setTimeout(() => {
        msgDiv.style.display = 'none';
    }, 3000);
}

// 登录
async function login() {
    const nickname = document.getElementById('nickname').value.trim();
    const password = document.getElementById('password').value.trim();

    if (!nickname || !password) {
        showMessage('请输入昵称和密码', 'error');
        return;
    }

    try {
        const response = await fetch('/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ nickname, password })
        });

        const result = await response.json();

        if (result.code === 0) {
            token = result.data.token;
            userId = extractUserIdFromToken(token);
            showMessage('登录成功！', 'success');
            document.getElementById('loginPanel').style.display = 'none';
            document.getElementById('seckillPanel').style.display = 'block';
            document.getElementById('currentUser').textContent = nickname;
            loadGoodsList();
        } else {
            showMessage(result.msg || '登录失败', 'error');
        }
    } catch (error) {
        console.error('登录错误:', error);
        showMessage('网络错误，请稍后重试', 'error');
    }
}

// 从token解析用户ID
function extractUserIdFromToken(tokenValue) {
    try {
        const payload = tokenValue.split('.')[1];
        const decoded = JSON.parse(atob(payload));
        return decoded.sub;
    } catch (e) {
        console.error('解析token失败', e);
        return null;
    }
}

// 退出登录
function logout() {
    token = null;
    userId = null;
    document.getElementById('loginPanel').style.display = 'block';
    document.getElementById('seckillPanel').style.display = 'none';
    Object.values(pollingIntervals).forEach(interval => clearInterval(interval));
    pollingIntervals = {};
    showMessage('已退出登录', 'info');
}

// 加载商品列表
async function loadGoodsList() {
    try {
        const response = await fetch('/goods/list', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        const result = await response.json();

        if (result.code === 0) {
            renderGoodsList(result.data);
        } else {
            showMessage('加载商品列表失败', 'error');
        }
    } catch (error) {
        console.error('加载商品错误:', error);
        showMessage('网络错误', 'error');
    }
}

// 渲染商品列表
function renderGoodsList(goodsList) {
    const container = document.getElementById('goodsList');
    container.innerHTML = '';

    goodsList.forEach(goods => {
        const now = new Date();
        const startDate = new Date(goods.startDate);
        const endDate = new Date(goods.endDate);
        const isSeckilling = goods.startDate && goods.endDate &&
            now >= startDate && now <= endDate;
        const hasStock = goods.stockCount > 0;

        const card = document.createElement('div');
        card.className = 'goods-card';
        card.innerHTML = `
            <div class="goods-name">${goods.goodsName}</div>
            <div class="goods-price">
                ${goods.seckillPrice ? '¥' + goods.seckillPrice : '¥' + goods.goodsPrice}
                ${goods.seckillPrice ? `<span class="original-price">¥${goods.goodsPrice}</span>` : ''}
            </div>
            <div class="seckill-info">
                ${goods.seckillPrice ? `
                    <div>🔥 秒杀库存：<span class="stock">${goods.stockCount}</span></div>
                    <div>⏰ 秒杀时间：${formatDateTime(goods.startDate)} ~ ${formatDateTime(goods.endDate)}</div>
                ` : '普通商品'}
            </div>
            ${goods.seckillPrice ? `
                <button class="seckill-btn" 
                        onclick="startSeckill(${goods.id})"
                        ${!isSeckilling || !hasStock ? 'disabled' : ''}>
                    ${!isSeckilling ? '未开始/已结束' : (!hasStock ? '已售罄' : '立即秒杀')}
                </button>
            ` : '<button disabled style="background:#95a5a6">普通商品</button>'}
        `;
        container.appendChild(card);
    });
}

// 格式化日期时间
function formatDateTime(dateStr) {
    if (!dateStr) return '未设置';
    const date = new Date(dateStr);
    return `${date.getMonth()+1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2,'0')}`;
}

// 开始秒杀
async function startSeckill(goodsId) {
    try {
        const response = await fetch(`/seckill/${goodsId}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();

        if (result.code === 0) {
            showMessage('秒杀请求已提交，正在排队中...', 'info');
            startPollingResult(goodsId);
        } else {
            showMessage(result.msg || '秒杀失败', 'error');
        }
    } catch (error) {
        console.error('秒杀错误:', error);
        showMessage('网络错误', 'error');
    }
}

// 轮询秒杀结果
function startPollingResult(goodsId) {
    if (pollingIntervals[goodsId]) {
        clearInterval(pollingIntervals[goodsId]);
    }

    let retryCount = 0;
    const maxRetries = 30;

    pollingIntervals[goodsId] = setInterval(async () => {
        retryCount++;

        try {
            const response = await fetch(`/seckill/result/${goodsId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            const result = await response.json();

            if (result.code === 0) {
                const data = result.data;
                if (data.status === 1) {
                    clearInterval(pollingIntervals[goodsId]);
                    delete pollingIntervals[goodsId];
                    showMessage(`🎉 秒杀成功！订单号：${data.orderNo}`, 'success');
                    loadGoodsList();
                } else if (data.status === -1 || retryCount >= maxRetries) {
                    clearInterval(pollingIntervals[goodsId]);
                    delete pollingIntervals[goodsId];
                    if (retryCount >= maxRetries) {
                        showMessage('秒杀处理超时，请稍后查看订单', 'error');
                    } else {
                        showMessage('秒杀失败', 'error');
                    }
                    loadGoodsList();
                }
            } else {
                if (retryCount >= maxRetries) {
                    clearInterval(pollingIntervals[goodsId]);
                    delete pollingIntervals[goodsId];
                    showMessage('秒杀失败', 'error');
                    loadGoodsList();
                }
            }
        } catch (error) {
            console.error('查询结果错误:', error);
            if (retryCount >= maxRetries) {
                clearInterval(pollingIntervals[goodsId]);
                delete pollingIntervals[goodsId];
                showMessage('查询失败，请刷新页面', 'error');
            }
        }
    }, 1000);
}

// ---------- 注册相关函数 ----------
function showRegisterModal() {
    document.getElementById('registerModal').style.display = 'block';
}

function closeRegisterModal() {
    document.getElementById('registerModal').style.display = 'none';
    document.getElementById('regNickname').value = '';
    document.getElementById('regPassword').value = '';
    document.getElementById('regConfirmPassword').value = '';
}

async function register() {
    const nickname = document.getElementById('regNickname').value.trim();
    const password = document.getElementById('regPassword').value;
    const confirmPwd = document.getElementById('regConfirmPassword').value;

    if (!nickname || !password) {
        showMessage('请填写昵称和密码', 'error');
        return;
    }

    if (password !== confirmPwd) {
        showMessage('两次输入的密码不一致', 'error');
        return;
    }

    if (password.length < 3) {
        showMessage('密码长度至少3位', 'error');
        return;
    }

    try {
        const response = await fetch('/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ nickname, password })
        });

        const result = await response.json();

        if (result.code === 0) {
            showMessage('注册成功！请登录', 'success');
            closeRegisterModal();
            document.getElementById('nickname').value = nickname;
            document.getElementById('password').value = '';
        } else {
            showMessage(result.msg || '注册失败', 'error');
        }
    } catch (error) {
        console.error('注册错误:', error);
        showMessage('网络错误', 'error');
    }
}

// 点击模态框外部关闭
window.onclick = function(event) {
    const modal = document.getElementById('registerModal');
    if (event.target === modal) {
        closeRegisterModal();
    }
}