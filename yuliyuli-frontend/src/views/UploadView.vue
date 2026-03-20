<template>
  <div class="upload-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo">
          <h2 style="color: deeppink;">Yuliyuli</h2>
        </div>
        <router-link style="color: grey; text-decoration-line: none;margin-left: -80px;" to="/"><i class="el-icon-arrow-left"></i>首页</router-link>
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
          <el-avatar v-if="isLoggedIn" :src="userAvatar" alt="用户" @click="handleAvatarClick" style="cursor: pointer;" />
          <div class="user-info">
            <el-button v-if="isLoggedIn" type="default" style="background-color: #f0f0f0; color: #333; border-radius: 10px; border: none;
              margin-left: 10px; width: 80px;" @click="handleLogout">退出登录</el-button>
            <el-button v-else type="primary" style="background-color: deeppink; color: white; border-radius: 10px; border: none;
              margin-left: 10px; width: 80px;" @click="handleLogin">登录</el-button>
          </div>
        </div>
        <div class="user-actions">
          <button style="background-color: deepskyblue; color: white; 
            border-radius: 10px; width: 80px; margin-left: -130px;" class="upload-btn" @click="router.push('/upload')">投稿</button>
        </div>
      </div>
    </header>

    <!-- 投稿内容区 -->
    <main class="upload-content">
      <div class="upload-header">
        <h1>投稿视频</h1>
      </div>

      <!-- 视频上传提示 -->
      <div class="upload-tips">
        <div class="tip-item">
          <div class="tip-icon">
            <i class="el-icon-document"></i>
          </div>
          <div class="tip-content">
            <h3>视频大小</h3>
            <p>视频大小16G以内,时长10小时以内</p>
            <p>粉丝数 ≥ 1000,可自动解锁64G超大文件</p>
          </div>
        </div>
        <div class="tip-item">
          <div class="tip-icon">
            <i class="el-icon-video-camera"></i>
          </div>
          <div class="tip-content">
            <h3>视频格式</h3>
            <p>推荐上传MP4/MOV/MKV 格式，转码更快、过审更顺利</p>
          </div>
        </div>
        <div class="tip-item">
          <div class="tip-icon">
            <i class="el-icon-picture"></i>
          </div>
          <div class="tip-content">
            <h3>视频分辨率</h3>
            <p>推荐分辨率：1080P、4K，高分辨率更清晰流畅</p>
          </div>
        </div>
      </div>

      <!-- 上传区域 -->
      <div class="upload-area">
        <input type="file" ref="fileInput" accept="video/*" style="display: none;" @change="handleFileSelect" />
        <div class="upload-placeholder" @click="triggerFileSelect" @dragover.prevent @drop.prevent="handleDrop">
          <div class="upload-icon">
            <i class="el-icon-upload"></i>
          </div>
          <p class="upload-text">点击上传或将视频拖拽到此区域</p>
          <el-button type="primary" size="large" class="upload-button">上传视频</el-button>
          
          <!-- 选中的文件信息 -->
          <div v-if="selectedFile" class="selected-file">
            <el-icon class="file-icon"><i class="el-icon-video-camera"></i></el-icon>
            <div class="file-info">
              <p class="file-name">{{ selectedFile.name }}</p>
              <p class="file-size">{{ formatFileSize(selectedFile.size) }}</p>
            </div>
            <el-button type="text" @click="clearFile" class="clear-btn">
              <i class="el-icon-close"></i>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 视频信息表单 -->
      <div v-if="selectedFile" class="video-form">
        <!-- 封面设置 -->
        <div class="form-section">
          <h3 class="section-title">
            <i class="el-icon-picture"></i> 封面
          </h3>
          <div class="cover-section">
            <div class="main-cover">
              <input type="file" ref="coverInput" accept="image/*" style="display: none;" @change="handleCoverSelect" />
              <div class="cover-upload-area" @click="triggerCoverSelect">
                <div v-if="coverImage" class="cover-preview-container">
                  <img :src="coverImage" alt="封面预览" class="cover-preview" />
                  <div class="cover-overlay">
                    <span>更换封面</span>
                  </div>
                </div>
                <div v-else class="cover-placeholder">
                  <div class="plus-icon">+</div>
                  <p>点击上传封面</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 标题 -->
        <div class="form-section">
          <h3 class="section-title">
            <i class="el-icon-edit"></i> 标题
          </h3>
          <div class="title-input">
            <el-input 
              v-model="videoForm.title" 
              placeholder="请输入视频标题" 
              maxlength="80"
              show-word-limit
            />
          </div>
        </div>

        <!-- 分区 -->
        <div class="form-section">
          <h3 class="section-title">
            <i class="el-icon-folder"></i> 分区
          </h3>
          <div class="partition-select">
            <el-select v-model="videoForm.partition" placeholder="请选择分区" value-key="id">
              <el-option label="番剧" :value="{ id: 1, name: '番剧' }" />
              <el-option label="综艺" :value="{ id: 2, name: '综艺' }" />
              <el-option label="科技" :value="{ id: 3, name: '科技' }" />
              <el-option label="生活" :value="{ id: 4, name: '生活' }" />
              <el-option label="音乐" :value="{ id: 5, name: '音乐' }" />
              <el-option label="舞蹈" :value="{ id: 6, name: '舞蹈' }" />
              <el-option label="美食" :value="{ id: 7, name: '美食' }" />
              <el-option label="汽车" :value="{ id: 8, name: '汽车' }" />
              <el-option label="体育" :value="{ id: 9, name: '体育' }" />
              <el-option label="游戏" :value="{ id: 10, name: '游戏' }" />
            </el-select>
          </div>
        </div>

        <!-- 简介 -->
        <div class="form-section">
          <h3 class="section-title">
            <i class="el-icon-document"></i> 简介
          </h3>
          <div class="intro-input">
            <el-input 
              v-model="videoForm.intro" 
              type="textarea" 
              placeholder="填写更全面的相关信息，让更多的人能找到你的视频吧" 
              :rows="5"
              maxlength="2000"
              show-word-limit
            />
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="form-actions">
          <el-button type="primary" size="large" class="submit-btn" @click="publishVideo" :loading="isPublishing">
            {{ isPublishing ? '发布中...' : '发布视频' }}
          </el-button>
        </div>
      </div>
    </main>

    <!-- 版权footer -->
    <footer class="footer">
      <div class="footer-content">
        <p>&copy; 2026-Yuliyuli.有任何问题请联系我们:<span style="color: deepskyblue;">1913760871@qq.com</span></p>
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
                <p>请使用 yuliyuli 客户端扫码登录<br>或扫码下载APP</p>
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
                  <h8 style="font-size: 12px; margin-left: -10px; margin-top: 10px;">微信登录</h8>
                  <button class="login-icon weibo"></button>
                  <h8 style="font-size: 12px; margin-left: -10px; margin-top: 10px;">微博登录</h8>
                  <button class="login-icon qq"></button>
                  <h8 style="font-size: 12px; margin-left: -10px; margin-top: 10px;">QQ登录</h8>
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
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const router = useRouter();
const defaultAvatar = '/static/images/202304061680747832129368.jpg';

