<template>
  <div class="video-manager-page">
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

    <!-- 内容区 -->
    <main class="manager-content">
      <div class="manager-header">
        <h1>稿件管理</h1>
      </div>

      <!-- 管理标签页 -->
      <div class="manager-tabs">
        <div
          class="tab-item"
          :class="{ active: activeTab === 'video' }"
          @click="handleTabClick('video')"
        >
          视频管理
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === 'collection' }"
          @click="handleTabClick('collection')"
        >
          合集管理
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === 'image' }"
          @click="handleTabClick('image')"
        >
          图文管理
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === 'interactive' }"
          @click="handleTabClick('interactive')"
        >
          互动视频管理
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === 'audio' }"
          @click="handleTabClick('audio')"
        >
          音频管理
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === 'sticker' }"
          @click="handleTabClick('sticker')"
        >
          贴纸管理
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === 'material' }"
          @click="handleTabClick('material')"
        >
          视频素材管理
        </div>
        <div class="search-container">
          <input type="text" placeholder="搜索稿件" class="search-input" />
          <button class="search-btn"><i class="el-icon-search"></i></button>
        </div>
      </div>

      <!-- 状态标签 -->
      <div class="status-tabs">
        <div class="status-item active">全部稿件</div>
        <div class="action-buttons">
          <el-button
            type="primary"
            style="background-color: #1890ff; color: white; border-radius: 4px; border: none"
          >
            + 添加合集
          </el-button>
          <el-select v-model="sortOption" placeholder="投稿时间排序">
            <el-option label="投稿时间排序" value="time" />
            <el-option label="播放量排序" value="play" />
            <el-option label="点赞数排序" value="like" />
          </el-select>
        </div>
      </div>

      <!-- 视频列表 -->
      <div class="video-list">
        <div v-if="videoList.length > 0">
          <div v-for="video in videoList" :key="video.id" class="video-item">
            <div class="video-info">
              <div class="video-cover">
                <img :src="video.cover || video.coverUrl" alt="视频封面" />
                <div class="video-duration">{{ video.duration }}</div>
              </div>
              <div class="video-details">
                <h3 class="video-title">{{ video.title }}</h3>
                <p class="video-info-text">{{ video.createTime }}</p>
                <p class="video-info-text">{{ video.description }}</p>
                <div class="video-stats">
                  <span
                    ><i class="el-icon-video-camera"></i>
                    {{ formatPlayCount(video.playCount) }}</span
                  >
                  <span><i class="el-icon-star-on"></i> {{ video.likeCount }}</span>
                  <span><i class="el-icon-chat-dot-round"></i> {{ video.commentCount }}</span>
                  <span><i class="el-icon-collection-tag"></i> {{ video.collectCount }}</span>
                  <span><i class="el-icon-share"></i> {{ video.shareCount }}</span>
                </div>
              </div>
            </div>
            <div class="video-actions">
              <el-button
                type="default"
                style="margin-right: 10px"
                @click="handleDeleteVideo(video.url, video.userId || 0)"
              >
                <i class="el-icon-edit"></i> 删除
              </el-button>
            </div>
          </div>
        </div>
        <div v-else class="no-data">
          <p>暂无数据</p>
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
          <h2>登录</h2>
          <button class="close-btn" @click="closeLoginModal">&times;</button>
        </div>
        <div class="login-content">
          <div class="login-tabs">
            <div class="main-tabs">
              <button
                :class="['tab-btn', { active: activeMode === 'login' }]"
                @click="activeMode = 'login'"
              >
                账号密码登录
              </button>
              <button
                :class="['tab-btn', { active: activeMode === 'register' }]"
                @click="activeMode = 'register'"
              >
                注册
              </button>
            </div>
          </div>
          <div class="login-form">
            <div v-if="activeMode === 'login'">
              <div class="form-group">
                <label for="account">账号</label>
                <input
                  type="text"
                  id="account"
                  v-model="loginForm.account"
                  placeholder="请输入手机号"
                  maxlength="11"
                />
              </div>
              <div class="form-group">
                <label for="password">密码</label>
                <input
                  type="password"
                  id="password"
                  v-model="loginForm.password"
                  placeholder="请输入密码"
                  maxlength="16"
                />
              </div>
              <button class="login-submit" @click="handleLoginSubmit">登录</button>
            </div>
            <div v-else>
              <div class="form-group">
                <label for="reg-phone">手机号</label>
                <input
                  type="text"
                  id="reg-phone"
                  v-model="loginForm.phone"
                  placeholder="请输入手机号"
                  maxlength="11"
                />
              </div>
              <div class="form-group">
                <label for="reg-password">密码</label>
                <input
                  type="password"
                  id="reg-password"
                  v-model="loginForm.password"
                  placeholder="请输入密码"
                  maxlength="16"
                />
              </div>
              <button class="login-submit" @click="handleRegister">注册</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, watch } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import axios from 'axios';

  // 视频类型接口
  interface Video {
    id?: string | number;
    userId?: number;
    title: string;
    url: string;
    cover: string;
    coverUrl?: string;
    duration: string;
    createTime: string;
    description?: string;
    playCount: string | number;
    likeCount: string | number;
    commentCount: string | number;
    collectCount?: string | number;
    collectionCount?: string | number;
    shareCount?: string | number;
    typeId?: number;
  }

  const router = useRouter();
  const route = useRoute();
  const defaultAvatar = '/static/images/202304061680747832129368.jpg';

  // 用户ID
  const userId = ref<string>(
    (route.params.userId as string) ||
      (route.query.userId as string) ||
      localStorage.getItem('userId') ||
      ''
  );

  // 监听路由参数变化
  watch(
    () => route.params,
    newParams => {
      if (newParams.userId) {
        userId.value = newParams.userId as string;
        fetchVideoList();
      }
    },
    { deep: true }
  );

  // 监听路由查询参数变化
  watch(
    () => route.query,
    newQuery => {
      if (newQuery.userId) {
        userId.value = newQuery.userId as string;
        fetchVideoList();
      }
    },
    { deep: true }
  );

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

  // 登录模态框
  const showLoginModal = ref<boolean>(false);
  const activeMode = ref<'login' | 'register'>('login');
  const loginForm = ref({
    account: '',
    password: '',
    phone: '',
  });

  // 排序选项
  const sortOption = ref<string>('time');

  // 当前激活的标签页
  const activeTab = ref<string>('video');

  // 处理标签页点击
  const handleTabClick = (tab: string) => {
    activeTab.value = tab;
    // 这里可以根据需要添加其他逻辑，如加载对应数据等
  };

  // 视频列表数据
  const videoList = ref<Video[]>([]);

  // 获取视频列表数据
  const fetchVideoList = async () => {
    try {
      const userIdValue = userId.value || localStorage.getItem('userId');
      if (userIdValue) {
        const response = await axios.get('/api/info/authorPage/' + userIdValue);
        if (response.data && response.data.code === 200) {
          videoList.value = response.data.data || [];
        }
      }
    } catch (error) {
      console.error('获取视频列表失败:', error);
    }
  };

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
    localStorage.removeItem('userId');
    token.value = '';
    username.value = '';
    userAvatar.value = '';
    isLoggedIn.value = false;
  };

  // 删除视频
  const handleDeleteVideo = async (videoUrl: string, userIdValue: number) => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        console.error('用户未登录，无法删除视频');
        return;
      }
      if (!userIdValue) {
        console.error('用户未登录，无法删除视频');
        return;
      }

      const response = await axios.post(
        '/api/info/videoDelete',
        {
          videoUrl: videoUrl,
          userId: userIdValue,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (response.data && response.data.code === 200) {
        console.log('删除视频成功');
        // 乐观更新：直接从本地视频列表中移除被删除的视频
        videoList.value = videoList.value.filter(video => video.url !== videoUrl);
      } else {
        console.error('删除视频失败:', response.data?.msg || '未知错误');
      }
    } catch (error) {
      console.error('删除视频失败:', error);
    }
  };

  // 打开登录模态框
  const handleLogin = () => {
    showLoginModal.value = true;
  };

  // 关闭登录模态框
  const closeLoginModal = () => {
    showLoginModal.value = false;
  };

  // 登录提交
  const handleLoginSubmit = async () => {
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
          } else {
            console.error('登录失败: 响应数据结构不正确');
          }
        } else {
          console.error('登录失败:', response.data.msg || '未知错误');
        }
      } else {
        console.error('登录失败: 响应数据为空');
      }
    } catch (error) {
      console.error('登录失败:', error);
    }
  };

  // 注册提交
  const handleRegister = async () => {
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
            username: loginForm.value.phone, // 使用手机号作为用户名
          },
        }
      );

      console.log('注册响应:', response);

      if (response.data) {
        if (response.data.code === 200) {
          console.log('注册成功');
          // 注册成功后切换到登录模式
          activeMode.value = 'login';
        } else {
          console.error('注册失败:', response.data.msg || '未知错误');
        }
      } else {
        console.error('注册失败: 响应数据为空');
      }
    } catch (error) {
      console.error('注册失败:', error);
    }
  };

  // 退出登录
  const handleLogout = () => {
    // 退出登录逻辑
    clearLoginInfo();
    console.log('用户已退出登录');
  };

  // 点击头像跳转到个人页面
  const handleAvatarClick = () => {
    // 点击顶部头像跳转到个人页面
    if (isLoggedIn.value) {
      const currentUserId = localStorage.getItem('userId') || '1'; // 使用默认值1
      const username = localStorage.getItem('username') || '张三';
      const avatar = localStorage.getItem('userAvatar') || '';
      const path = `/author/${currentUserId}/${username}?avatar=${encodeURIComponent(avatar)}`;
      router.push(path);
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
      return String(count);
    }
  };

  onMounted(() => {
    initUserInfo();
    fetchVideoList();
  });

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
  .video-manager-page {
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

  .logo h2 {
    margin: 0;
    font-size: 20px;
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

  .user-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 5px;
  }

  /* 内容区 */
  .manager-content {
    flex: 1;
    width: 1200px;
    margin: 0 auto;
    padding: 20px 0;
  }

  .manager-header {
    margin-bottom: 20px;
  }

  .manager-header h1 {
    font-size: 24px;
    color: #333;
    margin: 0;
  }

  /* 管理标签页 */
  .manager-tabs {
    display: flex;
    align-items: center;
    background-color: #fff;
    padding: 15px 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    margin-bottom: 20px;
    flex-wrap: wrap;
    gap: 15px;
  }

  .tab-item {
    padding: 8px 16px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.3s ease;
  }

  .tab-item:hover {
    color: #1890ff;
  }

  .tab-item.active {
    color: #1890ff;
    font-weight: bold;
    background-color: #e6f7ff;
  }

  .search-container {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 5px;
  }

  .search-input {
    width: 200px;
    height: 32px;
    padding: 0 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
  }

  /* 状态标签 */
  .status-tabs {
    display: flex;
    align-items: center;
    background-color: #fff;
    padding: 15px 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    margin-bottom: 20px;
    flex-wrap: wrap;
    gap: 15px;
  }

  .status-item {
    padding: 8px 16px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.3s ease;
  }

  .status-item:hover {
    color: #1890ff;
  }

  .status-item.active {
    color: #1890ff;
    font-weight: bold;
    background-color: #e6f7ff;
  }

  .action-buttons {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 10px;
  }

  /* 视频列表 */
  .video-list {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    padding: 20px;
  }

  .video-item {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 20px 0;
    border-bottom: 1px solid #eee;
  }

  .video-item:last-child {
    border-bottom: none;
  }

  .video-info {
    display: flex;
    align-items: flex-start;
    gap: 15px;
    flex: 1;
  }

  .video-cover {
    width: 120px;
    height: 68px;
    position: relative;
    flex-shrink: 0;
  }

  .video-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 4px;
  }

  .video-duration {
    position: absolute;
    bottom: 4px;
    right: 4px;
    background-color: rgba(0, 0, 0, 0.6);
    color: white;
    font-size: 12px;
    padding: 2px 4px;
    border-radius: 2px;
  }

  .video-details {
    flex: 1;
  }

  .video-title {
    font-size: 16px;
    color: #333;
    margin: 0 0 8px 0;
    font-weight: 500;
  }

  .video-info-text {
    font-size: 14px;
    color: #999;
    margin: 0 0 8px 0;
  }

  .video-stats {
    display: flex;
    gap: 15px;
    font-size: 14px;
    color: #999;
  }

  .video-stats span {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .video-actions {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .no-data {
    text-align: center;
    padding: 60px 0;
    color: #999;
    font-size: 16px;
    background-color: #f9f9f9;
    border-radius: 8px;
    margin-top: 20px;
  }

  /* 版权footer */
  .footer {
    background-color: #fff;
    padding: 20px 0;
    margin-top: 40px;
    border-top: 1px solid #eee;
  }

  .footer-content {
    width: 1200px;
    margin: 0 auto;
    text-align: center;
    font-size: 14px;
    color: #999;
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
  }

  .login-modal {
    background-color: #fff;
    border-radius: 8px;
    width: 400px;
    max-width: 90%;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  .login-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #eee;
  }

  .login-header h2 {
    margin: 0;
    font-size: 18px;
    color: #333;
  }

  .close-btn {
    background: none;
    border: none;
    font-size: 20px;
    cursor: pointer;
    color: #999;
  }

  .login-content {
    padding: 20px;
  }

  .login-tabs {
    margin-bottom: 20px;
  }

  .main-tabs {
    display: flex;
    border-bottom: 1px solid #eee;
  }

  .tab-btn {
    flex: 1;
    padding: 10px;
    background: none;
    border: none;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    text-align: center;
  }

  .tab-btn.active {
    color: #1890ff;
    border-bottom: 2px solid #1890ff;
  }

  .form-group {
    margin-bottom: 15px;
  }

  .form-group label {
    display: block;
    margin-bottom: 5px;
    font-size: 14px;
    color: #333;
  }

  .form-group input {
    width: 100%;
    height: 40px;
    padding: 0 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
  }

  .login-submit {
    width: 100%;
    height: 40px;
    background-color: #1890ff;
    color: #fff;
    border: none;
    border-radius: 4px;
    font-size: 14px;
    cursor: pointer;
    margin-top: 10px;
  }

  .login-submit:hover {
    background-color: #40a9ff;
  }

  /* 响应式调整 */
  @media (max-width: 1200px) {
    .header-content,
    .manager-content,
    .footer-content {
      width: 95%;
    }

    .manager-tabs,
    .status-tabs {
      flex-direction: column;
      align-items: flex-start;
    }

    .search-container,
    .action-buttons {
      margin-left: 0;
      width: 100%;
    }

    .search-input {
      width: 100%;
    }

    .video-item {
      flex-direction: column;
      align-items: flex-start;
      gap: 15px;
    }

    .video-actions {
      align-self: flex-end;
    }
  }
</style>
