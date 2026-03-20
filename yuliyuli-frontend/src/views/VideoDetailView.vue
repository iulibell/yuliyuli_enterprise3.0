<template>
  <div class="video-detail">
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

    <!-- 视频内容区 -->
    <main class="video-main">
      <div class="video-container">
        <!-- 左侧视频播放区 -->
        <div class="video-player">
          <video :src="video.url" controls class="video-element"></video>
          <div class="video-info">
            <h1 class="video-title">{{ video.title }}</h1>
            <div class="video-stats">
              <div class="author-info">
                <el-avatar
                  :src="video.authorAvatar"
                  alt="作者"
                  @click="handleAuthorClick(video.authorName)"
                  style="cursor: pointer"
                />
                <div class="author-details">
                  <span
                    class="author-name"
                    @click="handleAuthorClick(video.authorName)"
                    style="cursor: pointer"
                    >{{ video.authorName }}</span
                  >
                  <span class="author-fans">粉丝: {{ video.fansCount || 0 }}</span>
                </div>
                <el-button
                  v-if="isFollowed"
                  type="primary"
                  style="background-color: #999; color: white; border-radius: 10px; border: none"
                  @click="handleFollow"
                  :loading="isFollowing"
                >
                  取消关注
                </el-button>
                <el-button
                  v-else
                  type="primary"
                  style="
                    background-color: deeppink;
                    color: white;
                    border-radius: 10px;
                    border: none;
                  "
                  @click="handleFollow"
                  :loading="isFollowing"
                >
                  关注
                </el-button>
              </div>
              <div class="video-actions">
                <div class="action-item" @click="handleLike" :class="{ liked: isLiked }">
                  <i class="el-icon-video-camera"></i>
                  👍
                  <span>{{ video.likeCount }}</span>
                </div>
                <div class="action-item" @click="handleCollect" :class="{ collected: isCollected }">
                  <i class="el-icon-star-on"></i>
                  ❤
                  <span>{{ video.collectionCount }}</span>
                </div>
                <div class="action-item">
                  <i class="el-icon-share"></i>
                  💌
                  <span>分享</span>
                </div>
              </div>
            </div>
            <div class="video-desc">
              <p>{{ video.intro || '该视频暂无简介' }}</p>
            </div>
          </div>

          <!-- 评论区 -->
          <div class="comment-section">
            <h3 class="comment-title">评论 ({{ commentList.length }})</h3>
            <div class="comment-input">
              <el-avatar v-if="isLoggedIn" :src="userAvatar" alt="用户" />
              <input
                type="text"
                v-model="commentContent"
                placeholder="添加评论..."
                v-if="isLoggedIn"
              />
              <div v-else class="login-prompt">
                登录后才能发表评论
                <el-button type="primary" size="small" @click="handleLogin">去登录</el-button>
              </div>
              <button class="comment-submit" v-if="isLoggedIn" @click="handleSubmitComment">
                发送
              </button>
            </div>
            <div class="comment-list">
              <div v-for="comment in commentList" :key="comment.id" class="comment-item">
                <el-avatar :src="comment.avatar || defaultAvatar" alt="用户" />
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="comment-username">{{ comment.username }}</span>
                    <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                  </div>
                  <p class="comment-text">{{ comment.content }}</p>
                  <div class="comment-actions">
                    <span class="comment-like">
                      <i class="el-icon-star-off"></i>
                      <span>{{ comment.likeCount }}</span>
                    </span>
                    <span class="comment-reply">回复</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧推荐视频区 -->
        <div class="recommend-videos">
          <h3 class="recommend-title">推荐视频</h3>
          <div class="recommend-list">
            <div
              v-for="item in hotVideoVOList"
              :key="item.id"
              class="recommend-item"
              @click="handleRecommendClick(item)"
            >
              <div class="recommend-cover">
                <img :src="item.coverUrl" alt="推荐视频封面" />
              </div>
              <div class="recommend-info">
                <h4 class="recommend-video-title">{{ item.title }}</h4>
                <div class="recommend-author">{{ item.authorName }}</div>
                <div class="recommend-stats">
                  {{ formatPlayCount(item.playCount) }} 播放 · {{ item.commentCount }} 评论
                </div>
              </div>
            </div>
          </div>
        </div>
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
                      <input type="text" v-model="loginForm.code" placeholder="请输入验证码" />
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
                <p>未注册过yuliyuli的手机号,我们将自动为你注册账号</p>
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
    id: string;
    title: string;
    url: string;
    coverUrl: string;
    duration: string;
    playCount: string;
    likeCount: string;
    commentCount: string;
    collectionCount: string;
    authorName: string;
    authorAvatar: string;
    intro: string;
    createTime: string;
    fansCount?: string;
    userId?: string;
  }

  interface Comment {
    id: string;
    content: string;
    username: string;
    avatar: string;
    createTime: string;
    likeCount: string;
  }

  interface HotRecommendVideoVO {
    id: string;
    title: string;
    url: string;
    coverUrl: string;
    intro: string;
    typeId: number;
    likeCount: number;
    collectionCount: number;
    createTime: Date;
    playCount: number;
    commentCount: number;
    authorName: string;
    authorAvatar: string;
    userId: number;
  }

  const route = useRoute();
  const router = useRouter();
  const video = ref<Video>({
    id: '',
    title: '',
    url: '',
    coverUrl: '',
    duration: '',
    playCount: '',
    likeCount: '',
    commentCount: '',
    collectionCount: '',
    authorName: '',
    authorAvatar: '',
    intro: '',
    createTime: '',
  });
  const commentList = ref<Comment[]>([]);
  const hotVideoVOList = ref<HotRecommendVideoVO[]>([]);
  const defaultAvatar = '/static/images/202304061680747832129368.jpg';

  // 评论内容
  const commentContent = ref<string>('');

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

  // 关注状态
  const isFollowing = ref<boolean>(false);
  const isFollowed = ref<boolean>(false);

  // 点赞和收藏状态
  const isLiked = ref<boolean>(false);
  const isCollected = ref<boolean>(false);

  // 初始化时从localStorage读取用户信息
  const initUserInfo = () => {
    const storedToken = localStorage.getItem('token');
    const storedUsername = localStorage.getItem('username');
    const storedAvatar = localStorage.getItem('userAvatar');

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
            const userIdValue = userData.id || userData.userId || '';
            saveLoginInfo(
              tokenValue,
              userData.username || userData.nickname || '用户',
              userData.avatar || defaultAvatar,
              userIdValue.toString()
            );

            showLoginModal.value = false;
            // 登录成功后保持在当前视频详情页
          }
        } else {
          ElMessage.error(response.data.msg || '登录失败');
        }
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
        } else {
          ElMessage.error(response.data.msg || '注册失败');
        }
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
        } else {
          ElMessage.error(response.data.msg || '验证码发送失败');
        }
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

  const handleRecommendClick = (item: HotRecommendVideoVO) => {
    router.push({
      path: `/video/${item.id}`,
      query: {
        video: JSON.stringify({
          id: item.id,
          title: item.title,
          url: item.url,
          coverUrl: item.coverUrl,
          intro: item.intro,
          typeId: item.typeId,
          likeCount: item.likeCount?.toString() || '0',
          collectionCount: item.collectionCount?.toString() || '0',
          createTime:
            typeof item.createTime === 'string'
              ? item.createTime
              : item.createTime?.toISOString() || new Date().toISOString(),
          playCount: item.playCount?.toString() || '0',
          commentCount: item.commentCount?.toString() || '0',
          authorName: item.authorName,
          authorAvatar: item.authorAvatar || defaultAvatar,
          userId: item.userId?.toString() || '0',
        }),
      },
    });
  };

  const handleAuthorClick = (authorName: string) => {
    // 从视频对象中获取userId和avatar，如果没有则使用默认值
    const userId = video.value.userId || '0';
    const avatar = video.value.authorAvatar || '';
    router.push(`/author/${userId}/${authorName}?avatar=${encodeURIComponent(avatar)}`);
  };

  const handleAvatarClick = () => {
    // 点击顶部头像跳转到个人页面
    if (isLoggedIn.value) {
      const currentUserId = localStorage.getItem('userId') || '';
      const username = localStorage.getItem('username') || '';
      const avatar = localStorage.getItem('userAvatar') || '';
      if (currentUserId) {
        router.push(`/author/${currentUserId}/${username}?avatar=${encodeURIComponent(avatar)}`);
      }
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

  const formatPlayCount = (count: string | number) => {
    if (!count) return '0';
    const num = typeof count === 'number' ? count : parseInt(count);
    if (isNaN(num)) return String(count);

    if (num >= 10000) {
      const wan = (num / 10000).toFixed(1);
      return wan.endsWith('.0') ? `${wan.substring(0, wan.length - 2)}万` : `${wan}万`;
    } else if (num >= 1000) {
      const qian = (num / 1000).toFixed(1);
      return qian.endsWith('.0') ? `${qian.substring(0, qian.length - 2)}千` : `${qian}千`;
    } else {
      return String(num);
    }
  };

  // 发送评论
  const handleSubmitComment = async () => {
    if (!commentContent.value.trim()) {
      ElMessage.warning('请输入评论内容');
      return;
    }

    try {
      const userId = localStorage.getItem('userId');
      if (!userId) {
        ElMessage.warning('请先登录');
        return;
      }

      const commentData = {
        videoId: video.value.id || video.value.url, // 使用视频ID或URL作为videoId
        userId: parseInt(userId),
        content: commentContent.value,
        avatar: localStorage.getItem('userAvatar'),
        username: localStorage.getItem('username'),
        parentId: 0, // 默认为一级评论
      };

      console.log('发送评论数据:', commentData);

      const response = await axios.post('/api/video/comment', commentData, {
        headers: {
          Authorization: `Bearer ${token.value}`,
        },
      });

      console.log('评论响应:', response);

      if (response.data && response.data.code === 200) {
        ElMessage.success('评论成功');
        // 保存评论内容
        const savedContent = commentContent.value;
        // 清空评论输入框
        commentContent.value = '';
        // 将新评论添加到列表开头，不调用fetchVideoData避免增加播放量
        const newComment: Comment = {
          id: Date.now().toString(),
          content: savedContent,
          username: localStorage.getItem('username') || '用户',
          avatar: localStorage.getItem('userAvatar') || defaultAvatar,
          createTime: new Date().toISOString(),
          likeCount: '0',
        };
        commentList.value.unshift(newComment);
      } else {
        ElMessage.error('评论失败：' + (response.data?.msg || '未知错误'));
      }
    } catch (error: any) {
      console.error('评论失败:', error);
      ElMessage.error('评论失败：' + (error.response?.data?.msg || '网络错误'));
    }
  };

  // 关注作者
  const handleFollow = async () => {
    if (!isLoggedIn.value) {
      showLoginModal.value = true;
      return;
    }

    isFollowing.value = true;

    try {
      const followUserId = video.value.userId || '0';
      const userId = localStorage.getItem('userId');

      if (!userId) {
        ElMessage.error('用户未登录');
        showLoginModal.value = true;
        return;
      }

      // 根据当前关注状态决定操作类型
      const operation = isFollowed.value ? 'unfollow' : 'follow';

      const response = await axios({
        method: 'post',
        url: '/api/info/follow',
        data: {
          operation: operation,
          userId: parseInt(userId),
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

  // 点赞视频
  const handleLike = async () => {
    if (!isLoggedIn.value) {
      showLoginModal.value = true;
      return;
    }

    try {
      const userId = localStorage.getItem('userId');
      if (!userId) {
        ElMessage.error('用户未登录');
        showLoginModal.value = true;
        return;
      }

      // 使用视频URL作为视频ID
      const videoId = video.value.url || video.value.id;

      const response = await axios.post(
        '/api/video/like',
        {
          videoId: videoId,
          userId: parseInt(userId),
        },
        {
          headers: {
            Authorization: `Bearer ${token.value}`,
          },
        }
      );

      console.log('点赞响应:', response);

      if (response.data && response.data.code === 200) {
        if (isLiked.value) {
          ElMessage.success('取消点赞成功');
          isLiked.value = false;
          video.value.likeCount = String(parseInt(video.value.likeCount || '0') - 1);
        } else {
          ElMessage.success('点赞成功');
          isLiked.value = true;
          video.value.likeCount = String(parseInt(video.value.likeCount || '0') + 1);
        }
      } else {
        ElMessage.error('操作失败：' + (response.data?.msg || '未知错误'));
      }
    } catch (error: any) {
      console.error('点赞失败:', error);
      ElMessage.error('点赞失败：' + (error.response?.data?.msg || '网络错误'));
    }
  };

  // 收藏视频
  const handleCollect = async () => {
    if (!isLoggedIn.value) {
      showLoginModal.value = true;
      return;
    }

    try {
      const userId = localStorage.getItem('userId');
      if (!userId) {
        ElMessage.error('用户未登录');
        showLoginModal.value = true;
        return;
      }

      // 使用视频URL作为视频ID
      const videoId = video.value.url || video.value.id;

      const response = await axios.post(
        '/api/video/collect',
        {
          videoId: videoId,
          userId: parseInt(userId),
        },
        {
          headers: {
            Authorization: `Bearer ${token.value}`,
          },
        }
      );

      console.log('收藏响应:', response);

      if (response.data && response.data.code === 200) {
        if (isCollected.value) {
          ElMessage.success('取消收藏成功');
          isCollected.value = false;
          video.value.collectionCount = String(parseInt(video.value.collectionCount || '0') - 1);
        } else {
          ElMessage.success('收藏成功');
          isCollected.value = true;
          video.value.collectionCount = String(parseInt(video.value.collectionCount || '0') + 1);
        }
      } else {
        ElMessage.error('操作失败：' + (response.data?.msg || '未知错误'));
      }
    } catch (error: any) {
      console.error('收藏失败:', error);
      ElMessage.error('收藏失败：' + (error.response?.data?.msg || '网络错误'));
    }
  };

  const fetchVideoData = async () => {
    try {
      // 从路由参数中获取视频信息
      const videoParam = route.query.video;
      if (videoParam) {
        video.value = JSON.parse(videoParam as string);
        console.log('从路由参数获取的视频信息:', video.value);
      }

      // 获取相关视频和评论
      const videoUrl = video.value.url || route.params.id;
      console.log('构建的videoUrl:', videoUrl);

      if (!videoUrl) {
        console.error('videoUrl为空，无法获取推荐视频');
        return;
      }

      // 获取当前用户ID和视频作者ID
      const currentUserId = localStorage.getItem('userId');
      const followUserId = video.value.userId;

      const response = await axios.get(`/api/video/clickVideo`, {
        params: {
          videoUrl: videoUrl,
          followUserId: followUserId,
          fanUserId: currentUserId,
        },
      });
      console.log('API响应:', response);

      if (response.data) {
        console.log('响应状态码:', response.data.code);
        console.log('响应数据:', response.data.data);

        if (response.data.code === 200) {
          // 乐观更新播放量
          if (video.value.playCount) {
            const currentCount = parseInt(video.value.playCount) || 0;
            video.value.playCount = (currentCount + 1).toString();
            console.log('乐观更新播放量:', video.value.playCount);
          }
          // 获取热门推荐视频
          hotVideoVOList.value =
            response.data.data.hotVideoVOList || response.data.data.videoVOList || [];
          console.log('获取到的推荐视频:', hotVideoVOList.value);
          // 获取评论列表
          commentList.value = response.data.data.commentList || [];
          // 更新关注状态
          isFollowed.value = response.data.data.isFollow === true;
        } else {
          console.error('API调用失败:', response.data.msg || '未知错误');
        }
      } else {
        console.error('API响应为空');
      }
    } catch (error) {
      console.error('获取视频数据失败:', error);
    }
  };

  onMounted(() => {
    initUserInfo();
    fetchVideoData();
  });

  watch(
    () => route.query.video,
    () => {
      fetchVideoData();
    }
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
  .video-detail {
    width: 100%;
    min-height: 100vh;
    background-color: #f5f5f5;
    display: flex;
    flex-direction: column;
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

  /* 视频内容区 */
  .video-main {
    width: 1200px;
    margin: 0 auto;
    padding: 20px 0;
    flex: 1;
  }

  .video-container {
    display: flex;
    gap: 20px;
  }

  /* 左侧视频播放区 */
  .video-player {
    flex: 1;
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .video-element {
    width: 100%;
    height: 540px;
    object-fit: cover;
    border-radius: 4px;
  }

  .video-info {
    margin-top: 20px;
  }

  .video-title {
    font-size: 20px;
    font-weight: bold;
    margin-bottom: 15px;
    color: #333;
  }

  .video-stats {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #eee;
  }

  .author-info {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .author-details {
    display: flex;
    flex-direction: column;
  }

  .author-name {
    font-size: 14px;
    font-weight: 500;
    color: #333;
  }

  .author-fans {
    font-size: 12px;
    color: #999;
  }

  .video-actions {
    display: flex;
    gap: 30px;
  }

  .action-item {
    margin-top: -10px;
    margin-right: 25px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 5px;
    cursor: pointer;
    font-size: 14px;
    color: #666;
    transition: color 0.3s ease;
  }

  .action-item:hover {
    color: #00a1d6;
  }

  .action-item.liked {
    color: #ff6b6b;
  }

  .action-item.collected {
    color: #ffd93d;
  }

  .action-item i {
    font-size: 20px;
  }

  .video-desc {
    margin-bottom: 30px;
    padding: 15px;
    background-color: #f9f9f9;
    border-radius: 4px;
    font-size: 14px;
    line-height: 1.5;
    color: #333;
  }

  /* 评论区 */
  .comment-section {
    margin-top: 30px;
  }

  .comment-title {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 20px;
    color: #333;
  }

  .comment-input {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
    padding: 10px;
    background-color: #f9f9f9;
    border-radius: 4px;
  }

  .comment-input input {
    flex: 1;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
  }

  .login-prompt {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    color: #999;
  }

  .comment-submit {
    padding: 8px 20px;
    background-color: #00a1d6;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    transition: background-color 0.3s ease;
  }

  .comment-submit:hover {
    background-color: #008fd1;
  }

  .comment-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .comment-item {
    display: flex;
    gap: 10px;
    padding-bottom: 20px;
    border-bottom: 1px solid #eee;
  }

  .comment-content {
    flex: 1;
  }

  .comment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }

  .comment-username {
    font-size: 14px;
    font-weight: 500;
    color: #333;
  }

  .comment-time {
    font-size: 12px;
    color: #999;
  }

  .comment-text {
    font-size: 14px;
    line-height: 1.5;
    color: #333;
    margin-bottom: 10px;
  }

  .comment-actions {
    display: flex;
    gap: 20px;
    font-size: 12px;
    color: #999;
  }

  .comment-like {
    display: flex;
    align-items: center;
    gap: 5px;
    cursor: pointer;
    transition: color 0.3s ease;
  }

  .comment-like:hover {
    color: #00a1d6;
  }

  .comment-reply {
    cursor: pointer;
    transition: color 0.3s ease;
  }

  .comment-reply:hover {
    color: #00a1d6;
  }

  /* 右侧推荐视频区 */
  .recommend-videos {
    width: 300px;
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    max-height: calc(145vh - 100px);
    overflow-y: auto;
  }

  .recommend-videos::-webkit-scrollbar {
    width: 6px;
  }

  .recommend-videos::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
  }

  .recommend-videos::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
  }

  .recommend-videos::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
  }

  .recommend-title {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 20px;
    color: #333;
    position: sticky;
    top: 0;
    background-color: #fff;
    padding-bottom: 10px;
    z-index: 1;
  }

  .recommend-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }

  .recommend-item {
    display: flex;
    gap: 10px;
    cursor: pointer;
    transition: transform 0.2s ease;
  }

  .recommend-item:hover {
    transform: translateX(5px);
  }

  .recommend-cover {
    position: relative;
    width: 120px;
    height: 67.5px;
    overflow: hidden;
    border-radius: 4px;
  }

  .recommend-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .recommend-duration {
    position: absolute;
    bottom: 5px;
    right: 5px;
    background-color: rgba(0, 0, 0, 0.7);
    color: white;
    font-size: 10px;
    padding: 2px 4px;
    border-radius: 2px;
  }

  .recommend-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 5px;
  }

  .recommend-video-title {
    font-size: 14px;
    font-weight: 500;
    color: #333;
    line-height: 1.3;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .recommend-author {
    font-size: 12px;
    color: #999;
  }

  .recommend-stats {
    font-size: 11px;
    color: #999;
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
    .video-main,
    .footer-content {
      width: 95%;
    }

    .video-container {
      flex-direction: column;
    }

    .recommend-videos {
      width: 100%;
      margin-top: 20px;
    }

    .recommend-list {
      flex-direction: row;
      flex-wrap: wrap;
    }

    .recommend-item {
      width: calc(33.333% - 10px);
    }
  }

  @media (max-width: 768px) {
    .video-element {
      height: 300px;
    }

    .recommend-item {
      width: calc(50% - 10px);
    }

    .video-actions {
      gap: 20px;
    }

    .action-item {
      font-size: 10px;
    }

    .action-item i {
      font-size: 16px;
    }
  }

  @media (max-width: 576px) {
    .recommend-item {
      width: 100%;
    }

    .video-stats {
      flex-direction: column;
      align-items: flex-start;
      gap: 15px;
    }

    .video-actions {
      width: 100%;
      justify-content: space-between;
    }
  }
</style>
