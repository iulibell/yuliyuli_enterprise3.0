<template>
  <div class="search-result">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo">
          <h2 style="color: deeppink; cursor: pointer" @click="router.push('/')">Yuliyuli</h2>
        </div>
        <router-link class="header-back-link" to="/">
          <i class="el-icon-arrow-left"></i>首页
        </router-link>
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
            <!-- 搜索下拉列表 -->
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
              border: none;
              height: 33px;
            "
            class="upload-btn"
            @click="handleUploadClick"
          >
            投稿
          </button>
        </div>
      </div>
    </header>

    <!-- 搜索结果内容 -->
    <main class="search-content">
      <!-- 分类标签 -->
      <div class="category-tabs" v-if="!loading && searchResults.length > 0">
        <div
          class="tab-item"
          :class="{ active: activeTab === '综合' }"
          @click="handleTabClick('综合')"
        >
          综合
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === '视频' }"
          @click="handleTabClick('视频')"
        >
          视频
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === '番剧' }"
          @click="handleTabClick('番剧')"
        >
          番剧
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === '影视' }"
          @click="handleTabClick('影视')"
        >
          影视
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === '直播' }"
          @click="handleTabClick('直播')"
        >
          直播
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === '专栏' }"
          @click="handleTabClick('专栏')"
        >
          专栏
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === '用户' }"
          @click="handleTabClick('用户')"
        >
          用户
        </div>
      </div>

      <!-- 排序选项 -->
      <div class="sort-options" v-if="!loading && searchResults.length > 0 && activeTab === '综合'">
        <div
          class="sort-item"
          :class="{ active: activeSort === '综合排序' }"
          @click="handleSortClick('综合排序')"
        >
          综合排序
        </div>
        <div
          class="sort-item"
          :class="{ active: activeSort === '最多播放' }"
          @click="handleSortClick('最多播放')"
        >
          最多播放        </div>
        <div
          class="sort-item"
          :class="{ active: activeSort === '最新发布' }"
          @click="handleSortClick('最新发布')"
        >
          最新发布        </div>
        <div
          class="sort-item"
          :class="{ active: activeSort === '最多弹幕' }"
          @click="handleSortClick('最多弹幕')"
        >
          最多弹幕        </div>
        <div
          class="sort-item"
          :class="{ active: activeSort === '最多收藏' }"
          @click="handleSortClick('最多收藏')"
        >
          最多收藏        </div>
        <div class="sort-more">更多筛选?<i class="el-icon-arrow-down"></i></div>
      </div>

      <!-- 搜索结果网格 -->
      <div class="result-grid" v-if="searchResults.length > 0 && activeTab === '综合'">
        <div
          v-for="video in searchResults"
          :key="video.url"
          class="result-card"
          @click="handleVideoClick(video)"
        >
          <div class="result-cover">
            <img :src="video.cover" alt="视频封面" />
            <div class="result-stats-overlay">
              <span class="play-count"
                ><i class="el-icon-video-camera"></i> 👁
                {{ formatPlayCount(video.playCount) }}</span
              >
              <span class="danmaku-count"
                ><i class="el-icon-chat-dot-round"></i> 💬 {{ video.commentCount }}</span
              >
            </div>
          </div>
          <div class="result-info">
            <h3 class="result-title">{{ video.title }}</h3>
            <div class="result-meta">
              <span class="result-author" @click.stop="handleAuthorClick(video)">{{
                video.authorName
              }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 其他标签显示还在开发中 -->
      <div class="no-result" v-else-if="!loading && activeTab !== '综合'">
        <p>该功能还在开发中</p>
        <p class="no-result-tip">敬请期待</p>
      </div>

      <!-- 无结果提示 -->
      <div class="no-result" v-else-if="!loading">
        <div class="no-result-icon">🔍</div>
        <p>抱歉，没有找到相关视频</p>
        <p class="no-result-tip">换个关键词试试吧</p>
      </div>

      <!-- 加载中 -->
      <div class="loading" v-if="loading">
        <el-loading-spinner />
        <p>正在搜索...</p>
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
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import axios from 'axios';
  import { ElMessage } from 'element-plus';

  interface Video {
    id?: string;
    url: string;
    title: string;
    intro: string;
    cover: string;
    coverUrl?: string;
    duration: string;
    playCount: string;
    likeCount: string;
    commentCount: string;
    collectionCount: string;
    authorName: string;
    authorAvatar: string;
    createTime: string;
    typeId?: number;
    userId?: string;
  }

  const route = useRoute();
  const router = useRouter();
  const searchQuery = ref<string>('');
  const currentKeyword = ref<string>('');
  const searchResults = ref<Video[]>([]);
  const loading = ref<boolean>(false);
  const activeTab = ref<string>('综合');
  const activeSort = ref<string>('综合排序');

  // 搜索相关
  const showSearchDropdown = ref<boolean>(false);
  const searchSuggestions = ref<string[]>([]);
  const hotSearchKeywords = ref<Array<{ keyword: string; hot?: string }>>([]);

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

  const handleLogin = () => {
    // 跳转到首页并打开登录弹窗
    router.push({
      path: '/',
      query: { showLogin: '1' },
    });
  };

  const handleUploadClick = () => {
    if (isLoggedIn.value) {
      router.push('/upload');
      return;
    }
    handleLogin();
  };

  const handleLogout = () => {
    clearLoginInfo();
    ElMessage.success('已退出登录');
  };

  // 格式化播放量
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

  // 获取热门搜索（前十热门视频）
  const fetchHotSearch = async () => {
    try {
      const token = localStorage.getItem('token');
  
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
            hot: index < 3 ? 'HOT' : index < 5 ? 'NEW' : index < 8 ? 'TOP' : undefined,
          };
        });
          } else {
            hotSearchKeywords.value = [];
      }
    } catch (error: any) {
      // 401错误时不显示错误信息，只设置空数组
      if (error.response && error.response.status === 401) {
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
    // 跳转到搜索结果页
    router.push({
      path: '/search',
      query: { keyword: suggestion },
    });
  };

  const selectHotKeyword = (keyword: string) => {
    searchQuery.value = keyword;
    showSearchDropdown.value = false;
    // 跳转到搜索结果页
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

  const handleAvatarClick = () => {
    if (isLoggedIn.value) {
      const currentUserId = localStorage.getItem('userId');
      const username = localStorage.getItem('username') || '张三';
      const avatar = localStorage.getItem('userAvatar') || '';
      router.push(`/author/${currentUserId}/${username}?avatar=${encodeURIComponent(avatar)}`);
    }
  };

  // 搜索功能
  const handleSearch = () => {
    if (searchQuery.value.trim()) {
      router.push({
        path: '/search',
        query: {
          keyword: searchQuery.value,
          tab: activeTab.value,
          sort: activeSort.value,
        },
      });
    }
  };

  // 获取搜索结果
  const fetchSearchResults = async (keyword: string) => {
    if (!keyword) return;

    loading.value = true;
    currentKeyword.value = keyword;

    try {
      const token = localStorage.getItem('token');

      // 构建请求配置
      const config: any = {
        params: { keyword },
      };

      // 只有当token存在时才添加Authorization头
      if (token) {
        config.headers = {
          Authorization: `Bearer ${token}`,
        };
      }

      const response = await axios.get('/api/search/video', config);

      if (response.data && response.data.code === 200) {
        // 处理搜索结果 - 后端返回的是SearchVideoVO列表
    
        // 直接检查是否有视频数据
        if (Array.isArray(response.data.data)) {
          searchResults.value = response.data.data
            .map((item: any) => {
              // 打印每个item的详细信息，帮助调试
                                        
              // 检查item是否直接包含视频信息
              if (item.title) {
                // 直接是视频对象
                return {
                  id: item.id,
                  url: item.videoId || item.url,
                  title: item.title,
                  intro: item.intro || item.description || '',
                  cover: item.cover || item.coverUrl,
                  coverUrl: item.coverUrl,
                  duration: item.duration || '00:00',
                  playCount: item.playCount || '0',
                  likeCount: item.likeCount || '0',
                  commentCount: item.commentCount || '0',
                  collectionCount: item.collectionCount || item.collectCount || '0',
                  authorName: item.authorName || item.author || '未知作者',
                  authorAvatar: item.authorAvatar || '/static/images/202304061680747832129368.jpg',
                  createTime: item.createTime,
                  typeId: item.typeId,
                  userId: item.userId,
                };
              } else if (item.videoDocuments && item.videoDocuments.length > 0) {
                // 包含videoDocuments数组
                return item.videoDocuments.map((doc: any) => ({
                  id: doc.id,
                  url: doc.url,
                  title: doc.title,
                  intro: doc.intro || doc.description || '',
                  cover: doc.cover || doc.coverUrl,
                  coverUrl: doc.coverUrl,
                  duration: doc.duration || '00:00',
                  playCount: doc.playCount || '0',
                  likeCount: doc.likeCount || '0',
                  commentCount: doc.commentCount || '0',
                  collectionCount: doc.collectionCount || doc.collectCount || '0',
                  authorName: doc.authorName || doc.author || '未知作者',
                  authorAvatar: doc.authorAvatar || '/static/images/202304061680747832129368.jpg',
                  createTime: doc.createTime,
                  typeId: doc.typeId,
                  userId: doc.userId,
                }));
              }
              return null;
            })
            .flat()
            .filter(Boolean);
        }

          } else {
        searchResults.value = [];
      }
    } catch (error: any) {
      // 401错误时不显示错误信息，只设置空数组
      if (error.response && error.response.status === 401) {
          } else {
        console.error('搜索失败:', error);
        ElMessage.error('搜索失败，请稍后重试');
      }
      searchResults.value = [];
    } finally {
      loading.value = false;
    }
  };

  // 点击视频跳转到详情页
  const handleVideoClick = (video: Video) => {
    // 使用url作为视频ID，与HomeView保持一致
    const videoId = video.id || video.url || 'unknown';
    router.push({
      path: `/video/${encodeURIComponent(videoId)}`,
      query: {
        video: JSON.stringify(video),
      },
    });
  };

  // 点击作者名字跳转到作者主页
  const handleAuthorClick = (video: Video) => {
    if (video.userId) {
      router.push({
        path: `/author/${video.userId}/${encodeURIComponent(video.authorName)}`,
        query: {
          avatar: video.authorAvatar,
        },
      });
    }
  };

  // 处理标签点击事件
  const handleTabClick = (tab: string) => {
    activeTab.value = tab;
    // 跳转到当前页面（刷新搜索结果）
    router.push({
      path: '/search',
      query: {
        keyword: currentKeyword.value,
        tab: tab,
      },
    });
  };

  // 处理排序选项点击事件
  const handleSortClick = (sort: string) => {
    activeSort.value = sort;
    // 跳转到当前页面（刷新搜索结果）
    router.push({
      path: '/search',
      query: {
        keyword: currentKeyword.value,
        tab: activeTab.value,
        sort: sort,
      },
    });
  };

  // 监听路由参数变化
  watch(
    () => route.query,
    newQuery => {
      const keyword = newQuery.keyword as string;
      const tab = newQuery.tab as string;
      const sort = newQuery.sort as string;

      if (keyword) {
        searchQuery.value = keyword;
        fetchSearchResults(keyword);
      }

      if (tab) {
        activeTab.value = tab;
      }

      if (sort) {
        activeSort.value = sort;
      }
    },
    { immediate: true }
  );

  onMounted(() => {
    initUserInfo();
  });
</script>

<style scoped>
  .search-result {
    width: 100%;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background: url('/static/images/v2-217f1b1062ab037739e18c823aa15db6_r.jpg') center / cover
      no-repeat fixed;
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
  .search-content,
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
    left: 0;
    right: 0;
    top: calc(100% + 8px);
    border: 1px solid var(--bili-border);
    border-radius: 14px;
    background: #fff;
    box-shadow: 0 8px 24px rgba(24, 25, 28, 0.08);
    max-height: 320px;
    overflow: auto;
    z-index: 130;
  }

  .search-content {
    flex: 1;
    margin-top: 20px;
    margin-bottom: 12px;
    padding: 20px;
    border: 1px solid #ebedf0;
    border-radius: 14px;
    background: #fff;
  }

  .category-tabs,
  .sort-options {
    display: flex;
    gap: 12px;
    align-items: center;
    flex-wrap: wrap;
    padding: 10px 0;
  }

  .tab-item,
  .sort-item {
    color: #61666d;
    cursor: pointer;
    border-radius: 999px;
    padding: 6px 12px;
    background: #f6f7f8;
    font-size: 13px;
  }

  .tab-item.active,
  .sort-item.active,
  .tab-item:hover,
  .sort-item:hover {
    color: #fff;
    background: var(--bili-primary);
  }

  .sort-more {
    margin-left: auto;
    color: #9499a0;
    font-size: 13px;
  }

  .result-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 18px;
  }

  .result-card {
    border-radius: 12px;
    overflow: hidden;
    border: 1px solid #edf0f2;
    background: #fff;
    transition: transform 0.2s, box-shadow 0.2s;
  }

  .result-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 22px rgba(24, 25, 28, 0.08);
  }

  .result-cover {
    position: relative;
    padding-top: 56.25%;
  }

  .result-cover img {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .result-stats-overlay {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    color: #fff;
    font-size: 12px;
    display: flex;
    justify-content: space-between;
    padding: 14px 8px 8px;
    background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.65));
  }

  .result-info {
    padding: 10px 12px 12px;
  }

  .result-title {
    color: #18191c;
    font-size: 15px;
    margin-bottom: 8px;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .result-author {
    color: #9499a0;
    font-size: 12px;
  }

  .result-author:hover {
    color: var(--bili-primary);
  }

  .no-result,
  .loading {
    border-radius: 12px;
    border: 1px dashed #dde1e6;
    background: #fafbfc;
    text-align: center;
    padding: 64px 20px;
    color: #9499a0;
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
</style>