// 文件上传相关
const fileInput = ref<HTMLInputElement | null>(null);
const selectedFile = ref<File | null>(null);

// 封面上传相关
const coverInput = ref<HTMLInputElement | null>(null);
const coverImage = ref<string | null>(null);

// 发布状态
const isPublishing = ref<boolean>(false);

// 视频表单数据
const videoForm = ref({
  title: '',
  type: '自制',
  partition: { id: 1, name: '番剧' },
  tags: '',
  intro: '',
  schedule: false,
  collection: ''
});

// 登录状态
const token = ref<string>(localStorage.getItem('token') || '');
const username = ref<string>(localStorage.getItem('username') || '');
const userAvatar = ref<string>(localStorage.getItem('userAvatar') || '');
const isLoggedIn = ref<boolean>(!!token.value);

// 搜索相关
const searchQuery = ref<string>('');
const showSearchDropdown = ref<boolean>(false);
const searchSuggestions = ref<string[]>([]);
const hotSearchKeywords = ref<Array<{keyword: string, hot?: string}>>([]);

// 登录模态框
const showLoginModal = ref<boolean>(false);
const activeMode = ref<'login' | 'register'>('login');
const activeLoginTab = ref<'password' | 'sms'>('password');
const loginForm = ref({
  account: '',
  password: '',
  phone: '',
  code: ''
});

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
      password: loginForm.value.password
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
          saveLoginInfo(tokenValue, userData.username || userData.nickname || '用户', userData.avatar || defaultAvatar, userIdValue.toString());
          
          showLoginModal.value = false;
        } else {
          console.error('登录失败: 响应数据结构不正确');
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

// 注册提交
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
    const response = await axios.post('/api/user/register', {
      phone: loginForm.value.phone,
      password: loginForm.value.password
    }, {
      params: {
        code: loginForm.value.code, // code作为查询参数
        username: loginForm.value.phone // 使用手机号作为用户名
      }
    });
    
    console.log('注册响应:', response);
    
    if (response.data) {
      if (response.data.code === 200) {
        ElMessage.success('注册成功，请登录');
        console.log('注册成功');
        // 清空注册表单
        loginForm.value.phone = '';
        loginForm.value.code = '';
        loginForm.value.password = '';
        // 注册成功后切换到登录模式
        activeMode.value = 'login';
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

// 获取验证码
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
      phone: loginForm.value.phone
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

// 文件上传相关方法
const triggerFileSelect = () => {
  fileInput.value?.click();
};

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    selectedFile.value = target.files[0];
  }
};

const handleDrop = (event: DragEvent) => {
  if (event.dataTransfer && event.dataTransfer.files.length > 0) {
    selectedFile.value = event.dataTransfer.files[0];
  }
};

const clearFile = () => {
  selectedFile.value = null;
  if (fileInput.value) {
    fileInput.value.value = '';
  }
};

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

// 封面上传相关方法
const triggerCoverSelect = () => {
  coverInput.value?.click();
};

const handleCoverSelect = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    const file = target.files[0];
    const reader = new FileReader();
    reader.onload = (e) => {
      coverImage.value = e.target?.result as string;
    };
    reader.readAsDataURL(file);
  }
};

