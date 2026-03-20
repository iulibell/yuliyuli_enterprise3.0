<template>
  <div class="profile-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo">
          <h2 style="color: deeppink">Yuliyuli</h2>
        </div>
        <router-link style="color: grey; text-decoration-line: none; margin-left: -80px" to="/">
          <i class="el-icon-arrow-left"></i>首页
        </router-link>
        <div class="search-box">
          <input type="text" placeholder="搜索视频、番剧、用户" />
          <button class="search-btn">搜索</button>
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

    <!-- 个人中心侧边栏 -->
    <div class="profile-container">
      <div class="sidebar">
        <h3>个人中心</h3>
        <ul class="menu">
          <li class="menu-item" :class="{ active: currentPath === '/home' }">
            <router-link to="/home" class="menu-link">
              <i class="el-icon-s-home"></i> 首页
            </router-link>
          </li>
          <li class="menu-item" :class="{ active: currentPath === '/info' }">
            <router-link to="/info" class="menu-link">
              <i class="el-icon-user"></i> 我的信息
            </router-link>
          </li>
          <li class="menu-item" :class="{ active: currentPath === '/profile' }">
            <router-link to="/profile" class="menu-link">
              <i class="el-icon-camera"></i> 我的头像
            </router-link>
          </li>
          <li class="menu-item" :class="{ active: currentPath === '/security' }">
            <router-link to="/security" class="menu-link">
              <i class="el-icon-lock"></i> 账号安全
            </router-link>
          </li>
        </ul>
      </div>

      <!-- 主内容区 -->
      <div class="main-content">
        <!-- 我的头像 -->
        <div v-if="currentPath === '/profile'" class="content-section">
          <div class="content-header">
            <h2>我的头像 <span class="sub-title">> 更换头像</span></h2>
          </div>

          <div class="avatar-section">
            <div class="avatar-upload">
              <div class="upload-options">
                <input
                  type="file"
                  ref="fileInput"
                  accept="image/*"
                  style="display: none"
                  @change="handleFileChange"
                />
                <el-button type="default" class="upload-btn" @click="triggerFileInput">
                  <i class="el-icon-picture-outline"></i> 选择本地图片
                </el-button>
              </div>

              <div class="current-avatar">
                <el-avatar :src="previewAvatar || userInfo.avatar || defaultAvatar" :size="120" />
                <p>当前头像</p>
              </div>
            </div>

            <div class="upload-tips">
              <p>请选择图片上传：大小180 * 180像素支持JPG、PNG等格式，图片需小于2M</p>
            </div>

            <div class="action-buttons">
              <el-button
                type="primary"
                class="update-btn"
                @click="handleAvatarUpdate"
                :loading="isUploading"
              >
                {{ isUploading ? '上传中...' : '更新' }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 我的信息 -->
        <div v-else-if="currentPath === '/info'" class="content-section">
          <div class="content-header">
            <h2>我的信息</h2>
          </div>
          <div class="info-section">
            <form class="info-form">
              <div class="form-item">
                <label class="form-label">我的签名:</label>
                <div class="form-input-group">
                  <el-input
                    v-model="userProfile.signature"
                    type="textarea"
                    placeholder="设置您的签名- ( ´▽` )ﾉロ"
                    class="form-textarea"
                    :rows="3"
                  />
                </div>
              </div>

              <div class="form-item">
                <label class="form-label">性别:</label>
                <div class="form-input-group">
                  <el-radio-group v-model="userProfile.gender">
                    <el-radio label="男">男</el-radio>
                    <el-radio label="女">女</el-radio>
                    <el-radio label="保密">保密</el-radio>
                  </el-radio-group>
                </div>
              </div>

              <div class="form-item">
                <label class="form-label">出生日期:</label>
                <div class="form-input-group">
                  <el-date-picker
                    v-model="userProfile.birthday"
                    type="date"
                    placeholder="选择日期"
                    class="form-date"
                  />
                </div>
              </div>

              <div class="form-actions">
                <el-button type="primary" class="save-btn" @click="handleSave" :loading="isSaving">
                  {{ isSaving ? '保存中...' : '保存' }}
                </el-button>
              </div>
            </form>
          </div>
        </div>

        <!-- 账号安全 -->
        <div v-else-if="currentPath === '/security'" class="content-section">
          <div class="content-header">
            <h2>账号安全</h2>
          </div>
          <div class="security-section">
            <p style="text-align: center">账号安全管理功能开发中...</p>
          </div>
        </div>

        <!-- 个人中心首页 -->
        <div v-else-if="currentPath === '/home'" class="content-section">
          <div class="content-header">
            <h2>我的主页</h2>
          </div>
          <div class="home-section">
            <p style="text-align: center">个人中心首页功能开发中...</p>
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

  const route = useRoute();
  const router = useRouter();
  const defaultAvatar = '/static/images/202304061680747832129368.jpg';

  // 当前路由路径
  const currentPath = ref(route.path);

  // 登录状态
  const token = ref<string>(localStorage.getItem('token') || '');
  const username = ref<string>(localStorage.getItem('username') || '');
  const userAvatar = ref<string>(localStorage.getItem('userAvatar') || '');
  const isLoggedIn = ref<boolean>(!!token.value);

  // 用户信息
  const userInfo = ref({
    avatar: userAvatar.value,
    username: username.value,
    userId: localStorage.getItem('userId') || '',
  });

  // 用户个人资料
  const userProfile = ref({
    nickname: '',
    username: username.value,
    signature: '',
    gender: '保密',
    birthday: null,
  });

  // 保存状态
  const isSaving = ref<boolean>(false);

  // 头像上传相关
  const fileInput = ref<HTMLInputElement | null>(null);
  const previewAvatar = ref<string>('');
  const selectedFile = ref<File | null>(null);
  const isUploading = ref<boolean>(false);

  // 监听路由变化
  watch(
    () => route.path,
    newPath => {
      currentPath.value = newPath;
    }
  );

  // 从路由参数中获取用户信息
  onMounted(() => {
    const avatar = route.query.avatar as string;
    const userId = route.query.userId as string;
    const name = route.query.name as string;

    if (avatar) {
      userInfo.value.avatar = avatar;
      userAvatar.value = avatar;
    }
    if (userId) {
      userInfo.value.userId = userId;
      // 如果从路由参数中获取到userId，也更新localStorage，确保保存时使用正确的userId
      localStorage.setItem('userId', userId);
    }
    if (name) {
      userInfo.value.username = name;
      username.value = name;
      localStorage.setItem('username', name);
    }
  });

  // 处理头像点击
  const handleAvatarClick = () => {
    if (isLoggedIn.value) {
      const currentUserId = localStorage.getItem('userId') || '';
      const username = localStorage.getItem('username') || '';
      const avatar = localStorage.getItem('userAvatar') || '';
      if (currentUserId) {
        router.push(`/author/${currentUserId}/${username}?avatar=${encodeURIComponent(avatar)}`);
      }
    }
  };

  // 处理登录
  const handleLogin = () => {
    // 登录逻辑
  };

  // 处理退出登录
  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('userAvatar');
    localStorage.removeItem('userId');
    isLoggedIn.value = false;
    router.push('/');
  };

  // 触发文件选择
  const triggerFileInput = () => {
    fileInput.value?.click();
  };

  // 处理文件选择
  const handleFileChange = (event: Event) => {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files[0]) {
      const file = target.files[0];

      // 检查文件大小
      if (file.size > 2 * 1024 * 1024) {
        ElMessage.error('图片大小不能超过2M');
        return;
      }

      // 检查文件类型
      if (!file.type.startsWith('image/')) {
        ElMessage.error('请选择图片文件');
        return;
      }

      selectedFile.value = file;

      // 生成预览
      const reader = new FileReader();
      reader.onload = e => {
        if (e.target?.result) {
          previewAvatar.value = e.target.result as string;
        }
      };
      reader.readAsDataURL(file);
    }
  };

  // 上传头像
  const handleAvatarUpdate = async () => {
    if (!selectedFile.value) {
      ElMessage.warning('请先选择图片');
      return;
    }

    try {
      isUploading.value = true;

      const formData = new FormData();
      formData.append('avatar', selectedFile.value);
      formData.append('userId', localStorage.getItem('userId') || '');

      const response = await axios.post('/api/user/modifyAvatar', formData, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'multipart/form-data',
        },
      });

      console.log('头像上传响应:', response);

      if (response.data && response.data.code === 200) {
        const avatarUrl = response.data.data;
        userInfo.value.avatar = avatarUrl;
        userAvatar.value = avatarUrl;
        localStorage.setItem('userAvatar', avatarUrl);
        ElMessage.success('头像更新成功！');
      } else {
        ElMessage.error('头像上传失败：' + (response.data?.msg || '未知错误'));
      }
    } catch (error: any) {
      console.error('头像上传失败:', error);
      if (error.response) {
        ElMessage.error('头像上传失败：' + (error.response.data?.msg || '服务器错误'));
      } else if (error.request) {
        ElMessage.error('头像上传失败：网络错误，请检查网络连接');
      } else {
        ElMessage.error('头像上传失败：' + (error.message || '未知错误'));
      }
    } finally {
      isUploading.value = false;
    }
  };

  // 保存个人信息
  const handleSave = async () => {
    try {
      isSaving.value = true;

      // 转换性别为数字
      let genderNum = 0;
      if (userProfile.value.gender === '男') {
        genderNum = 1;
      } else if (userProfile.value.gender === '女') {
        genderNum = 2;
      }

      // 准备请求参数
      const userInfoData = {
        userId: localStorage.getItem('userId'),
        gender: genderNum,
        birthday: userProfile.value.birthday,
        sign: userProfile.value.signature,
      };

      console.log('保存请求参数:', userInfoData);

      // 调用后端接口
      const response = await axios.post('/api/user/modifyInfo', userInfoData, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
      });

      console.log('保存响应:', response);
      console.log('响应数据:', response.data);

      if (response.data) {
        // 检查是否有code字段
        if (response.data.code === 200) {
          ElMessage.success('保存成功！');
        } else if (response.data.msg) {
          ElMessage.error('保存失败：' + response.data.msg);
        } else {
          // 后端返回了数据但没有预期的结构
          ElMessage.success('保存成功！');
          console.log('后端返回数据:', response.data);
        }
      } else {
        ElMessage.error('保存失败：响应数据格式错误');
      }
    } catch (error: any) {
      console.error('保存失败:', error);
      if (error.response) {
        // 服务器返回错误
        console.error('响应错误:', error.response);
        ElMessage.error('保存失败：' + (error.response.data?.msg || '服务器错误'));
      } else if (error.request) {
        // 请求已发送但没有收到响应
        console.error('请求错误:', error.request);
        ElMessage.error('保存失败：网络错误，请检查网络连接');
      } else {
        // 其他错误
        ElMessage.error('保存失败：' + (error.message || '未知错误'));
      }
    } finally {
      isSaving.value = false;
    }
  };
