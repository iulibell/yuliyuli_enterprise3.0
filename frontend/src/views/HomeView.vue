<template>
  <div class="home">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo">
          <h2 style="color: deeppink">Yuliyuli</h2>
        </div>
        <router-link class="header-back-link" to="/"
          ><i class="el-icon-arrow-left"></i>首页</router-link
        >
        <div class="search-box">
          <div class="search-input-container">
            <input
              type="text"
              v-model="searchQuery"
              placeholder="搜索视频、番剧、用户"
              @focus="handleSearchFocus"
              @input="handleSearchInput"
              @blur="handleSearchBlur"
              @keyup.enter="handleSearch"
            />
            <button class="search-btn" @click="handleSearch">搜索</button>
          </div>
          <!-- 搜索下拉框 -->
          <div v-if="showSearchDropdown" class="search-dropdown">
            <!-- 热门搜索 -->
            <div class="hot-search" v-if="!searchQuery.trim() && hotSearchKeywords.length > 0">
              <div class="hot-search-title">yuliyuli热榜</div>
              <div class="hot-search-list">
                <div
                  v-for="(keyword, index) in hotSearchKeywords"
                  :key="index"
                  class="hot-search-item"
                  @click="selectHotKeyword(keyword.keyword)"
                >
                  <span class="hot-search-rank">{{ index + 1 }}</span>
                  <span class="hot-search-text">{{ keyword.keyword }}</span>
                  <span v-if="keyword.hot" class="hot-tag">{{ keyword.hot }}</span>
                </div>
              </div>
            </div>
            <!-- 搜索建议 -->
            <div class="search-suggestions" v-if="searchQuery.trim() && searchSuggestions.length > 0">
              <div
                v-for="suggestion in searchSuggestions"
                :key="suggestion"
                class="search-suggestion-item"
                @click="selectSuggestion(suggestion)"
              >
                <i class="el-icon-search"></i>
                <span>{{ suggestion }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="user-icon">
          <div
            v-if="isLoggedIn"
            @click="handleAvatarClick"
            style="cursor: pointer; display: inline-block"
          >
            <el-avatar :src="userAvatar" alt="用户" />
          </div>
          <div class="user-info">
            <el-button
              v-if="isLoggedIn"
              type="default"
              style="
                background-color: #f0f0f0;
                color: #333;
                border-radius: 10px;
                border: none;
                margin-left: 10px;
                width: 80px;
              "
              @click="handleLogout"
              >退出登录</el-button
            >
            <el-button
              v-else
              type="primary"
              style="
                background-color: deeppink;
                color: white;
                border-radius: 10px;
                border: none;
                margin-left: 10px;
                width: 80px;
              "
              @click="handleLogin"
              >登录</el-button
            >
          </div>
        </div>
        <div class="user-actions">
          <button
            style="background-color: deepskyblue; color: white; border-radius: 10px; width: 80px"
            class="upload-btn"
            @click="handleUploadClick"
          >
            投稿
          </button>
        </div>
      </div>
    </header>

    <!-- 分类导航 -->
    <nav class="category-nav">
      <div class="category-content">
        <router-link to="/hot" class="category-item" :class="{ active: $route.path === '/hot' }"
          >热门</router-link
        >
        <router-link
          to="/category/1"
          class="category-item"
          :class="{ active: $route.path === '/category/1' }"
          >番剧</router-link
        >
        <router-link
          to="/category/2"
          class="category-item"
          :class="{ active: $route.path === '/category/2' }"
          >综艺</router-link
        >
        <router-link
          to="/category/3"
          class="category-item"
          :class="{ active: $route.path === '/category/3' }"
          >科技</router-link
        >
        <router-link
          to="/category/4"
          class="category-item"
          :class="{ active: $route.path === '/category/4' }"
          >生活</router-link
        >
        <router-link
          to="/category/5"
          class="category-item"
          :class="{ active: $route.path === '/category/5' }"
          >音乐</router-link
        >
        <router-link
          to="/category/6"
          class="category-item"
          :class="{ active: $route.path === '/category/6' }"
          >舞蹈</router-link
        >
        <router-link
          to="/category/7"
          class="category-item"
          :class="{ active: $route.path === '/category/7' }"
          >美食</router-link
        >
        <router-link
          to="/category/8"
          class="category-item"
          :class="{ active: $route.path === '/category/8' }"
          >汽车</router-link
        >
        <router-link
          to="/category/9"
          class="category-item"
          :class="{ active: $route.path === '/category/9' }"
          >体育</router-link
        >
        <router-link
          to="/category/10"
          class="category-item"
          :class="{ active: $route.path === '/category/10' }"
          >游戏</router-link
        >
      </div>
    </nav>

    <!-- 视频内容区 -->
    <main class="video-content">
      <div class="video-grid">
        <div v-for="video in videos" :key="video.id" class="video-card">
          <div class="video-cover" @click="handleVideoClick(video)">
            <img :src="video.cover || video.coverUrl" alt="视频封面" />
            <div class="video-stats-overlay">
              <span class="play-count"
                ><i class="el-icon-video-camera"></i>👁 {{ formatPlayCount(video.playCount) }}</span
              >
              <span class="danmaku-count"
                ><i class="el-icon-chat-dot-round"></i> 💬 {{ video.commentCount }}</span
              >
            </div>
          </div>
          <div class="video-info">
            <h3 class="video-title" @click="handleVideoClick(video)">{{ video.title }}</h3>
            <div class="video-author">
              <span @click="handleAuthorClick(video)">{{ video.authorName }}</span>
              <span class="video-time">{{ formatTime(video.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-more">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>

      <div
        v-else-if="!hasMore && videos.length > 0"
        class="no-more"
        style="color: black; margin-top: 30px"
      >
        没有更多视频了
      </div>
    </main>

    <!-- 版权footer -->
    <footer class="footer">
      <div class="footer-content">
        <p>
          &copy; 2026-Yuliyuli.有任何问题请联系我们:<span style="color: deepskyblue"
            >1913760871@qq.com</span
          >
        </p>
      </div>
    </footer>

    <!-- 登录模态框 -->
    <div v-if="showLoginModal" class="login-modal-overlay" @click="closeLoginModal">
      <div class="login-modal" @click.stop>
        <div class="login-header">
          <button class="close-btn" @click="closeLoginModal">&times;</button>
        </div>
        <div class="login-content">
          <div class="login-tabs">
            <div class="main-tabs">
              <button
                :class="['tab-btn', { active: activeMode === 'login' }]"
                @click="activeMode = 'login'"
              >
                登录
              </button>
              <button
                :class="['tab-btn', { active: activeMode === 'register' }]"
                @click="activeMode = 'register'"
              >
                注册
              </button>
            </div>
          </div>
          <div class="login-body">
            <div class="qr-code-section">
              <div class="qr-code">
                <img src="/static/images/OIP-C.webp" alt="登录二维码" />
                <p>请使用 yuliyuli 客户端扫码登录<br />或扫码下载APP</p>
              </div>
            </div>
            <div class="form-section">
              <!-- 登录表单 -->
              <div v-if="activeMode === 'login'" class="login-form">
                <div v-if="activeLoginTab === 'password'" class="login-form-content">
                  <div class="form-item">
                    <label>账号</label>
                    <input
                      type="text"
                      v-model="loginForm.account"
                      placeholder="请输入账号...."
                      maxlength="11"
                    />
                  </div>
                  <div class="form-item">
                    <label>密码</label>
                    <input
                      type="password"
                      v-model="loginForm.password"
                      placeholder="请输入密码...."
                      maxlength="16"
                    />
                  </div>
                </div>
                <div v-else class="login-form-content">
                  <div class="form-item">
                    <label>手机号</label>
                    <input
                      type="tel"
                      v-model="loginForm.phone"
                      placeholder="请输入手机号"
                      maxlength="11"
                    />
                  </div>
                  <div class="form-item">
                    <label>验证码</label>
                    <div class="code-input">
                      <input
                        type="text"
                        v-model="loginForm.code"
                        placeholder="请输入验证码"
                        maxlength="6"
                      />
                      <button class="get-code-btn" @click="getCode">获取验证码</button>
                    </div>
                  </div>
                </div>
                <div class="form-actions">
                  <button class="login-btn" @click="handleLoginSubmit">登录</button>
                </div>
              </div>

              <!-- 注册表单 -->
              <div v-else class="login-form">
                <div class="login-form-content">
                  <div class="form-item">
                    <label>手机号</label>
                    <input
                      type="tel"
                      v-model="loginForm.phone"
                      placeholder="请输入手机号"
                      maxlength="11"
                    />
                  </div>
                  <div class="form-item">
                    <label>验证码</label>
                    <div class="code-input">
                      <input
                        type="text"
                        v-model="loginForm.code"
                        placeholder="请输入验证码"
                        maxlength="6"
                      />
                      <button class="get-code-btn" @click="getCode">获取验证码</button>
                    </div>
                  </div>
                  <div class="form-item">
                    <label>密码</label>
                    <input
                      type="password"
                      v-model="loginForm.password"
                      placeholder="请设置密码"
                      maxlength="16"
                    />
                  </div>
                </div>
                <div class="form-actions">
                  <button class="register-btn" @click="handleRegister">注册</button>
                </div>
              </div>
              <div class="other-login" v-if="activeMode === 'login'">
                <p>其他登录方式</p>
                <div class="login-icons">
                  <button class="login-icon wechat"></button>
                  <h8 style="font-size: 12px; margin-left: -10px; margin-top: 10px">微信登录</h8>
                  <button class="login-icon weibo"></button>
                  <h8 style="font-size: 12px; margin-left: -10px; margin-top: 10px">微博登录</h8>
                  <button class="login-icon qq"></button>
                  <h8 style="font-size: 12px; margin-left: -10px; margin-top: 10px">QQ登录</h8>
                </div>
              </div>
              <div class="login-agreement">
                <p>未注册过yuliyuli的手机号，我们将自动为你注册账号</p>
                <p>登录或注册即代表你同意 <a href="#">用户协议</a> 和 <a href="#">隐私政策</a></p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, watch, onUnmounted } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import axios from 'axios';
  import { ElMessage } from 'element-plus';

  interface Video {
    url: string;
    id?: string;
    title: string;
    cover: string;
    coverUrl?: string;
    duration: string;
    playCount: string;
    likeCount: string;
    commentCount: string;
    collectionCount?: string;
    authorName: string;
    authorAvatar: string;
    createTime: string;
    typeId?: number;
    userId?: string;
  }

  const route = useRoute();
  const router = useRouter();
  const videos = ref<Video[]>([]);
  const currentPage = ref(1);
  const pageSize = ref(10);
  const loading = ref(false);
  const hasMore = ref(true);
  const defaultAvatar = '/static/images/202304061680747832129368.jpg';
  // 登录状态
  const token = ref<string>(localStorage.getItem('token') || '');
  const username = ref<string>(localStorage.getItem('username') || '');
  const userAvatar = ref<string>(localStorage.getItem('userAvatar') || '');
  const isLoggedIn = ref<boolean>(!!token.value);

  // 初始化时从localStorage读取用户信息
  const initUserInfo = () => {
    const storedToken = localStorage.getItem('token');
    const storedUsername = localStorage.getItem('username');
    const storedAvatar = localStorage.getItem('userAvatar');
    const storedUserId = localStorage.getItem('userId');

    console.log('从localStorage读取的userId:', storedUserId);

    if (storedToken) {
      token.value = storedToken;
      isLoggedIn.value = true;
    }
    if (storedUsername) {
      username.value = storedUsername;
    }
    if (storedAvatar) {
      userAvatar.value = storedAvatar;
    }
    if (storedUserId) {
      console.log('设置currentUserId为:', storedUserId);
    }
  };

  // 保存登录信息到localStorage
  const saveLoginInfo = (tokenValue: string, name: string, avatar: string, userId: string) => {
    localStorage.setItem('token', tokenValue);
    localStorage.setItem('username', name);
    localStorage.setItem('userAvatar', avatar);
    localStorage.setItem('userId', userId);
    token.value = tokenValue;
    username.value = name;
    userAvatar.value = avatar;
    isLoggedIn.value = !!tokenValue;
  };

  // 清除登录信息
  const clearLoginInfo = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('userAvatar');
    token.value = '';
    username.value = '';
    userAvatar.value = '';
    isLoggedIn.value = false;
  };

  const showLoginModal = ref<boolean>(false);
  const activeMode = ref<string>('login'); // login or register
  const activeLoginTab = ref<string>('password'); // password or sms
  const loginForm = ref({
    account: '',
    password: '',
    phone: '',
    code: '',
  });

  // 搜索功能
  const searchQuery = ref<string>('');
  const showSearchDropdown = ref<boolean>(false);
  const searchSuggestions = ref<string[]>([]);
  const hotSearchKeywords = ref<Array<{ keyword: string; hot?: string }>>([]);
  let searchDebounceTimer: ReturnType<typeof setTimeout> | null = null;
  let searchAbortController: AbortController | null = null;

  // 获取热门搜索（前十热门视频）
  const fetchHotSearch = async () => {
    try {
      const token = localStorage.getItem('token');
      console.log('开始获取热门视频...');

      // 构建请求配置
      const config: any = {
        params: {
          keyword: searchQuery.value.trim() || '',
        },
      };

      // 只有当token存在时才添加Authorization头
      if (token) {
        config.headers = {
          Authorization: `Bearer ${token}`,
        };
      }

      const response = await axios.get('/api/search/topTenVideo', config);
      if (
        response.data &&
        response.data.code === 200 &&
        response.data.data &&
        response.data.data.length > 0
      ) {
        // 将热门视频数据转换为搜索关键词格式
        hotSearchKeywords.value = response.data.data.map((video: any, index: number) => {
          // 从SearchVideoVO中提取标题
          const title =
            video.videoDocuments && video.videoDocuments[0]
              ? video.videoDocuments[0].title
              : video.title || `热门视频 ${index + 1}`;
          return {
            keyword: title,
            hot: index < 3 ? '热' : index < 5 ? 'NEW' : index < 8 ? '新' : undefined,
          };
        });
        console.log('热门搜索关键词:', hotSearchKeywords.value);
      } else {
        console.log('后端返回数据为空');
        hotSearchKeywords.value = [];
      }
    } catch (error: any) {
      // 401错误时不显示错误信息，只设置空数组
      if (error.response && error.response.status === 401) {
        console.log('未登录状态，无法获取热门搜索');
      } else {
        console.error('获取热门搜索失败:', error);
      }
      // 即使出现错误也不阻止用户使用搜索功能
      hotSearchKeywords.value = [];
    }
  };

  const requestSearchSuggestions = async (keyword: string) => {
    if (searchAbortController) {
      searchAbortController.abort();
    }
    searchAbortController = new AbortController();
    try {
      const response = await axios.get('/api/search/video', {
        params: { keyword },
        signal: searchAbortController.signal,
      });
      if (response.data && response.data.code === 200) {
        searchSuggestions.value = response.data.data.flatMap((item: any) => {
          if (item.title) {
            return [item.title];
          } else if (item.videoDocuments && item.videoDocuments.length > 0) {
            return item.videoDocuments.map((doc: any) => doc.title);
          }
          return [];
        });
      }
    } catch (error: any) {
      if (error?.name !== 'CanceledError' && error?.code !== 'ERR_CANCELED') {
        console.error('获取搜索建议失败:', error);
      }
    }
  };

  const handleSearchInput = () => {
    const keyword = searchQuery.value.trim();
    if (!keyword) {
      if (searchDebounceTimer) {
        clearTimeout(searchDebounceTimer);
        searchDebounceTimer = null;
      }
      if (searchAbortController) {
        searchAbortController.abort();
      }
      searchSuggestions.value = [];
      return;
    }
    if (searchDebounceTimer) {
      clearTimeout(searchDebounceTimer);
    }
    searchDebounceTimer = setTimeout(() => {
      requestSearchSuggestions(keyword);
    }, 300);
  };

  // 处理搜索 - 跳转到搜索结果页面
  const handleSearch = () => {
    if (searchQuery.value.trim()) {
      // 关闭下拉框
      showSearchDropdown.value = false;
      // 跳转到搜索结果页面
      router.push({
        path: '/search',
        query: { keyword: searchQuery.value },
      });
    }
  };

  const selectSuggestion = (suggestion: string) => {
    searchQuery.value = suggestion;
    showSearchDropdown.value = false;
    // 跳转到搜索结果页面
    router.push({
      path: '/search',
      query: { keyword: suggestion },
    });
  };

  const selectHotKeyword = (keyword: string) => {
    searchQuery.value = keyword;
    showSearchDropdown.value = false;
    // 跳转到搜索结果页面
    router.push({
      path: '/search',
      query: { keyword: keyword },
    });
  };

  const handleSearchBlur = () => {
    // 延迟关闭，以便点击下拉项时能够触发点击事件
    setTimeout(() => {
      showSearchDropdown.value = false;
    }, 200);
  };

  // 搜索框获得焦点时获取热门视频
  const handleSearchFocus = async () => {
    showSearchDropdown.value = true;
    // 如果还没有获取过热门搜索，则获取
    if (hotSearchKeywords.value.length === 0) {
      await fetchHotSearch();
    }
  };

  const handleLogin = () => {
    showLoginModal.value = true;
  };

  const handleUploadClick = () => {
    if (isLoggedIn.value) {
      router.push('/upload');
      return;
    }
    showLoginModal.value = true;
  };

  const closeLoginModal = () => {
    showLoginModal.value = false;
  };

  const phoneRegex = /^1[3-9]\d{9}$/;
  const smsCodeRegex = /^\d{6}$/;
  // 8-12位，不限制必须包含字母
  const passwordRegex = /^\S{8,12}$/;

  const handleLoginSubmit = async () => {
    const account = (loginForm.value.account || '').trim();
    const password = (loginForm.value.password || '').trim();

    // 表单验证
    if (!account) {
      ElMessage.warning('请输入手机号账号');
      return;
    }

    if (!phoneRegex.test(account)) {
      ElMessage.warning('请输入正确的11位手机号');
      return;
    }

    if (!password) {
      ElMessage.warning('请输入密码');
      return;
    }

    if (!passwordRegex.test(password)) {
      ElMessage.warning('密码需为8-12位');
      return;
    }

    try {
      // 登录逻辑实现
      console.log('登录提交:', loginForm.value);
      const response = await axios.post('/api/user/login', {
        phone: account, // 后端使用phone字段作为账号
        password: password,
      });

      console.log('登录响应:', response);

      if (response.data) {
        console.log('响应状态码:', response.data.code);
        console.log('响应消息:', response.data.msg);
        console.log('响应数据:', response.data.data);

        if (response.data.code === 200) {
          if (response.data.data) {
            // 后端返回的用户信息在data中，包含token和user
            const userData = response.data.data.user || response.data.data;
            // 保存登录信息
            const tokenValue = response.data.data.token;
            const userIdValue = userData.userId;
            console.log('登录时的userId:', userIdValue);
            saveLoginInfo(
              tokenValue,
              userData.username || userData.nickname || '用户',
              userData.avatar || defaultAvatar,
              userIdValue.toString()
            );

            showLoginModal.value = false;
            // 登录成功后跳转到视频详情页（如果有保存的视频信息）
            if (currentVideo.value) {
              router.push({
                path: `/video/${currentVideo.value.id}`,
                query: {
                  video: JSON.stringify(currentVideo.value),
                },
              });
              // 清空保存的视频信息
              currentVideo.value = null;
            } else {
              router.push('/'); // 没有视频信息则跳转到首页
            }
          } else {
            console.error('登录失败: 响应数据结构不正确');
          }
        } else {
          ElMessage.error(response.data.msg || '登录失败');
          console.error('登录失败:', response.data.msg || '未知错误');
        }
      } else {
        console.error('登录失败: 响应数据为空');
      }
    } catch (error: any) {
      console.error('登录失败:', error);
      if (error.response && error.response.data) {
        ElMessage.error(error.response.data.msg || '登录失败');
      } else {
        ElMessage.error('登录失败，请稍后重试');
      }
    }
  };

  const handleLogout = () => {
    // 退出登录逻辑
    clearLoginInfo();
    console.log('用户已退出登录');
  };

  const handleAvatarClick = () => {
    // 点击顶部头像跳转到个人页面
    console.log('点击头像，handleAvatarClick函数被调用');
    console.log('isLoggedIn.value:', isLoggedIn.value);
    if (isLoggedIn.value) {
      const currentUserId = localStorage.getItem('userId'); // 使用默认值1
      const username = localStorage.getItem('username') || '张三';
      const avatar = localStorage.getItem('userAvatar') || '';
      console.log('currentUserId:', currentUserId);
      console.log('username:', username);
      const path = `/author/${currentUserId}/${username}?avatar=${encodeURIComponent(avatar)}`;
      console.log('准备跳转到:', path);
      router.push(path);
      console.log('跳转命令已发送');
    } else {
      console.log('用户未登录，无法跳转');
    }
  };

  const handleRegister = async () => {
    const phone = (loginForm.value.phone || '').trim();
    const code = (loginForm.value.code || '').trim();
    const password = (loginForm.value.password || '').trim();

    // 表单验证
    if (!phone) {
      ElMessage.warning('请输入手机号');
      return;
    }

    if (!phoneRegex.test(phone)) {
      ElMessage.warning('请输入正确的11位手机号');
      return;
    }

    if (!code) {
      ElMessage.warning('请输入验证码');
      return;
    }

    if (!smsCodeRegex.test(code)) {
      ElMessage.warning('验证码需为6位数字');
      return;
    }

    if (!password) {
      ElMessage.warning('请输入密码');
      return;
    }

    if (!passwordRegex.test(password)) {
      ElMessage.warning('密码需为8-12位');
      return;
    }

    try {
      // 注册逻辑实现
      console.log('注册提交:', loginForm.value);
      const response = await axios.post(
        '/api/user/register',
        {
          phone: phone,
          password: password,
        },
        {
          params: {
            code: code, // code作为查询参数
          },
        }
      );

      console.log('注册响应:', response);

      if (response.data) {
        console.log('响应状态码:', response.data.code);
        console.log('响应消息:', response.data.msg);
        console.log('响应数据:', response.data.data);

        if (response.data.code === 200) {
          ElMessage.success('注册成功，请登录');
          // 清空注册表单
          loginForm.value.phone = '';
          loginForm.value.code = '';
          loginForm.value.password = '';
          // 注册成功后显示登录表单，让用户登录
          activeMode.value = 'login';
          console.log('注册成功，请登录');
        } else {
          ElMessage.error(response.data.msg || '注册失败');
          console.error('注册失败:', response.data.msg || '未知错误');
        }
      } else {
        console.error('注册失败: 响应数据为空');
      }
    } catch (error: any) {
      console.error('注册失败:', error);
      if (error.response && error.response.data) {
        ElMessage.error(error.response.data.msg || '注册失败');
      } else {
        ElMessage.error('注册失败，请稍后重试');
      }
    }
  };

  const getCode = async () => {
    const phone = (loginForm.value.phone || '').trim();

    // 表单验证
    if (!phone) {
      ElMessage.warning('请输入手机号');
      return;
    }

    if (!phoneRegex.test(phone)) {
      ElMessage.warning('请输入正确的11位手机号');
      return;
    }

    try {
      // 获取验证码逻辑
      console.log('获取验证码:', phone);
      const response = await axios.post('/api/user/getCode', {
        phone: phone,
      });

      console.log('获取验证码响应:', response);

      if (response.data) {
        console.log('响应状态码:', response.data.code);
        console.log('响应消息:', response.data.msg);
        console.log('响应数据:', response.data.data);

        if (response.data.code === 200) {
          ElMessage.success('验证码发送成功');
          console.log('验证码发送成功');
        } else {
          ElMessage.error(response.data.msg || '验证码发送失败');
          console.error('验证码发送失败:', response.data.msg || '未知错误');
        }
      } else {
        console.error('验证码发送失败: 响应数据为空');
      }
    } catch (error: any) {
      console.error('验证码发送失败:', error);
      if (error.response && error.response.data) {
        ElMessage.error(error.response.data.msg || '验证码发送失败');
      } else {
        ElMessage.error('验证码发送失败，请稍后重试');
      }
    }
  };

  // 保存当前点击的视频信息
  const currentVideo = ref<Video | null>(null);

  const handleVideoClick = (video: Video) => {
    // 直接跳转到视频详情页，不需要登录验证
    // 使用url作为视频ID
    const videoId = video.id || video.url || 'unknown';
    router.push({
      path: `/video/${encodeURIComponent(videoId)}`,
      query: {
        video: JSON.stringify(video),
      },
    });
  };

  const handleAuthorClick = (video: Video) => {
    const uid = video.userId != null && String(video.userId) !== '' ? String(video.userId) : '0';
    const name = video.authorName || '用户';
    const avatar = video.authorAvatar || '';
    router.push(
      `/author/${uid}/${encodeURIComponent(name)}?avatar=${encodeURIComponent(avatar)}`
    );
  };

  const formatTime = (time: string) => {
    if (!time) return '';
    const date = new Date(time);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (days > 0) {
      return `${days}天前`;
    } else if (hours > 0) {
      return `${hours}小时前`;
    } else if (minutes > 0) {
      return `${minutes}分钟前`;
    } else {
      return '刚刚';
    }
  };

  const formatPlayCount = (count: string) => {
    if (!count) return '0';
    const num = parseInt(count);
    if (isNaN(num)) return count;

    if (num >= 10000) {
      const wan = (num / 10000).toFixed(1);
      return wan.endsWith('.0') ? `${wan.substring(0, wan.length - 2)}万` : `${wan}万`;
    } else if (num >= 1000) {
      const qian = (num / 1000).toFixed(1);
      return qian.endsWith('.0') ? `${qian.substring(0, qian.length - 2)}千` : `${qian}千`;
    } else {
      return count;
    }
  };

  const fetchVideos = async (isLoadMore = false) => {
    if (loading.value || !hasMore.value) return;

    loading.value = true;

    try {
      if (route.path === '/hot') {
        // 从Redis缓存获取热门视频
        const response = await axios.get('/api/video/videoList', {
          params: {
            pageNum: isLoadMore ? currentPage.value + 1 : 1,
            pageSize: pageSize.value,
          },
        });

        const newVideos = response.data.data.records;
        if (isLoadMore) {
          videos.value = [...videos.value, ...newVideos];
          currentPage.value++;
        } else {
          videos.value = newVideos;
          currentPage.value = 1;
        }

        hasMore.value = newVideos.length === pageSize.value;
      } else if (route.path.startsWith('/category/')) {
        // 根据视频类型id获取视频列表
        const typeId = route.params.id;
        const response = await axios.get('/api/video/videoTypeList', {
          params: {
            typeId,
            pageNum: isLoadMore ? currentPage.value + 1 : 1,
            pageSize: pageSize.value,
          },
        });

        const newVideos = response.data.data.records;
        if (isLoadMore) {
          videos.value = [...videos.value, ...newVideos];
          currentPage.value++;
        } else {
          videos.value = newVideos;
          currentPage.value = 1;
        }

        hasMore.value = newVideos.length === pageSize.value;
      } else {
        // 动态页面
        const response = await axios.get('/api/video/videoList', {
          params: {
            pageNum: isLoadMore ? currentPage.value + 1 : 1,
            pageSize: pageSize.value,
          },
        });

        const newVideos = response.data.data.records;
        if (isLoadMore) {
          videos.value = [...videos.value, ...newVideos];
          currentPage.value++;
        } else {
          videos.value = newVideos;
          currentPage.value = 1;
        }

        hasMore.value = newVideos.length === pageSize.value;
      }
    } catch (error) {
      console.error('获取视频失败:', error);
      if (!isLoadMore) {
        videos.value = [];
      }
    } finally {
      loading.value = false;
    }
  };

  onMounted(() => {
    initUserInfo();
    fetchVideos();
    fetchHotSearch();

    // 其他页面跳回首页时可携带 showLogin=1，自动打开登录框
    if (route.query.showLogin === '1') {
      showLoginModal.value = true;
      router.replace({ path: route.path, query: {} });
    }

    // 添加滚动事件监听器
    window.addEventListener('scroll', handleScroll);
  });

  onUnmounted(() => {
    // 移除滚动事件监听器
    window.removeEventListener('scroll', handleScroll);
    if (searchDebounceTimer) {
      clearTimeout(searchDebounceTimer);
    }
    if (searchAbortController) {
      searchAbortController.abort();
    }
  });

  watch(
    () => route.path,
    () => {
      // 路由变化时重置分页状态
      currentPage.value = 1;
      hasMore.value = true;
      fetchVideos();
    }
  );

  // 滚动加载更多
  const handleScroll = () => {
    if (loading.value || !hasMore.value) return;

    const scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
    const scrollHeight = document.documentElement.scrollHeight || document.body.scrollHeight;
    const clientHeight = document.documentElement.clientHeight || window.innerHeight;

    // 当滚动到距离底部100px时加载更多
    if (scrollTop + clientHeight >= scrollHeight - 100) {
      fetchVideos(true);
    }
  };
</script>

<style scoped>
  .home {
    width: 100%;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background: url('/static/images/R-C.jpg') center / cover no-repeat fixed;
  }

  .header {
    position: sticky;
    top: 0;
    z-index: 120;
    backdrop-filter: blur(12px);
    background: rgba(255, 255, 255, 0.9);
    border-bottom: 1px solid #e8eaed;
  }

  .header-content,
  .category-content,
  .video-content,
  .footer-content {
    width: min(1280px, 95%);
    margin: 0 auto;
  }

  .header-content {
    height: 68px;
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .logo h2 {
    color: var(--bili-primary) !important;
    letter-spacing: 0.5px;
  }

  .search-box {
    flex: 1;
    min-width: 0;
    max-width: 560px;
    position: relative;
  }

  .search-input-container input {
    width: 100%;
    height: 42px;
    border: 1px solid var(--bili-border);
    border-radius: 999px;
    background: #fff;
    padding: 0 96px 0 16px;
    font-size: 14px;
    transition: border-color 0.2s;
  }

  .search-input-container input:focus {
    outline: none;
    border-color: var(--bili-primary);
  }

  .search-btn {
    position: absolute;
    right: 6px;
    top: 50%;
    transform: translateY(-50%);
    height: 32px;
    border: none;
    border-radius: 999px;
    background: var(--bili-primary);
    color: #fff;
    padding: 0 16px;
    cursor: pointer;
  }

  .search-dropdown {
    position: absolute;
    left: 0;
    right: 0;
    top: calc(100% + 8px);
    border: 1px solid var(--bili-border);
    background: #fff;
    border-radius: 14px;
    box-shadow: 0 8px 24px rgba(24, 25, 28, 0.08);
    z-index: 1000;
    max-height: 320px;
    overflow: auto;
  }

  .hot-search,
  .search-suggestions {
    padding: 12px;
  }

  .hot-search-title {
    color: var(--bili-text-main);
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 10px;
  }

  .hot-search-list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .hot-search-item,
  .search-suggestion-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px;
    border-radius: 8px;
    cursor: pointer;
  }

  .hot-search-item:hover,
  .search-suggestion-item:hover {
    background: #f6f7f8;
  }

  .category-nav {
    border-bottom: 1px solid #eceef0;
    background: #fff;
  }

  .category-content {
    display: flex;
    overflow-x: auto;
    gap: 18px;
    padding: 12px 0;
  }

  .category-item {
    color: #61666d;
    text-decoration: none;
    white-space: nowrap;
    font-size: 14px;
    padding: 6px 0;
    border-bottom: 2px solid transparent;
  }

  .category-item.active,
  .category-item:hover {
    color: var(--bili-primary);
    border-bottom-color: var(--bili-primary);
  }

  .video-content {
    flex: 1;
    padding: 24px 0 8px;
  }

  .video-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 18px;
  }

  .video-card {
    border-radius: 12px;
    overflow: hidden;
    background: #fff;
    border: 1px solid #edf0f2;
    transition: transform 0.2s, box-shadow 0.2s;
  }

  .video-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 20px rgba(24, 25, 28, 0.08);
  }

  .video-cover {
    position: relative;
    padding-top: 62.5%;
    overflow: hidden;
  }

  .video-cover img {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .video-stats-overlay {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    color: #fff;
    font-size: 12px;
    padding: 16px 10px 8px;
    display: flex;
    justify-content: space-between;
    background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.6));
  }

  .video-info {
    padding: 10px 12px 12px;
    background: #fff;
  }

  .video-title {
    font-size: 15px;
    line-height: 1.4;
    color: #18191c;
    margin-bottom: 8px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .video-author {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: #9499a0;
  }

  .video-author span:hover {
    color: var(--bili-primary);
  }

  .user-info :deep(.el-button),
  .upload-btn {
    border-radius: 999px !important;
    border: 1px solid #e3e5e7 !important;
    height: 34px;
    padding: 0 14px;
    font-size: 13px;
  }

  .upload-btn {
    background: var(--bili-primary) !important;
    color: #fff !important;
    border-color: var(--bili-primary) !important;
    margin-left: 0 !important;
  }

  .loading-more,
  .no-more {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 22px 0;
    color: #9499a0;
  }

  .footer {
    margin-top: 8px;
    border-top: 1px solid #e9ebee;
    background: #fff;
    padding: 18px 0;
  }

  .footer-content p {
    color: #9499a0;
    font-size: 13px;
  }

  /* 登录弹窗样式（之前缺失导致显示为浏览器默认样式） */
  .login-modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 3000;
    backdrop-filter: blur(4px);
  }

  .login-modal {
    width: min(960px, 92vw);
    min-height: 560px;
    background: #fff;
    border-radius: 14px;
    overflow: hidden;
    box-shadow: 0 18px 45px rgba(0, 0, 0, 0.28);
  }

  .login-header {
    display: flex;
    justify-content: flex-end;
    padding: 12px 16px;
    background: #fafbfc;
    border-bottom: 1px solid #eceff2;
  }

  .close-btn {
    width: 30px;
    height: 30px;
    border: none;
    border-radius: 50%;
    background: transparent;
    color: #7a8088;
    font-size: 20px;
    cursor: pointer;
  }

  .close-btn:hover {
    background: #eceff2;
    color: #2b2f36;
  }

  .login-content {
    display: flex;
    flex-direction: column;
    min-height: calc(560px - 54px);
  }

  .login-tabs {
    display: flex;
    justify-content: center;
    border-bottom: 1px solid #eceff2;
  }

  .main-tabs {
    display: flex;
    gap: 8px;
  }

  .tab-btn {
    padding: 14px 28px;
    border: none;
    background: transparent;
    color: #61666d;
    font-size: 15px;
    cursor: pointer;
    border-bottom: 2px solid transparent;
  }

  .tab-btn.active {
    color: var(--bili-primary);
    border-bottom-color: var(--bili-primary);
    font-weight: 600;
  }

  .login-body {
    display: flex;
    flex: 1;
    padding: 26px;
    gap: 24px;
  }

  .qr-code-section {
    width: 300px;
    flex-shrink: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    border-right: 1px solid #f0f2f4;
    padding-right: 24px;
  }

  .qr-code {
    text-align: center;
    background: #fff;
    border: 1px solid #eef1f4;
    border-radius: 10px;
    padding: 18px;
  }

  .qr-code img {
    width: 150px;
    height: 150px;
    margin-bottom: 12px;
  }

  .qr-code p {
    font-size: 12px;
    color: #61666d;
    line-height: 1.5;
  }

  .form-section {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  .login-form {
    border: 1px solid #eef1f4;
    border-radius: 10px;
    padding: 18px;
    background: #fff;
  }

  .form-item {
    margin-bottom: 14px;
  }

  .form-item label {
    display: block;
    margin-bottom: 6px;
    font-size: 14px;
    color: #18191c;
  }

  .form-item input {
    width: 100%;
    height: 38px;
    border: 1px solid #dfe3e8;
    border-radius: 8px;
    padding: 0 10px;
    font-size: 14px;
  }

  .form-item input:focus {
    outline: none;
    border-color: var(--bili-primary);
  }

  .code-input {
    display: flex;
    gap: 8px;
  }

  .get-code-btn {
    height: 38px;
    padding: 0 12px;
    border: 1px solid #dfe3e8;
    border-radius: 8px;
    background: #f7f8fa;
    cursor: pointer;
    white-space: nowrap;
  }

  .form-actions {
    display: flex;
    justify-content: center;
    margin-top: 8px;
  }

  .login-btn,
  .register-btn {
    width: 100%;
    max-width: 220px;
    height: 40px;
    border-radius: 999px;
    border: none;
    cursor: pointer;
    font-size: 14px;
  }

  .login-btn {
    background: var(--bili-primary);
    color: #fff;
  }

  .register-btn {
    border: 1px solid var(--bili-primary);
    color: var(--bili-primary);
    background: #fff;
  }

  .other-login {
    margin-top: 14px;
  }

  .other-login p {
    text-align: center;
    color: #9499a0;
    font-size: 12px;
    margin-bottom: 10px;
  }

  .login-icons {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
  }

  .login-icon {
    width: 34px;
    height: 34px;
    border-radius: 50%;
    border: none;
    cursor: pointer;
  }

  .wechat {
    background: #07c160;
  }
  .weibo {
    background: #e6162d;
  }
  .qq {
    background: #12b7f5;
  }

  .login-agreement {
    margin-top: 12px;
  }

  .login-agreement p {
    text-align: center;
    font-size: 12px;
    color: #9499a0;
    line-height: 1.6;
  }

  .login-agreement a {
    color: var(--bili-primary);
    text-decoration: none;
  }

  /* 统一登录/注册弹窗样式（B站风） */
  .login-modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.48);
    backdrop-filter: blur(4px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 5000;
  }

  .login-modal {
    width: min(980px, 92vw);
    min-height: 560px;
    border-radius: 14px;
    background: #fff;
    box-shadow: 0 20px 54px rgba(0, 0, 0, 0.24);
    overflow: hidden;
    position: relative;
  }

  .login-header {
    height: 56px;
    padding: 12px 16px;
    border-bottom: 1px solid #f0f1f3;
    background: #fff;
    display: flex;
    justify-content: flex-end;
    align-items: center;
  }

  .close-btn {
    width: 34px;
    height: 34px;
    border: none;
    border-radius: 50%;
    background: transparent;
    font-size: 30px;
    line-height: 1;
    color: #18191c;
    cursor: pointer;
  }

  .close-btn:hover {
    background: #f5f6f7;
  }

  .login-content {
    min-height: calc(560px - 56px);
    display: flex;
    flex-direction: column;
  }

  .login-tabs {
    display: flex;
    justify-content: center;
    border-bottom: 1px solid #f0f1f3;
  }

  .main-tabs {
    display: flex;
    gap: 2px;
  }

  .tab-btn {
    border: none;
    background: transparent;
    color: #61666d;
    font-size: 32px;
    font-weight: 500;
    cursor: pointer;
    padding: 16px 26px 14px;
    border-bottom: 3px solid transparent;
  }

  .tab-btn.active {
    color: #00aeec;
    border-bottom-color: #00aeec;
  }

  .login-body {
    flex: 1;
    display: flex;
    gap: 32px;
    padding: 26px 36px 20px;
  }

  .qr-code-section {
    width: 320px;
    flex-shrink: 0;
    border-right: 1px solid #f1f2f4;
    padding-right: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .qr-code {
    text-align: center;
  }

  .qr-code img {
    width: 208px;
    height: 208px;
    border: 1px solid #e3e5e7;
    border-radius: 8px;
    margin-bottom: 14px;
  }

  .qr-code p {
    color: #61666d;
    font-size: 24px;
    line-height: 1.5;
  }

  .form-section {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  .login-form {
    margin-bottom: 18px;
  }

  .form-item {
    margin-bottom: 14px;
  }

  .form-item label {
    display: block;
    color: #61666d;
    font-size: 24px;
    margin-bottom: 8px;
  }

  .form-item input {
    width: 100%;
    height: 52px;
    border: 1px solid #e3e5e7;
    border-radius: 8px;
    padding: 0 14px;
    font-size: 22px;
  }

  .form-item input:focus {
    outline: none;
    border-color: #00aeec;
  }

  .code-input {
    display: flex;
    gap: 10px;
  }

  .get-code-btn {
    height: 52px;
    border: 1px solid #e3e5e7;
    border-radius: 8px;
    background: #f6f7f8;
    color: #61666d;
    padding: 0 12px;
    font-size: 18px;
    cursor: pointer;
  }

  .form-actions {
    display: flex;
    justify-content: center;
    gap: 14px;
    margin-top: 8px;
  }

  .login-btn,
  .register-btn {
    width: 220px;
    height: 50px;
    border-radius: 10px;
    font-size: 24px;
    border: none;
    cursor: pointer;
  }

  .login-btn {
    background: #00aeec;
    color: #fff;
  }

  .register-btn {
    background: #fff;
    color: #18191c;
    border: 1px solid #dfe1e4;
  }

  .other-login p {
    text-align: center;
    color: #9499a0;
    font-size: 22px;
    margin-bottom: 12px;
  }

  .login-icons {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 14px;
  }

  .login-icon {
    width: 38px;
    height: 38px;
    border-radius: 50%;
    border: none;
  }

  .login-agreement {
    margin-top: 8px;
  }

  .login-agreement p {
    text-align: center;
    color: #9499a0;
    font-size: 21px;
    line-height: 1.6;
  }

  .login-agreement a {
    color: #00aeec;
  }
</style>
