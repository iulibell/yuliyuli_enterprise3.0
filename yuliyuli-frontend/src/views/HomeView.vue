<template>
  <div class="home">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo">
          <h2 style="color: deeppink">Yuliyuli</h2>
        </div>
        <router-link style="color: grey; text-decoration-line: none; margin-left: -80px" to="/"
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
            <div class="hot-search" v-if="hotSearchKeywords.length > 0">
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
            <div class="search-suggestions" v-if="searchSuggestions.length > 0">
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
            style="
              background-color: deepskyblue;
              color: white;
              border-radius: 10px;
              width: 80px;
              margin-left: -130px;
            "
            class="upload-btn"
            @click="router.push('/upload')"
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
                <img
                  src="C:\Users\Administrator\Desktop\yuliyuli_enterprise\yuliyuli-frontend\static\images\OIP-C.webp"
                  alt="登录二维码"
                />
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

  const handleSearchInput = async () => {
    if (searchQuery.value.trim()) {
      try {
        const response = await axios.get('/api/search/video', {
          params: { keyword: searchQuery.value },
        });
        if (response.data && response.data.code === 200) {
          console.log('搜索建议响应:', response.data.data);
          // 处理搜索建议 - 后端返回的是SearchVideoVO列表
          searchSuggestions.value = response.data.data.flatMap((item: any) => {
            if (item.title) {
              // 直接是视频对象
              return [item.title];
            } else if (item.videoDocuments && item.videoDocuments.length > 0) {
              // 包含videoDocuments数组
              return item.videoDocuments.map((doc: any) => doc.title);
            }
            return [];
          });
        }
      } catch (error) {
        console.error('获取搜索建议失败:', error);
      }
    } else {
      searchSuggestions.value = [];
    }
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

  const closeLoginModal = () => {
    showLoginModal.value = false;
  };

  const handleLoginSubmit = async () => {
    // 表单验证
    if (!loginForm.value.account || !loginForm.value.account.trim()) {
      ElMessage.warning('请输入账号');
      return;
    }

    if (loginForm.value.account.length !== 11) {
      ElMessage.warning('账号必须是11位');
      return;
    }

    if (!loginForm.value.password || !loginForm.value.password.trim()) {
      ElMessage.warning('请输入密码');
      return;
    }

    if (loginForm.value.password.length < 8 || loginForm.value.password.length > 12) {
      ElMessage.warning('密码必须是8-12位');
      return;
    }

    try {
      // 登录逻辑实现
      console.log('登录提交:', loginForm.value);
      const response = await axios.post('/api/user/login', {
        phone: loginForm.value.account, // 后端使用phone字段作为账号
        password: loginForm.value.password,
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
    // 表单验证
    if (!loginForm.value.phone || !loginForm.value.phone.trim()) {
      ElMessage.warning('请输入手机号');
      return;
    }

    if (loginForm.value.phone.length !== 11) {
      ElMessage.warning('手机号必须是11位');
      return;
    }

    if (!loginForm.value.code || !loginForm.value.code.trim()) {
      ElMessage.warning('请输入验证码');
      return;
    }

    if (!loginForm.value.password || !loginForm.value.password.trim()) {
      ElMessage.warning('请输入密码');
      return;
    }

    if (loginForm.value.password.length < 8 || loginForm.value.password.length > 12) {
      ElMessage.warning('密码必须是8-12位');
      return;
    }

    try {
      // 注册逻辑实现
      console.log('注册提交:', loginForm.value);
      const response = await axios.post(
        '/api/user/register',
        {
          phone: loginForm.value.phone,
          password: loginForm.value.password,
        },
        {
          params: {
            code: loginForm.value.code, // code作为查询参数
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
    // 表单验证
    if (!loginForm.value.phone || !loginForm.value.phone.trim()) {
      ElMessage.warning('请输入手机号');
      return;
    }

    if (loginForm.value.phone.length !== 11) {
      ElMessage.warning('手机号必须是11位');
      return;
    }

    try {
      // 获取验证码逻辑
      console.log('获取验证码:', loginForm.value.phone);
      const response = await axios.post('/api/user/getCode', {
        phone: loginForm.value.phone,
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
    const userId = video.userId || '0';
    const authorName = video.authorName;
    const avatar = video.authorAvatar || '';
    router.push(`/author/${userId}/${authorName}?avatar=${encodeURIComponent(avatar)}`);
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

    // 添加滚动事件监听器
    window.addEventListener('scroll', handleScroll);
  });

  onUnmounted(() => {
    // 移除滚动事件监听器
    window.removeEventListener('scroll', handleScroll);
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
    background-color: #f5f5f5;
    display: flex;
    flex-direction: column;
    background-image: url('C:\Users\Administrator\Desktop\yuliyuli_enterprise\yuliyuli-frontend\static\images\v2-70119c437cff012b67f2bcc385f4180e_r.jpg');
    background-size: cover;
    background-position: center;
    background-attachment: fixed;
    background-repeat: no-repeat;
  }

  /* 顶部导航栏 */
  .header {
    background-color: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    position: sticky;
    top: 0;
    z-index: 100;
  }

  .header-content {
    width: 1200px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 0;
  }

  .logo img {
    height: 40px;
  }

  .search-box {
    flex: 1;
    max-width: 400px;
    margin: 0 20px;
    position: relative;
  }

  .search-input-container {
    position: relative;
  }

  .search-box input {
    width: 100%;
    height: 36px;
    padding: 0 15px;
    border: 1px solid #ddd;
    border-radius: 18px;
    font-size: 14px;
  }

  .search-btn {
    position: absolute;
    right: 5px;
    top: 50%;
    transform: translateY(-50%);
    background-color: #00a1d6;
    color: white;
    border: none;
    border-radius: 13px;
    padding: 5px 12px;
    font-size: 12px;
    cursor: pointer;
  }

  /* 搜索下拉框 */
  .search-dropdown {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background-color: white;
    border: 1px solid #ddd;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    z-index: 1000;
    margin-top: 5px;
    max-height: 300px;
    overflow-y: auto;
  }

  /* 热门搜索 */
  .hot-search {
    padding: 10px;
    border-bottom: 1px solid #f0f0f0;
  }

  .hot-search-title {
    font-size: 14px;
    font-weight: bold;
    color: #333;
    margin-bottom: 10px;
  }

  .hot-search-list {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;
  }

  .hot-search-item {
    display: flex;
    align-items: center;
    padding: 5px 0;
    cursor: pointer;
    font-size: 13px;
  }

  .hot-search-item:hover {
    background-color: #f5f5f5;
  }

  .hot-search-rank {
    width: 20px;
    font-weight: bold;
    margin-right: 8px;
    color: #999;
  }

  .hot-search-rank:nth-child(-n + 3) {
    color: #ff4d4f;
  }

  .hot-search-text {
    flex: 1;
    color: #333;
  }

  .hot-tag {
    font-size: 10px;
    color: #ff4d4f;
    margin-left: 5px;
  }

  /* 搜索建议 */
  .search-suggestions {
    padding: 5px 0;
  }

  .search-suggestion-item {
    display: flex;
    align-items: center;
    padding: 8px 15px;
    cursor: pointer;
    font-size: 14px;
    color: #333;
  }

  .search-suggestion-item:hover {
    background-color: #f5f5f5;
  }

  .search-suggestion-item i {
    color: #999;
    margin-right: 8px;
  }

  /* 无数据提示 */
  .no-data {
    padding: 20px;
    text-align: center;
    color: #999;
    font-size: 14px;
  }

  .user-actions {
    display: flex;
    align-items: center;
  }

  .upload-btn {
    background-color: #f0f0f0;
    border: 1px solid #ddd;
    border-radius: 4px;
    padding: 6px 12px;
    margin-right: 15px;
    cursor: pointer;
    font-size: 14px;
  }

  .user-icon {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .user-icon img {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    cursor: pointer;
  }

  .user-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 5px;
  }

  .username {
    font-size: 12px;
    color: #666;
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  /* 分类导航 */
  .category-nav {
    background-color: #fff;
    border-bottom: 1px solid #ddd;
    margin-bottom: 10px;
  }

  .category-content {
    width: 1200px;
    margin: 0 auto;
    display: flex;
    overflow-x: auto;
    padding: 10px 0;
  }

  .category-item {
    margin-right: 20px;
    font-size: 14px;
    color: #666;
    text-decoration: none;
    white-space: nowrap;
    padding: 5px 0;
    position: relative;
  }

  .category-item:hover {
    color: #00a1d6;
  }

  .category-item.active {
    color: #00a1d6;
    font-weight: bold;
  }

  .category-item.active::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2px;
    background-color: #00a1d6;
  }

  /* 视频内容区 */
  .video-content {
    width: 1200px;
    margin: 0 auto;
    padding: 20px 0;
    flex: 1;
    margin-top: 20px;
    margin-bottom: 20px;
  }

  .video-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 20px;
    padding: 0 15px;
    max-width: 1400px;
    margin: 0 auto;
  }

  /* 加载状态 */
  .loading-more {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 30px 0;
    gap: 10px;
    color: #666;
  }

  .loading-spinner {
    width: 20px;
    height: 20px;
    border: 2px solid #f3f3f3;
    border-top: 2px solid #00a1d6;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  @keyframes spin {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(360deg);
    }
  }

  .no-more {
    text-align: center;
    padding: 30px 0;
    color: #999;
    font-size: 14px;
  }

  .video-card {
    width: 101%;
    height: auto;
    border-radius: 8px;
    overflow: hidden;
    transition: transform 0.2s ease;
    cursor: pointer;
    background-color: transparent;
  }

  .video-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  }

  .video-cover {
    position: relative;
    width: 100%;
    padding-top: 62.5%; /* 8:5 比例，比16:9更高 */
    overflow: hidden;
    border-radius: 8px;
  }

  .video-cover img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .video-stats-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
    padding: 12px;
    color: white;
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 15px;
  }

  .video-stats-overlay span {
    display: flex;
    align-items: center;
    gap: 5px;
  }

  .video-duration {
    position: absolute;
    bottom: 10px;
    right: 10px;
    background-color: rgba(0, 0, 0, 0.7);
    color: white;
    font-size: 12px;
    padding: 2px 5px;
    border-radius: 2px;
    z-index: 1;
  }

  .video-info {
    padding: 10px 12px;
    background-color: rgba(0, 0, 0, 0.7);
    color: white;
    height: 56px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .video-title {
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 4px;
    line-height: 1.3;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    color: white;
    cursor: pointer;
  }

  .video-author {
    display: flex;
    align-items: center;
    font-size: 12px;
    color: #ccc;
    justify-content: space-between;
  }

  .video-author span {
    cursor: pointer;
  }

  .video-author span:hover {
    color: #00a1d6;
  }

  /* 登录模态框 */
  .login-modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
    backdrop-filter: blur(5px);
  }

  .login-modal {
    width: 1000px;
    height: 600px;
    background-color: white;
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  }

  .login-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    background-color: #f5f5f5;
    border-bottom: 1px solid #ddd;
  }

  .login-header h2 {
    margin: 0;
    font-size: 18px;
    color: #333;
  }

  .close-btn {
    background: none;
    border: none;
    font-size: 24px;
    cursor: pointer;
    color: #999;
    padding: 0;
    width: 30px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    transition: all 0.3s ease;
  }

  .close-btn:hover {
    background-color: #e0e0e0;
    color: #333;
  }

  .login-content {
    height: calc(100% - 60px);
    display: flex;
    flex-direction: column;
  }

  .login-tabs {
    display: flex;
    justify-content: center;
    align-items: center;
    border-bottom: 1px solid #ddd;
    position: relative;
  }

  .main-tabs {
    display: flex;
  }

  .login-type-tabs {
    position: absolute;
    right: 20px;
    top: 50%;
    transform: translateY(-50%);
  }

  .tab-btn {
    padding: 15px 30px;
    background: none;
    border: none;
    cursor: pointer;
    font-size: 14px;
    color: #666;
    transition: all 0.3s ease;
    position: relative;
  }

  .tab-btn.active {
    color: #00a1d6;
    border-bottom: 2px solid #00a1d6;
  }

  .login-type-tabs {
    display: flex;
    flex: 1;
    justify-content: flex-end;
    align-items: center;
    padding-right: 20px;
  }

  .type-tab-btn {
    padding: 0 15px;
    background: none;
    border: none;
    cursor: pointer;
    font-size: 14px;
    color: #666;
    transition: all 0.3s ease;
    margin-left: 10px;
  }

  .type-tab-btn.active {
    color: #00a1d6;
    border-bottom: 2px solid #00a1d6;
  }

  .login-body {
    flex: 1;
    display: flex;
    padding: 30px;
    background-position: center;
    position: relative;
  }

  .qr-code-section {
    width: 300px;
    height: 300px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-right: 1px solid rgba(255, 255, 255, 0.3);
    padding-right: 30px;
    flex-shrink: 0;
    position: relative;
  }

  .form-section {
    flex: 1;
    padding-left: 30px;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  .qr-code {
    text-align: center;
    background-color: white;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  }

  .qr-code img {
    width: 150px;
    height: 150px;
    margin-bottom: 15px;
  }

  .qr-code p {
    margin: 0;
    font-size: 12px;
    color: #666;
    line-height: 1.4;
  }

  .form-section {
    flex: 1;
    padding-left: 30px;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  .login-form {
    background-color: white;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;
  }

  .form-item {
    margin-bottom: 15px;
  }

  .form-item label {
    display: block;
    font-size: 14px;
    color: #333;
    margin-bottom: 5px;
  }

  .form-item input {
    width: 100%;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
    transition: border-color 0.3s ease;
  }

  .form-item input:focus {
    outline: none;
    border-color: #00a1d6;
  }

  .code-input {
    display: flex;
    gap: 10px;
  }

  .code-input input {
    flex: 1;
  }

  .get-code-btn {
    padding: 0 15px;
    background-color: #f0f0f0;
    border: 1px solid #ddd;
    border-radius: 4px;
    cursor: pointer;
    font-size: 12px;
    transition: all 0.3s ease;
  }

  .get-code-btn:hover {
    background-color: #e0e0e0;
  }

  .form-actions {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
  }

  .form-actions button {
    width: 100%;
    max-width: 200px;
  }

  .register-btn {
    padding: 12px;
    background-color: white;
    border: 1px solid #ddd;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.3s ease;
  }

  .register-btn:hover {
    border-color: #00a1d6;
    color: #00a1d6;
  }

  .login-btn {
    padding: 12px;
    background-color: #00a1d6;
    border: none;
    border-radius: 4px;
    color: white;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.3s ease;
  }

  .login-btn:hover {
    background-color: #008fd1;
  }

  .other-login {
    margin-bottom: 20px;
  }

  .other-login p {
    margin: 0 0 10px 0;
    font-size: 12px;
    color: #666;
    text-align: center;
  }

  .login-icons {
    display: flex;
    justify-content: center;
    gap: 20px;
  }

  .login-icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    transition: all 0.3s ease;
  }

  .login-icon.wechat {
    background-color: #07c160;
    color: white;
  }

  .login-icon.weibo {
    background-color: #e6162d;
    color: white;
  }

  .login-icon.qq {
    background-color: #12b7f5;
    color: white;
  }

  .login-agreement {
    margin-left: 165px;
    font-size: 12px;
    color: #999;
    line-height: 1.4;
  }

  .login-agreement a {
    color: #00a1d6;
    text-decoration: none;
  }

  .login-agreement a:hover {
    text-decoration: underline;
  }

  /* 版权footer */
  .footer {
    background-color: rgba(255, 255, 255, 0.9);
    border-top: 1px solid #ddd;
    padding: 20px 0;
    margin-top: 20px;
  }

  .footer-content {
    width: 1200px;
    margin: 0 auto;
    text-align: center;
  }

  .footer-content p {
    font-size: 14px;
    color: #666;
    margin: 0;
  }

  /* 响应式设计 */
  @media (max-width: 1400px) {
    .video-grid {
      grid-template-columns: repeat(5, 1fr);
    }
  }

  @media (max-width: 1200px) {
    .header-content,
    .category-content,
    .video-content,
    .footer-content {
      width: 95%;
    }

    .video-grid {
      grid-template-columns: repeat(4, 1fr);
    }
  }

  @media (max-width: 992px) {
    .video-grid {
      grid-template-columns: repeat(3, 1fr);
    }
  }

  @media (max-width: 768px) {
    .video-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  @media (max-width: 576px) {
    .video-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
