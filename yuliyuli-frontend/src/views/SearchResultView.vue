<template>
  <div class="search-result">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo">
          <h2 style="color: deeppink; cursor: pointer" @click="router.push('/')">Yuliyuli</h2>
        </div>
        <router-link style="color: grey; text-decoration-line: none; margin-left: -80px" to="/">
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
              border: none;
              height: 33px;
            "
            class="upload-btn"
            @click="router.push('/upload')"
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
    router.push('/');
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
    background-image: url('/static/images/v2-217f1b1062ab037739e18c823aa15db6_r.jpg');
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
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

  .user-icon {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  /* 搜索结果内容 */
  .search-content {
    width: 1200px;
    margin: 0 auto;
    padding: 20px;
    flex: 1;
    background-color: rgba(255, 255, 255, 0.95);
    border-radius: 8px;
    margin-top: 20px;
    margin-bottom: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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

  .search-header {
    margin-bottom: 20px;
    padding: 20px;
    background-color: #fff;
    border-radius: 8px;
  }

  .search-title {
    font-size: 18px;
    color: #333;
    margin: 0 0 10px 0;
  }

  .search-count {
    font-size: 14px;
    color: #999;
    margin: 0;
  }

  /* 分类标签 */
  .category-tabs {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 15px 0;
    border-bottom: 1px solid #e5e5e5;
    margin-bottom: 15px;
  }

  .tab-item {
    font-size: 14px;
    color: #666;
    cursor: pointer;
    padding: 5px 0;
    position: relative;
  }

  .tab-item:hover {
    color: #00a1d6;
  }

  .tab-item.active {
    color: #00a1d6;
    font-weight: 500;
  }

  .tab-item.active::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2px;
    background-color: #00a1d6;
  }

  /* 排序选项 */
  .sort-options {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 15px 0;
    margin-bottom: 20px;
    background-color: #fff;
    border-radius: 8px;
    padding: 10px 20px;
  }

  .sort-item {
    font-size: 14px;
    color: #666;
    cursor: pointer;
    padding: 5px 10px;
    border-radius: 15px;
    transition: all 0.3s ease;
  }

  .sort-item:hover {
    color: #00a1d6;
    background-color: #f0f7ff;
  }

  .sort-item.active {
    color: #00a1d6;
    background-color: #e6f7ff;
    font-weight: 500;
  }

  .sort-more {
    font-size: 14px;
    color: #666;
    cursor: pointer;
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 5px;
  }

  /* 搜索结果网格 */
  .result-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 20px;
  }

  .result-card {
    background-color: #fff;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s ease;
  }

  .result-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  .result-cover {
    position: relative;
    width: 100%;
    padding-top: 56.25%; /* 16:9 比例 */
    overflow: hidden;
  }

  .result-cover img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .result-duration {
    position: absolute;
    bottom: 5px;
    right: 5px;
    background-color: rgba(0, 0, 0, 0.7);
    color: white;
    font-size: 12px;
    padding: 2px 5px;
    border-radius: 2px;
    z-index: 2;
  }

  .result-stats-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
    color: white;
    font-size: 12px;
    padding: 10px 5px 5px;
    display: flex;
    justify-content: space-between;
    z-index: 1;
  }

  .play-count,
  .danmaku-count {
    display: flex;
    align-items: center;
    gap: 3px;
  }

  .result-info {
    padding: 10px;
  }

  .result-title {
    font-size: 14px;
    color: #333;
    margin: 0 0 8px 0;
    line-height: 1.4;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .result-meta {
    display: flex;
    align-items: center;
    font-size: 12px;
    color: #999;
  }

  .result-author {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 100%;
    cursor: pointer;
    transition: color 0.3s ease;
  }

  .result-author:hover {
    color: #00a1d6;
  }

  /* 无结果提示 */
  .no-result {
    text-align: center;
    padding: 100px 20px;
    background-color: #fff;
    border-radius: 8px;
  }

  .no-result-icon {
    font-size: 60px;
    margin-bottom: 20px;
  }

  .no-result p {
    font-size: 16px;
    color: #666;
    margin: 0 0 10px 0;
  }

  .no-result-tip {
    font-size: 14px;
    color: #999;
  }

  /* 加载中 */
  .loading {
    text-align: center;
    padding: 100px 20px;
    background-color: #fff;
    border-radius: 8px;
  }

  .loading p {
    margin-top: 20px;
    color: #999;
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
  @media (max-width: 1200px) {
    .header-content,
    .search-content,
    .footer-content {
      width: 95%;
    }
  }

  @media (max-width: 768px) {
    .result-item {
      flex-direction: column;
    }

    .result-cover {
      width: 100%;
      height: 180px;
    }

    .result-info {
      margin-left: 0;
      margin-top: 10px;
    }
  }
</style>
