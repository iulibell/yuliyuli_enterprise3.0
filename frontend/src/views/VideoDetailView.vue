<template>
  <div class="video-detail">
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
            style="background-color: deepskyblue; color: white; border-radius: 10px; width: 80px"
            class="upload-btn"
            @click="handleUploadClick"
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
                  class="follow-btn"
                  type="primary"
                  style="background-color: #999; color: white; border-radius: 10px; border: none"
                  @click="handleFollow"
                  :loading="isFollowing"
                >
                  取消关注
                </el-button>
                <el-button
                  v-else
                  class="follow-btn"
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
              <div v-if="isLoggedIn && replyTargetCommentId" class="replying-tip">
                正在回复 {{ replyTargetUsername }}
                <button type="button" class="replying-cancel" @click="cancelReply">取消</button>
              </div>
              <input
                type="text"
                v-model="commentContent"
                :placeholder="replyTargetCommentId ? `回复 ${replyTargetUsername}...` : '添加评论...'"
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
              <div v-for="comment in groupedComments" :key="comment.id" class="comment-thread">
                <div
                  class="comment-item"
                  :class="{ 'is-reply': comment.parentId && Number(comment.parentId) !== 0 }"
                >
                  <el-avatar
                    :src="comment.avatar || defaultAvatar"
                    alt="用户"
                    :size="comment.parentId && Number(comment.parentId) !== 0 ? 32 : 40"
                    class="comment-avatar"
                  />
                  <div class="comment-main">
                    <div class="comment-header">
                      <span class="comment-username">{{ comment.username }}</span>
                      <span class="comment-dot">·</span>
                      <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                    </div>
                    <div class="comment-content">
                      <p class="comment-text">{{ comment.content }}</p>
                    </div>
                    <div class="comment-actions">
                      <span
                        class="comment-like"
                        :class="{ active: comment.liked }"
                        @click="handleCommentLike(comment)"
                      >
                        <i class="el-icon-star-off"></i>
                        <span>{{ comment.likeCount || 0 }}</span>
                      </span>
                      <span class="comment-reply" @click="handleReply(comment)">回复</span>
                    </div>
                  </div>
                </div>

                <div v-if="comment.replies.length > 0" class="comment-replies">
                  <div
                    v-for="reply in comment.replies"
                    :key="reply.id"
                    class="comment-item is-reply"
                  >
                    <el-avatar
                      :src="reply.avatar || defaultAvatar"
                      alt="用户"
                      :size="32"
                      class="comment-avatar"
                    />
                    <div class="comment-main">
                      <div class="comment-header">
                        <span class="comment-username">{{ reply.username }}</span>
                        <span class="comment-dot">·</span>
                        <span class="comment-time">{{ formatTime(reply.createTime) }}</span>
                      </div>
                      <div class="comment-content">
                        <p class="comment-reply-to" v-if="reply.parentId && reply.parentId !== 0">
                          回复 {{ getParentCommentName(reply.parentId) }}：
                        </p>
                        <p class="comment-text">{{ reply.content }}</p>
                      </div>
                      <div class="comment-actions">
                        <span
                          class="comment-like"
                          :class="{ active: reply.liked }"
                          @click="handleCommentLike(reply)"
                        >
                          <i class="el-icon-star-off"></i>
                          <span>{{ reply.likeCount || 0 }}</span>
                        </span>
                        <span class="comment-reply" @click="handleReply(reply)">回复</span>
                      </div>
                    </div>
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
  import { computed, ref, onMounted, onUnmounted, watch } from 'vue';
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
    userId?: string;
    parentId?: number;
    liked?: boolean;
    replies?: Comment[];
    depth?: number;
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
  const groupedComments = computed(() => {
    const byId = new Map<number, Comment>();
    const repliesByRoot = new Map<number, Comment[]>();
    const roots: Comment[] = [];

    commentList.value.forEach(item => {
      const id = Number(item.id);
      if (id) {
        byId.set(id, item);
      }
    });

    const resolveRootAndDepth = (item: Comment) => {
      let currentParentId = Number(item.parentId || 0);
      let rootId = 0;
      let depth = 1;
      const visited = new Set<number>();
      while (currentParentId && !visited.has(currentParentId)) {
        visited.add(currentParentId);
        const parent = byId.get(currentParentId);
        if (!parent) {
          // 当前分页里拿不到父评论时，不丢弃该回复，作为独立线程显示
          rootId = Number(item.id);
          depth = 1;
          break;
        }
        depth += 1;
        const parentParentId = Number(parent.parentId || 0);
        if (!parentParentId) {
          rootId = Number(parent.id);
          break;
        }
        currentParentId = parentParentId;
      }
      return { rootId, depth };
    };

    for (const item of commentList.value) {
      const parentId = Number(item.parentId || 0);
      if (!parentId) {
        roots.push({ ...item, replies: [], depth: 1 });
      } else {
        const { rootId, depth } = resolveRootAndDepth(item);
        if (!rootId || rootId === Number(item.id)) {
          roots.push({ ...item, replies: [], depth: 1 });
          continue;
        }
        const list = repliesByRoot.get(rootId) || [];
        list.push({ ...item, depth });
        repliesByRoot.set(rootId, list);
      }
    }

    return roots.map(root => ({
      ...root,
      replies: (repliesByRoot.get(Number(root.id)) || []).sort(
        (a, b) => new Date(a.createTime).getTime() - new Date(b.createTime).getTime()
      ),
    }));
  });
  const hotVideoVOList = ref<HotRecommendVideoVO[]>([]);
  const defaultAvatar = '/static/images/202304061680747832129368.jpg';

  // 评论内容
  const commentContent = ref<string>('');
  const replyTargetCommentId = ref<number | null>(null);
  const replyTargetUsername = ref<string>('');

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
  let searchDebounceTimer: ReturnType<typeof setTimeout> | null = null;
  let searchAbortController: AbortController | null = null;

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
            const userIdValue = userData.userId || userData.id || '';
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
    const uid =
      video.value.userId != null && String(video.value.userId) !== ''
        ? String(video.value.userId)
        : '0';
    const avatar = video.value.authorAvatar || '';
    const name = authorName || video.value.authorName || '用户';
    router.push(
      `/author/${uid}/${encodeURIComponent(name)}?avatar=${encodeURIComponent(avatar)}`
    );
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

  const fetchCommentList = async () => {
    try {
      const videoUrl = video.value.url || route.params.id;
      if (!videoUrl) {
        return;
      }
      const currentUserId = localStorage.getItem('userId');
      const res = await axios.get('/api/video/comment/list', {
        params: {
          videoUrl,
          fanUserId: currentUserId ? Number(currentUserId) : undefined,
        },
      });
      if (res.data?.code === 200) {
        commentList.value = (res.data.data || []).map((item: any) => ({
          ...item,
          likeCount: String(item.likeCount ?? 0),
          liked: item.liked === true,
          parentId: item.parentId ? Number(item.parentId) : 0,
        }));
      }
    } catch (e) {
      console.warn('刷新评论列表失败:', e);
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
        // 与拉取评论保持同一标识，避免刚评论后查不到
        videoId: video.value.url || (route.params.id as string),
        userId: parseInt(userId),
        content: commentContent.value,
        avatar: localStorage.getItem('userAvatar'),
        username: localStorage.getItem('username'),
        parentId: replyTargetCommentId.value || 0, // 0为一级评论，非0为回复
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
        // 清空评论输入框
        commentContent.value = '';
        cancelReply();
        // 使用后端真实ID刷新评论，避免本地临时ID导致父子关系错乱
        await fetchCommentList();
      } else {
        ElMessage.error('评论失败：' + (response.data?.msg || '未知错误'));
      }
    } catch (error: any) {
      console.error('评论失败:', error);
      ElMessage.error('评论失败：' + (error.response?.data?.msg || '网络错误'));
    }
  };

  const handleReply = (comment: Comment) => {
    if (!isLoggedIn.value) {
      showLoginModal.value = true;
      return;
    }
    replyTargetCommentId.value = Number(comment.id);
    replyTargetUsername.value = comment.username || '用户';
  };

  const cancelReply = () => {
    replyTargetCommentId.value = null;
    replyTargetUsername.value = '';
  };

  const getParentCommentName = (parentId?: number) => {
    if (!parentId) {
      return '用户';
    }
    const parent = commentList.value.find(item => Number(item.id) === Number(parentId));
    return parent?.username || '用户';
  };

  const handleCommentLike = async (comment: Comment) => {
    if (!isLoggedIn.value) {
      showLoginModal.value = true;
      return;
    }
    const userId = localStorage.getItem('userId');
    if (!userId) {
      showLoginModal.value = true;
      return;
    }
    try {
      const res = await axios.post(
        '/api/video/comment/like',
        {
          commentId: Number(comment.id),
          userId: Number(userId),
        },
        {
          headers: {
            Authorization: `Bearer ${token.value}`,
          },
        }
      );
      if (res.data?.code === 200 && res.data?.data) {
        comment.liked = res.data.data.liked === true;
        comment.likeCount = String(res.data.data.likeCount ?? 0);
      } else {
        ElMessage.error(res.data?.msg || '评论点赞失败');
      }
    } catch (e: any) {
      ElMessage.error(e.response?.data?.msg || '评论点赞失败');
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
      const followUserId = await ensureAuthorUserId();
      const userId = localStorage.getItem('userId');

      if (!userId) {
        ElMessage.error('用户未登录');
        showLoginModal.value = true;
        return;
      }

      if (!followUserId) {
        ElMessage.error('未获取到作者信息，请刷新后重试');
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
          const currentFans = Number(video.value.fansCount || 0);
          video.value.fansCount = String(Math.max(0, currentFans - 1));
        } else {
          ElMessage.success('关注成功');
          isFollowed.value = true;
          const currentFans = Number(video.value.fansCount || 0);
          video.value.fansCount = String(currentFans + 1);
        }
      } else {
        const errorMessage = response.data?.msg || '未知错误';
        if (errorMessage === '已经关注该用户') {
          ElMessage.info('已经关注该用户');
          isFollowed.value = true;
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

  const ensureAuthorUserId = async (): Promise<string> => {
    // 优先使用作者名查询，避免路由里携带的是数据库主键id而非业务userId
    if (video.value.authorName) {
      try {
        const res = await axios.get(
          `/api/info/userInfoByName/${encodeURIComponent(video.value.authorName)}`
        );
        if (res.data?.code === 200 && res.data.data?.userId != null) {
          const uid = String(res.data.data.userId);
          video.value.userId = uid;
          if (res.data.data?.fansCount != null) {
            video.value.fansCount = String(res.data.data.fansCount);
          }
          return uid;
        }
      } catch (e) {
        console.warn('根据作者名解析userId失败:', e);
      }
    }

    if (video.value.userId != null && String(video.value.userId) !== '') {
      return String(video.value.userId);
    }
    return '';
  };

  const refreshFollowStatus = async () => {
    if (!isLoggedIn.value) {
      isFollowed.value = false;
      return;
    }
    const followUserId = await ensureAuthorUserId();
    const fanUserId = localStorage.getItem('userId') || '';
    if (!followUserId || !fanUserId) {
      isFollowed.value = false;
      return;
    }
    try {
      const res = await axios.get('/api/info/followStatus', {
        params: {
          followUserId: Number(followUserId),
          fanUserId: Number(fanUserId),
        },
        headers: token.value
          ? {
              Authorization: `Bearer ${token.value}`,
            }
          : undefined,
      });
      if (res.data?.code === 200) {
        isFollowed.value = res.data.data === true;
      }
    } catch (e) {
      console.warn('刷新关注状态失败:', e);
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
      const videoUrl = (video.value.url || route.params.id) as string;
      console.log('构建的videoUrl:', videoUrl);

      if (!videoUrl) {
        console.error('videoUrl为空，无法获取推荐视频');
        return;
      }

      if (!video.value.url) {
        video.value.url = videoUrl;
      }

      // 仅从消息通知/直链进入时，补齐视频基础信息，避免页面空白
      if (!videoParam) {
        try {
          const detailRes = await axios.get('/api/video/detail', {
            params: {
              videoUrl,
            },
          });
          if (detailRes.data?.code === 200 && detailRes.data?.data) {
            video.value = {
              ...video.value,
              ...detailRes.data.data,
              url: detailRes.data.data.url || videoUrl,
            };
          }
        } catch (e) {
          console.warn('拉取视频详情失败:', e);
        }
      }

      // 兜底解析作者ID（有些入口跳转时未携带userId）
      const resolvedFollowUserId = await ensureAuthorUserId();

      // 获取当前用户ID和视频作者ID
      const currentUserId = localStorage.getItem('userId');
      const followUserId = resolvedFollowUserId || video.value.userId;

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
          commentList.value = (response.data.data.commentList || []).map((item: any) => ({
            ...item,
            likeCount: String(item.likeCount ?? 0),
            liked: item.liked === true,
            parentId: item.parentId ? Number(item.parentId) : 0,
          }));
          // 更新关注状态（先用clickVideo返回，再用followStatus兜底校准）
          isFollowed.value = response.data.data.isFollow === true;
          await refreshFollowStatus();
          await fetchCommentList();
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

  onUnmounted(() => {
    if (searchDebounceTimer) {
      clearTimeout(searchDebounceTimer);
    }
    if (searchAbortController) {
      searchAbortController.abort();
    }
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
    display: flex;
    flex-direction: column;
    background: linear-gradient(180deg, #eef7ff 0%, #f6f7f8 240px, #f6f7f8 100%);
  }

  .header {
    position: sticky;
    top: 0;
    z-index: 120;
    backdrop-filter: blur(10px);
    background: rgba(255, 255, 255, 0.9);
    border-bottom: 1px solid #e8eaed;
  }

  .header-content,
  .video-main,
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
    padding: 0 96px 0 16px;
    outline: none;
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
    padding: 0 14px;
    cursor: pointer;
  }

  .search-dropdown {
    position: absolute;
    top: calc(100% + 8px);
    left: 0;
    right: 0;
    border: 1px solid var(--bili-border);
    border-radius: 14px;
    background: #fff;
    box-shadow: 0 8px 24px rgba(24, 25, 28, 0.08);
    z-index: 130;
  }

  .video-main {
    flex: 1;
    padding: 20px 0 14px;
  }

  .video-container {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 320px;
    gap: 18px;
  }

  .video-player,
  .recommend-videos {
    background: #fff;
    border: 1px solid #eceef1;
    border-radius: 14px;
    box-shadow: 0 4px 14px rgba(24, 25, 28, 0.04);
  }

  .video-player {
    padding: 16px;
  }

  .video-element {
    width: 100%;
    border-radius: 10px;
    background: #000;
    aspect-ratio: 16 / 9;
    height: auto;
  }

  .video-title {
    font-size: 22px;
    margin: 14px 0 10px;
  }

  .video-stats {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid #eff1f3;
  }

  .author-info,
  .video-actions {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .author-details {
    display: flex;
    align-items: baseline;
    gap: 12px;
  }

  .author-fans {
    color: #61666d;
    white-space: nowrap;
  }

  .follow-btn {
    margin-left: 10px;
  }

  .action-item {
    border: 1px solid #e8eaee;
    border-radius: 999px;
    padding: 8px 12px;
    color: #61666d;
    display: flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
  }

  .action-item:hover,
  .action-item.liked,
  .action-item.collected {
    border-color: var(--bili-primary);
    color: var(--bili-primary);
    background: #f1fbff;
  }

  .video-desc {
    margin: 14px 0;
    padding: 14px;
    border-radius: 10px;
    background: #f8f9fa;
    color: #61666d;
  }

  .comment-input {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
    background: #f8f9fa;
    border: 1px solid #eceef1;
    border-radius: 10px;
    padding: 10px;
  }

  .comment-input input {
    flex: 1 1 320px;
    border: 1px solid #dfe3e8;
    border-radius: 8px;
    padding: 9px 10px;
  }

  .comment-submit {
    border: none;
    border-radius: 999px;
    background: var(--bili-primary);
    color: #fff;
    padding: 8px 16px;
    cursor: pointer;
  }

  .replying-tip {
    width: 100%;
    color: #61666d;
    font-size: 12px;
  }

  .replying-cancel {
    margin-left: 8px;
    border: none;
    background: transparent;
    color: var(--bili-primary);
    cursor: pointer;
  }

  .comment-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-top: 16px;
  }

  .comment-thread {
    border-bottom: 1px solid #f0f2f5;
    padding: 10px 2px 12px;
  }

  .comment-item {
    display: flex;
    gap: 12px;
    padding: 0;
  }

  .comment-item.is-reply {
    gap: 10px;
    padding-top: 10px;
  }

  .comment-avatar {
    flex-shrink: 0;
    margin-top: 2px;
  }

  .comment-main {
    flex: 1;
    min-width: 0;
  }

  .comment-header {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 6px;
    line-height: 1;
  }

  .comment-username {
    font-size: 14px;
    font-weight: 600;
    color: #18191c;
  }

  .comment-dot {
    color: #c0c4cc;
    font-size: 12px;
  }

  .comment-time {
    color: #9499a0;
    font-size: 12px;
  }

  .comment-content {
    background: transparent;
    border: none;
    border-radius: 0;
    padding: 0;
  }

  .comment-reply-to {
    color: #9499a0;
    font-size: 12px;
    margin: 0 0 2px;
  }

  .comment-text {
    color: #303133;
    font-size: 14px;
    line-height: 1.6;
    margin: 0;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .comment-actions {
    margin-top: 8px;
    display: flex;
    align-items: center;
    gap: 16px;
    color: #9499a0;
    font-size: 13px;
  }

  .comment-like,
  .comment-reply {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
    transition: color 0.2s ease;
  }

  .comment-like:hover,
  .comment-reply:hover {
    color: var(--bili-primary);
  }

  .comment-like.active {
    color: var(--bili-primary);
  }

  .comment-replies {
    margin-left: 52px;
    margin-top: 6px;
    padding-left: 12px;
    border-left: 2px solid #f2f3f5;
  }

  .recommend-videos {
    padding: 14px;
    height: fit-content;
    max-height: calc(100vh - 92px);
    overflow: auto;
  }

  .recommend-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .recommend-item {
    display: flex;
    gap: 8px;
    cursor: pointer;
    padding: 6px;
    border-radius: 10px;
  }

  .recommend-item:hover {
    background: #f7f8fa;
  }

  .recommend-cover {
    width: 126px;
    height: 72px;
    border-radius: 8px;
    overflow: hidden;
    flex-shrink: 0;
  }

  .recommend-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .recommend-video-title {
    font-size: 13px;
    line-height: 1.35;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    display: -webkit-box;
    overflow: hidden;
  }

  .footer {
    border-top: 1px solid #e9ebee;
    background: #fff;
    padding: 18px 0;
  }

  .footer-content p {
    color: #9499a0;
    font-size: 13px;
  }

  @media (max-width: 1080px) {
    .video-container {
      grid-template-columns: 1fr;
    }
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
