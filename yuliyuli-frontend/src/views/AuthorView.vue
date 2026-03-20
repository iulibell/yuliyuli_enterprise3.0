<template>
  <div class="author-page">
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
        </div>
        <div class="user-icon">
          <el-avatar
            v-if="isLoggedIn"
            :src="userAvatar"
            alt="用户"
            @click="handleAvatarClick"
            style="cursor: pointer"
          />
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

    <!-- 个人主页内容区 -->
    <main class="author-content">
      <!-- 作者信息头部 -->
      <div class="author-header">
        <div class="author-avatar">
          <el-avatar :src="authorInfo.avatar || defaultAvatar" :size="100" />
        </div>
        <div class="author-info">
          <h1 class="author-name">{{ authorInfo.username }}</h1>
          <div class="author-stats">
            <span class="stat-item">关注数: {{ authorInfo.followingCount || 0 }}</span>
            <span class="stat-item">粉丝数: {{ authorInfo.fansCount || 0 }}</span>
          </div>
          <div class="author-buttons">
            <el-button
              v-if="!isCurrentUser"
              type="primary"
              :style="
                isFollowed
                  ? 'background-color: #999; color: white; border-radius: 10px; border: none; margin-right: 10px;'
                  : 'background-color: deeppink; color: white; border-radius: 10px; border: none; margin-right: 10px;'
              "
              @click="handleFollow"
              :loading="isFollowing"
            >
              {{ isFollowed ? '取消关注' : '关注' }}
            </el-button>
            <el-button
              v-if="!isCurrentUser"
              type="default"
              style="
                background-color: #f0f0f0;
                color: #333;
                border-radius: 10px;
                border: none;
                margin-right: 10px;
              "
              @click="handlePrivateMessage"
            >
              私信
            </el-button>
            <el-button
              v-if="isCurrentUser"
              type="default"
              style="
                background-color: #f0f0f0;
                color: #333;
                border-radius: 10px;
                border: none;
                margin-right: 10px;
              "
              @click="handleEditProfile"
            >
              编辑资料
            </el-button>
            <el-button
              v-if="isCurrentUser"
              type="default"
              style="background-color: #f0f0f0; color: #333; border-radius: 10px; border: none"
              @click="router.push(`/video-manager?userId=${userId}`)"
            >
              稿件管理
            </el-button>
          </div>
        </div>
      </div>

      <!-- 导航标签 -->
      <div class="author-nav">
        <div class="nav-content">
          <router-link
            :to="`/author/${userId}/${authorName}`"
            class="nav-item"
            :class="{ active: activeTab === 'home' }"
            >主页</router-link
          >
          <router-link
            :to="`/author/${userId}/${authorName}/videos`"
            class="nav-item"
            :class="{ active: activeTab === 'videos' }"
            >投稿</router-link
          >
          <router-link
            :to="`/author/${userId}/${authorName}/playlists`"
            class="nav-item"
            :class="{ active: activeTab === 'playlists' }"
            >合集和系列</router-link
          >
        </div>
      </div>

      <!-- 视频列表区 -->
      <div class="video-section">
        <!-- 合集和系列页面显示开发中 -->
        <div v-if="activeTab === 'playlists'" class="developing-section">
          <div class="developing-content">
            <i class="el-icon-s-tools" style="font-size: 48px; color: #999"></i>
            <p style="margin-top: 20px; font-size: 16px; color: #666">合集和系列功能开发中...</p>
          </div>
        </div>

        <!-- 视频列表 -->
        <template v-else>
          <div class="video-header">
            <h2 class="video-title">视频 · {{ videoList.length }}</h2>
            <div class="video-sort">
              <button class="sort-btn active">最新发布</button>
              <button class="sort-btn">最多播放</button>
              <button class="sort-btn">最多收藏</button>
            </div>
          </div>

          <div v-if="videoList.length > 0" class="video-grid">
            <div
              v-for="video in videoList"
              :key="video.id"
              class="video-card"
              @click="handleVideoClick(video)"
            >
              <div class="video-cover">
                <img :src="video.cover || video.coverUrl" alt="视频封面" />
                <div class="video-stats-overlay">
                  <span class="play-count"
                    ><i class="el-icon-video-camera"></i>👁
                    {{ formatPlayCount(video.playCount) }}</span
                  >
                  <span class="danmaku-count"
                    ><i class="el-icon-chat-dot-round"></i> 💬 {{ video.commentCount }}</span
                  >
                </div>
              </div>
              <div class="video-info">
                <h3 class="video-title">{{ video.title }}</h3>
                <div class="video-stats">
                  {{ formatPlayCount(video.playCount) }} 播放 · {{ formatTime(video.createTime) }}
                </div>
              </div>
            </div>
          </div>

          <div v-else class="no-videos">
            <p>该作者暂无任何视频</p>
          </div>
        </template>
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
                      <input type="text" v-model="loginForm.code" placeholder="请输入验证码" />
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
  import { ref, onMounted, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import axios from 'axios';
  import { ElMessage } from 'element-plus';

  interface Video {
    id?: string;
    url?: string;
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

  interface AuthorInfo {
    username: string;
    avatar: string;
    followingCount: number;
    fansCount: number;
    likeCount: number;
    playCount: number;
  }

  const route = useRoute();
  const router = useRouter();
  const userId = ref<string>(route.params.userId as string);
  const authorName = ref<string>(route.params.authorName as string);
  const avatarQuery = ref<string>((route.query.avatar as string) || '');
  const videoList = ref<Video[]>([]);
  const authorInfo = ref<AuthorInfo>({
    username: '用户',
    avatar: avatarQuery.value,
    followingCount: 0,
    fansCount: 0,
    likeCount: 0,
    playCount: 0,
  });
  const defaultAvatar = '/static/images/202304061680747832129368.jpg';

  // 登录状态
  const token = ref<string>(localStorage.getItem('token') || '');
  const username = ref<string>(localStorage.getItem('username') || '');
  const userAvatar = ref<string>(localStorage.getItem('userAvatar') || '');
  const isLoggedIn = ref<boolean>(!!token.value);

  // 搜索相关
  const searchQuery = ref<string>('');
  const showSearchDropdown = ref<boolean>(false);
  const searchSuggestions = ref<string[]>([]);
  const hotSearchKeywords = ref<Array<{ keyword: string; hot?: string }>>([]);

  // 判断是否是当前登录用户的个人页面
  const isCurrentUser = ref<boolean>(false);
  // 从localStorage中获取当前登录用户的ID
  const currentUserId = ref<string>(localStorage.getItem('userId') || '');

  // 当前激活的标签页
  const activeTab = ref<string>('home');

  // 关注状态
  const isFollowed = ref<boolean>(false);
  const isFollowing = ref<boolean>(false);

  // 处理编辑资料
  const handleEditProfile = () => {
    router.push({
      path: '/profile',
      query: {
        avatar: authorInfo.value.avatar,
        userId: userId.value,
        name: authorInfo.value.username,
      },
    });
  };

  // 处理私信按钮点击
  const handlePrivateMessage = () => {
    ElMessage.info('该功能正在开发中');
  };

  // 初始化时从localStorage读取用户信息
  const initUserInfo = () => {
    const storedUserId = localStorage.getItem('userId');
    if (storedUserId) {
      currentUserId.value = storedUserId;
    }
    const storedUsername = localStorage.getItem('username');
    if (storedUsername) {
      username.value = storedUsername;
    }
    const storedAvatar = localStorage.getItem('userAvatar');
    if (storedAvatar) {
      userAvatar.value = storedAvatar;
    }
    const storedToken = localStorage.getItem('token');
    isLoggedIn.value = !!storedToken;
  };

  // 检查是否是当前用户的个人页面
  const checkIsCurrentUser = () => {
    if (isLoggedIn.value && currentUserId.value && userId.value) {
      isCurrentUser.value = currentUserId.value === userId.value;
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
    currentUserId.value = userId;
    isLoggedIn.value = !!tokenValue;
    checkIsCurrentUser();
  };

  // 清除登录信息
  const clearLoginInfo = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('userAvatar');
    localStorage.removeItem('userId');
    token.value = '';
    username.value = '';
    userAvatar.value = '';
    currentUserId.value = '';
    isLoggedIn.value = false;
    isCurrentUser.value = false;
  };

  // 登录模态框
  const showLoginModal = ref<boolean>(false);
  const activeMode = ref<string>('login'); // login or register
  const activeLoginTab = ref<string>('password'); // password or sms
  const loginForm = ref({
    account: '',
    password: '',
    phone: '',
    code: '',
  });

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
            saveLoginInfo(
              tokenValue,
              userData.username || userData.nickname || '用户',
              userData.avatar || defaultAvatar,
              userIdValue.toString()
            );

            showLoginModal.value = false;
          }
        } else {
          ElMessage.error(response.data.msg || '登录失败');
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
    console.log('点击头像，准备跳转到个人页面');
    console.log('isLoggedIn:', isLoggedIn.value);
    console.log('currentUserId:', currentUserId.value);
    console.log('username:', username.value);
    if (isLoggedIn.value && currentUserId.value) {
      const avatar = localStorage.getItem('userAvatar') || '';
      const path = `/author/${currentUserId.value}/${username.value}?avatar=${encodeURIComponent(avatar)}`;
      console.log('准备跳转到:', path);
      router.push(path);
      console.log('跳转命令已发送');
    } else {
      console.log('未登录或没有用户ID，无法跳转');
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

  const handleVideoClick = (video: Video) => {
    // 使用url作为视频ID，与其他页面保持一致
    const videoId = video.id || video.url || 'unknown';
    router.push({
      path: `/video/${encodeURIComponent(videoId)}`,
      query: {
        video: JSON.stringify(video),
      },
    });
  };

  // 关注作者
  const handleFollow = async () => {
    if (!isLoggedIn.value) {
      showLoginModal.value = true;
      return;
    }

    isFollowing.value = true;

    try {
      const followUserId = userId.value;
      const currentUserIdValue = currentUserId.value;

      if (!currentUserIdValue) {
        ElMessage.error('用户未登录');
        showLoginModal.value = true;
        return;
      }

      const operation = isFollowed.value ? 'unfollow' : 'follow';

      const response = await axios({
        method: 'post',
        url: '/api/info/follow',
        data: {
          operation: operation,
          userId: parseInt(currentUserIdValue),
          followUserId: parseInt(followUserId),
        },
        headers: {
          Authorization: `Bearer ${token.value}`,
        },
      });

      console.log('关注响应:', response);

      if (response.data && response.data.code === 200) {
        if (isFollowed.value) {
          ElMessage.success('取消关注成功');
          isFollowed.value = false;
        } else {
          ElMessage.success('关注成功');
          isFollowed.value = true;
        }
      } else {
        const errorMessage = response.data?.msg || '未知错误';
        if (errorMessage === '已经关注该用户') {
          ElMessage.info('已经关注该用户');
        } else {
          ElMessage.error('操作失败：' + errorMessage);
        }
      }
    } catch (error: any) {
      console.error('操作失败:', error);
      ElMessage.error('操作失败：' + (error.response?.data?.msg || '网络错误'));
    } finally {
      isFollowing.value = false;
    }
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

  const fetchAuthorData = async () => {
    try {
      // 直接使用路由参数中的authorName作为用户名
      authorInfo.value.username = authorName.value;

      // 获取用户信息
      if (userId.value && userId.value !== '0') {
        const userInfoResponse = await axios.get(`/api/info/userInfo/${userId.value}`);
        console.log('获取用户信息响应:', userInfoResponse);

        if (userInfoResponse.data && userInfoResponse.data.code === 200) {
          const userData = userInfoResponse.data.data;
          authorInfo.value = {
            username: userData.username || userData.nickname || authorName.value,
            avatar: userData.avatar || defaultAvatar,
            followingCount: userData.followCount || 0,
            fansCount: userData.fansCount || 0,
            likeCount: 0,
            playCount: 0,
          };
          console.log('获取到的用户信息:', authorInfo.value);
        }
      }

      // 获取作者的视频列表
      if (userId.value && userId.value !== '0') {
        const videoResponse = await axios.get(`/api/info/authorPage/${userId.value}`);
        console.log('获取作者视频列表响应:', videoResponse);

        if (videoResponse.data && videoResponse.data.code === 200) {
          videoList.value = videoResponse.data.data || [];
          console.log('获取到的视频列表:', videoList.value);
        }
      }

      // 获取关注状态
      if (
        isLoggedIn.value &&
        currentUserId.value &&
        userId.value &&
        userId.value !== currentUserId.value
      ) {
        try {
          // 调用后端接口获取关注状态
          const response = await axios.get('/api/info/followStatus', {
            params: {
              followUserId: userId.value,
              fanUserId: currentUserId.value,
            },
            headers: {
              Authorization: `Bearer ${token.value}`,
            },
          });

          if (response.data && response.data.code === 200) {
            isFollowed.value = response.data.data || false;
          } else {
            isFollowed.value = false;
          }
        } catch (error) {
          console.error('获取关注状态失败:', error);
          isFollowed.value = false;
        }
      }
    } catch (error) {
      console.error('获取作者数据失败:', error);
      videoList.value = [];
    }
  };

  onMounted(() => {
    initUserInfo();
    checkIsCurrentUser();
    fetchAuthorData();
  });

  // 监听路由参数变化
  watch(
    () => route.params,
    newParams => {
      userId.value = newParams.userId as string;
      authorName.value = newParams.authorName as string;
      checkIsCurrentUser();
      fetchAuthorData();
    },
    { deep: true }
  );

  // 监听路由路径变化，设置当前激活的标签页
  watch(
    () => route.path,
    newPath => {
      if (newPath.includes('/videos')) {
        activeTab.value = 'videos';
      } else if (newPath.includes('/playlists')) {
        activeTab.value = 'playlists';
      } else {
        activeTab.value = 'home';
      }
    },
    { immediate: true }
  );

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
</script>

<style scoped>
  .author-page {
    width: 100%;
    min-height: 100vh;
    background-color: #f5f5f5;
    display: flex;
    flex-direction: column;
    background-image: url('/static/images/b835cbefbf6a66640a703a5f09956eff.webp');
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    background-attachment: fixed;
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
    outline: none;
  }

  .search-box input:focus {
    border-color: #00a1d6;
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
  }

  .search-suggestion-item:hover {
    background-color: #f5f5f5;
  }

  .search-suggestion-item i {
    color: #999;
    margin-right: 10px;
    font-size: 14px;
  }

  .search-suggestion-item span {
    color: #333;
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

  /* 个人主页内容区 */
  .author-content {
    width: 1200px;
    margin: 0 auto;
    padding: 20px 0;
    flex: 1;
  }

  /* 作者信息头部 */
  .author-header {
    background-color: #fff;
    border-radius: 8px;
    padding: 30px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    display: flex;
    align-items: center;
    gap: 30px;
  }

  .author-avatar {
    flex-shrink: 0;
  }

  .author-info {
    flex: 1;
  }

  .author-name {
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 15px;
    color: #333;
  }

  .author-stats {
    display: flex;
    gap: 30px;
    margin-bottom: 20px;
    font-size: 14px;
    color: #666;
  }

  .stat-item {
    display: flex;
    align-items: center;
  }

  .author-buttons {
    display: flex;
    gap: 10px;
  }

  /* 导航标签 */
  .author-nav {
    background-color: #fff;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .nav-content {
    display: flex;
    padding: 0 30px;
  }

  .nav-item {
    padding: 15px 20px;
    color: #666;
    text-decoration: none;
    font-size: 14px;
    position: relative;
    transition: color 0.3s ease;
  }

  .nav-item:hover {
    color: #00a1d6;
  }

  .nav-item.active {
    color: #00a1d6;
    font-weight: 500;
  }

  .nav-item.active::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 20px;
    right: 20px;
    height: 2px;
    background-color: #00a1d6;
  }

  /* 视频列表区 */
  .video-section {
    background-color: #fff;
    border-radius: 8px;
    padding: 30px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .video-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .video-title {
    font-size: 18px;
    font-weight: bold;
    color: #333;
    margin: 0;
  }

  .video-sort {
    display: flex;
    gap: 10px;
  }

  .sort-btn {
    padding: 5px 15px;
    background-color: #f0f0f0;
    border: 1px solid #ddd;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.3s ease;
  }

  .sort-btn:hover {
    background-color: #e0e0e0;
  }

  .sort-btn.active {
    background-color: #00a1d6;
    color: white;
    border-color: #00a1d6;
  }

  /* 视频网格 */
  .video-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    justify-content: flex-start;
  }

  .video-card {
    width: 200px;
    height: 160px;
    border-radius: 4px;
    overflow: hidden;
    transition: transform 0.2s ease;
    cursor: pointer;
    background-color: transparent;
    flex-shrink: 0;
    flex-grow: 0;
  }

  .video-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  }

  .video-cover {
    position: relative;
    width: 100%;
    padding-top: 56.25%; /* 16:9 比例 */
    overflow: hidden;
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
    padding: 10px;
    color: white;
    font-size: 12px;
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
    padding: 4px 6px;
    background-color: rgba(0, 0, 0, 0.7);
    color: white;
    height: 42px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .video-info .video-title {
    font-size: 12px;
    font-weight: 500;
    margin-bottom: 2px;
    line-height: 1.2;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    color: white;
    cursor: pointer;
  }

  .video-info .video-title:hover {
    color: #00a1d6;
  }

  .video-info .video-stats {
    font-size: 10px;
    color: #ccc;
  }

  /* 无视频提示 */
  .no-videos {
    text-align: center;
    padding: 60px 0;
    color: #999;
    font-size: 16px;
  }

  /* 开发中提示 */
  .developing-section {
    min-height: 400px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .developing-content {
    text-align: center;
    padding: 60px 0;
  }

  /* 版权footer */
  .footer {
    background-color: #fff;
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

  /* 响应式设计 */
  @media (max-width: 1200px) {
    .header-content,
    .author-content,
    .footer-content {
      width: 95%;
    }

    .video-grid {
      grid-template-columns: repeat(4, 1fr);
    }
  }

  @media (max-width: 992px) {
    .author-header {
      flex-direction: column;
      text-align: center;
    }

    .author-stats {
      justify-content: center;
    }

    .video-grid {
      grid-template-columns: repeat(3, 1fr);
    }
  }

  @media (max-width: 768px) {
    .video-grid {
      grid-template-columns: repeat(2, 1fr);
    }

    .nav-content {
      overflow-x: auto;
      white-space: nowrap;
    }
  }

  @media (max-width: 576px) {
    .video-grid {
      grid-template-columns: 1fr;
    }

    .author-stats {
      flex-wrap: wrap;
      gap: 15px;
    }
  }
</style>