// 发布视频
const publishVideo = async () => {
  // 检查登录状态
  if (!isLoggedIn.value) {
    showLoginModal.value = true;
    return;
  }

  // 验证表单
  if (!videoForm.value.title.trim()) {
    ElMessage.warning('请输入视频标题');
    return;
  }

  if (!selectedFile.value) {
    ElMessage.warning('请选择要上传的视频文件');
    return;
  }

  try {
    isPublishing.value = true;

    // 创建FormData
    const formData = new FormData();
    formData.append('file', selectedFile.value);
    formData.append('video.title', videoForm.value.title);
    formData.append('video.type', videoForm.value.partition.id.toString());
    
    // 如果有封面，添加封面文件
    if (coverInput.value?.files && coverInput.value.files.length > 0) {
      formData.append('video.cover', coverInput.value.files[0]);
    }
    
    if (videoForm.value.intro) {
      formData.append('video.intro', videoForm.value.intro);
    }

    // 调用后端接口
    const response = await axios.post('/api/video/delivery', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token.value}`
      }
    });

    if (response.data.code === 200) {
      ElMessage.success('视频上传成功！');
      // 重置表单
      selectedFile.value = null;
      coverImage.value = null;
      videoForm.value = {
        title: '',
        type: '自制',
        partition: { id: 1, name: '番剧' },
        tags: '',
        intro: '',
        schedule: false,
        collection: ''
      };
      if (fileInput.value) {
        fileInput.value.value = '';
      }
      if (coverInput.value) {
        coverInput.value.value = '';
      }
    } else {
      ElMessage.error('视频上传失败：' + (response.data.message || response.data.msg || '未知错误'));
    }
  } catch (error) {
    console.error('发布视频失败:', error);
    ElMessage.error('视频上传失败，请稍后重试');
  } finally {
    isPublishing.value = false;
  }
};

onMounted(() => {
  initUserInfo();
});

// 获取热门搜索（前十热门视频）
const fetchHotSearch = async () => {
  try {
    const token = localStorage.getItem('token');
    console.log('开始获取热门视频...');
    
    // 构建请求配置
    const config: any = {
      params: {
        keyword: searchQuery.value.trim() || ''
      }
    };
    
    // 只有当token存在时才添加Authorization头
    if (token) {
      config.headers = {
        'Authorization': `Bearer ${token}`
      };
    }
    
    const response = await axios.get('/api/search/topTenVideo', config);
    if (response.data && response.data.code === 200 && response.data.data && response.data.data.length > 0) {
      // 将热门视频数据转换为搜索关键词格式
      hotSearchKeywords.value = response.data.data.map((video: any, index: number) => {
        // 从SearchVideoVO中提取标题
        const title = video.videoDocuments && video.videoDocuments[0] 
          ? video.videoDocuments[0].title 
          : video.title || `热门视频 ${index + 1}`;
        return {
          keyword: title,
          hot: index < 3 ? '热' : index < 5 ? 'NEW' : index < 8 ? '新' : undefined
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
        params: { keyword: searchQuery.value }
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
    query: { keyword: suggestion }
  });
};

const selectHotKeyword = (keyword: string) => {
  searchQuery.value = keyword;
  showSearchDropdown.value = false;
  // 跳转到搜索结果页面
  router.push({
    path: '/search',
    query: { keyword: keyword }
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
      query: { keyword: searchQuery.value }
    });
  }
};
</script>

<style scoped>
.upload-page {
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

.hot-search-rank:nth-child(-n+3) {
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

/* 投稿内容区 */
.upload-content {
  flex: 1;
  width: 1200px;
  margin: 0 auto;
  padding: 20px 0;
}

.upload-header {
  margin-bottom: 30px;
}

.upload-header h1 {
  font-size: 24px;
  color: #333;
}

/* 上传提示 */
.upload-tips {
  display: flex;
  justify-content: space-between;
  margin-bottom: 30px;
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.tip-item {
  flex: 1;
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 0 20px;
  border-right: 1px solid #eee;
}

.tip-item:last-child {
  border-right: none;
}

.tip-icon {
  width: 40px;
  height: 40px;
  background-color: #e6f7ff;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #1890ff;
  font-size: 20px;
  flex-shrink: 0;
}

.tip-content h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
}

.tip-content p {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 4px;
}

/* 上传区域 */
.upload-area {
  background-color: #fff;
  padding: 60px 0;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: center;
  align-items: center;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 40px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  width: 80%;
  text-align: center;
}

.upload-icon {
  font-size: 60px;
  color: #d9d9d9;
  margin-bottom: 10px;
}

.upload-text {
  font-size: 16px;
  color: #999;
  margin-bottom: 10px;
}

.upload-button {
  width: 200px;
  height: 40px;
  font-size: 16px;
}

/* 选中的文件信息 */
.selected-file {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
  padding: 15px;
  background-color: #f6f6f6;
  border-radius: 8px;
  width: 100%;
  max-width: 500px;
}

.file-icon {
  font-size: 32px;
  color: #1890ff;
  flex-shrink: 0;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  color: #333;
  margin: 0 0 5px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.clear-btn {
  color: #999;
  flex-shrink: 0;
}

/* 视频表单样式 */
.video-form {
  margin-top: 40px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.form-section {
  margin-bottom: 30px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
  padding-left: 5px;
  border-left: 3px solid #1890ff;
}

/* 封面设置 */
.cover-section {
  margin-top: 10px;
}

.main-cover {
  position: relative;
  display: inline-block;
  margin-bottom: 15px;
}

.cover-upload-area {
  width: 320px;
  height: 180px;
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
  border: 2px dashed #d9d9d9;
  background-color: #fafafa;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.cover-placeholder:hover {
  border-color: #1890ff;
  background-color: #f0f9ff;
}

.plus-icon {
  font-size: 48px;
  color: #d9d9d9;
  margin-bottom: 10px;
  transition: color 0.3s;
}

.cover-placeholder:hover .plus-icon {
  color: #1890ff;
}

.cover-placeholder p {
  font-size: 14px;
  color: #999;
  margin: 0;
  transition: color 0.3s;
}

.cover-placeholder:hover p {
  color: #1890ff;
}

.cover-preview-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  opacity: 0;
  transition: opacity 0.3s;
}

.cover-preview-container:hover .cover-overlay {
  opacity: 1;
}

.cover-tips {
  margin-bottom: 15px;
  font-size: 14px;
  color: #666;
}

.recommended-covers {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.cover-item {
  position: relative;
  width: 120px;
  height: 68px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
}

.cover-item.active {
  border-color: #1890ff;
}

.cover-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-item .el-icon-check {
  position: absolute;
  bottom: 5px;
  right: 5px;
  color: #1890ff;
  font-size: 16px;
  background-color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 标题输入 */
.title-input {
  width: 100%;
  max-width: 600px;
}

/* 类型选择 */
.type-select {
  margin-top: 5px;
}

/* 分区选择 */
.partition-select {
  width: 200px;
}

/* 标签输入 */
.tag-input {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  width: 100%;
  max-width: 600px;
}

.tag-input .el-input {
  flex: 1;
}

.tag-count {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}

.recommended-tags {
  margin-top: 10px;
}

.recommended-label {
  font-size: 14px;
  color: #666;
  margin-right: 10px;
}

/* 话题 */
.topics {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
  align-items: center;
}

.topic-status {
  font-size: 10px;
  color: #999;
  margin-left: 5px;
}

.more-topics {
  font-size: 12px;
  color: #1890ff;
  margin-left: 10px;
}

/* 简介输入 */
.intro-input {
  width: 100%;
  max-width: 800px;
}

/* 定时发布 */
.schedule-setting {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 5px;
}

.schedule-tip {
  font-size: 12px;
  color: #999;
  flex: 1;
}

/* 合集选择 */
.collection-select {
  width: 200px;
}

/* 提交按钮 */
.form-actions {
  display: flex;
  gap: 20px;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.submit-btn {
  width: 150px;
  height: 40px;
}

.draft-btn {
  width: 150px;
  height: 40px;
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
  background-color: #07C160;
  color: white;
}

.login-icon.weibo {
  background-color: #E6162D;
  color: white;
}

.login-icon.qq {
  background-color: #12B7F5;
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
</style>