</script>

<style scoped>
  .upload-btn {
    background-color: #f0f0f0;
    border: 1px solid #ddd;
    border-radius: 4px;
    padding: 6px 12px;
    margin-right: 15px;
    cursor: pointer;
    font-size: 14px;
  }

  .user-actions {
    display: flex;
    align-items: center;
  }

  .profile-page {
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
  }

  .search-box {
    flex: 1;
    max-width: 400px;
    margin: 0 20px;
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

  .user-icon {
    display: flex;
    align-items: center;
  }

  .user-info {
    margin-left: 10px;
  }

  /* 个人中心容器 */
  .profile-container {
    display: flex;
    width: 1200px;
    margin: 20px auto;
    min-height: 600px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  /* 侧边栏 */
  .sidebar {
    width: 200px;
    padding: 20px 0;
    border-right: 1px solid #eee;
  }

  .sidebar h3 {
    padding: 0 20px;
    margin: 0 0 20px 0;
    font-size: 16px;
    font-weight: bold;
    color: #333;
  }

  .menu {
    list-style: none;
    padding: 0;
    margin: 0;
  }

  .menu-item {
    margin-bottom: 5px;
  }

  .menu-link {
    display: flex;
    align-items: center;
    padding: 12px 20px;
    color: #666;
    text-decoration: none;
    transition: all 0.3s;
  }

  .menu-link:hover {
    background-color: #f0f0f0;
    color: #00a1d6;
  }

  .menu-item.active .menu-link {
    background-color: #e6f7ff;
    color: #00a1d6;
    border-left: 3px solid #00a1d6;
  }

  .menu-link i {
    margin-right: 10px;
    font-size: 16px;
  }

  /* 主内容区 */
  .main-content {
    flex: 1;
    padding: 20px;
  }

  .content-section {
    width: 100%;
  }

  .content-header {
    margin-bottom: 30px;
    padding-bottom: 15px;
    border-bottom: 1px solid #eee;
  }

  .content-header h2 {
    margin: 0;
    font-size: 18px;
    font-weight: bold;
    color: #333;
  }

  .sub-title {
    font-size: 14px;
    color: #999;
    font-weight: normal;
    margin-left: 10px;
  }

  /* 头像上传区 */
  .avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;
  }

  .avatar-upload {
    display: flex;
    align-items: center;
    margin-bottom: 30px;
    width: 100%;
    max-width: 600px;
  }

  .upload-options {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 15px;
    margin-right: 40px;
  }

  .upload-btn {
    width: 100%;
    height: 80px;
    border: 2px dashed #ddd;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s;
  }

  .upload-btn:hover {
    border-color: #00a1d6;
    background-color: #f0f9ff;
  }

  .upload-btn i {
    margin-right: 8px;
    font-size: 20px;
  }

  .current-avatar {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .current-avatar p {
    margin-top: 10px;
    font-size: 14px;
    color: #666;
  }

  /* 上传提示 */
  .upload-tips {
    margin-bottom: 30px;
    text-align: center;
    color: #999;
    font-size: 14px;
  }

  /* 操作按钮 */
  .action-buttons {
    display: flex;
    justify-content: center;
  }

  .update-btn {
    width: 120px;
    height: 40px;
    border-radius: 20px;
    background-color: #00a1d6;
    border: none;
    color: white;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.3s;
  }

  .update-btn:hover {
    background-color: #008ec4;
  }

  /* 信息和安全区域 */
  .security-section,
  .home-section {
    padding: 40px 0;
    text-align: center;
    color: #666;
    font-size: 16px;
  }

  /* 我的信息表单 */
  .info-form {
    max-width: 600px;
    margin: 0 auto;
  }

  .form-item {
    margin-bottom: 25px;
    display: flex;
    align-items: flex-start;
  }

  .form-label {
    width: 100px;
    font-size: 14px;
    color: #333;
    padding-top: 8px;
    flex-shrink: 0;
  }

  .form-input-group {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .form-input {
    width: 300px;
    height: 36px;
  }

  .form-static {
    font-size: 14px;
    color: #333;
    padding: 8px 0;
  }

  .form-textarea {
    width: 100%;
    min-height: 80px;
    resize: vertical;
  }

  .form-date {
    width: 300px;
  }

  .form-tip {
    font-size: 12px;
    color: #999;
    margin-top: 5px;
  }

  .form-actions {
    display: flex;
    justify-content: center;
    margin-top: 40px;
    padding-top: 20px;
    border-top: 1px solid #eee;
  }

  .save-btn {
    width: 120px;
    height: 40px;
    border-radius: 4px;
    background-color: #00a1d6;
    border: none;
    color: white;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.3s;
  }

  .save-btn:hover {
    background-color: #008ec4;
  }

  /* 单选按钮样式 */
  .el-radio-group {
    display: flex;
    gap: 20px;
  }

  .el-radio {
    font-size: 14px;
  }

  .el-radio__input.is-checked .el-radio__inner {
    background-color: #00a1d6;
    border-color: #00a1d6;
  }

  .el-radio__input.is-checked + .el-radio__label {
    color: #00a1d6;
  }
</style>
