<template>
  <div id="app">
    <router-view />
    <LoadingSpinner :visible="loading" />
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import LoadingSpinner from './components/LoadingSpinner.vue';

  const router = useRouter();
  const loading = ref<boolean>(false);

  onMounted(() => {
    // 监听路由跳转开始
    router.beforeEach((_to, _from, next) => {
      loading.value = true;
      next();
    });

    // 监听路由跳转结束
    router.afterEach(() => {
      // 延迟一点时间，确保加载动画有足够的显示时间
      setTimeout(() => {
        loading.value = false;
      }, 300);
    });
  });
</script>

<style>
  * {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
  }

  :root {
    --bili-primary: #00aeec;
    --bili-primary-hover: #2bbff3;
    --bili-text-main: #18191c;
    --bili-text-sub: #61666d;
    --bili-border: #e3e5e7;
    --bili-bg: #f6f7f8;
    --bili-card-bg: #ffffff;
  }

  body {
    font-family: 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
    color: var(--bili-text-main);
    background: radial-gradient(circle at top, #f2f9ff 0%, #f6f7f8 28%, #f6f7f8 100%);
  }

  #app {
    width: 100%;
    min-height: 100vh;
  }

  a {
    color: inherit;
  }

  /* 顶栏：避免负 margin 导致 Logo 与「首页」重叠；头像与按钮同一行 */
  #app header .user-icon {
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 10px;
    flex-shrink: 0;
  }

  #app header .user-actions {
    display: flex;
    align-items: center;
    flex-shrink: 0;
  }

  #app header .upload-btn {
    margin-left: 0 !important;
    width: 80px !important;
    height: 34px !important;
    padding: 0 14px !important;
    border: none !important;
    box-shadow: none !important;
    outline: none !important;
    border-radius: 999px !important;
  }

  #app header .upload-btn:hover,
  #app header .upload-btn:focus,
  #app header .upload-btn:active {
    border: none !important;
    box-shadow: none !important;
    outline: none !important;
  }

  .header-back-link {
    color: var(--bili-text-sub);
    text-decoration: none;
    font-size: 14px;
    white-space: nowrap;
    flex-shrink: 0;
  }

  .header-back-link:hover {
    color: var(--bili-primary);
  }

  /* 统一非主页顶部风格到主页样式 */
  #app .header {
    position: sticky !important;
    top: 0 !important;
    z-index: 120 !important;
    backdrop-filter: blur(12px) !important;
    background: rgba(255, 255, 255, 0.92) !important;
    border-bottom: 1px solid #e8eaed !important;
    box-shadow: 0 1px 0 rgba(24, 25, 28, 0.04) !important;
  }

  #app .header-content {
    width: min(1280px, 95%) !important;
    margin: 0 auto !important;
    height: 68px !important;
    display: flex !important;
    align-items: center !important;
    justify-content: flex-start !important;
    gap: 16px !important;
    padding: 0 !important;
  }

  #app .logo h2 {
    margin: 0 !important;
    color: var(--bili-primary) !important;
    letter-spacing: 0.5px !important;
    font-size: 28px !important;
    font-weight: 700 !important;
  }

  #app .search-box {
    flex: 1 !important;
    min-width: 0 !important;
    max-width: 560px !important;
    margin: 0 !important;
    position: relative !important;
  }

  #app .user-info :deep(.el-button),
  #app .upload-btn {
    border-radius: 999px !important;
    height: 34px !important;
    padding: 0 14px !important;
    font-size: 13px !important;
    line-height: 34px !important;
  }

  #app .user-info .el-button--default {
    background: #f6f7f8 !important;
    color: #18191c !important;
    border: 1px solid #e3e5e7 !important;
  }

  #app .user-info .el-button--primary {
    background: var(--bili-primary) !important;
    color: #fff !important;
    border: 1px solid var(--bili-primary) !important;
  }

  #app .upload-btn {
    background: var(--bili-primary) !important;
    color: #fff !important;
    border: 1px solid var(--bili-primary) !important;
  }

  /* 全站统一搜索风格（参考图中B站样式） */
  #app header .search-box {
    position: relative;
  }

  #app header .search-input-container {
    position: relative;
  }

  #app header .search-box input,
  #app header .search-input-container input {
    width: 100% !important;
    height: 40px !important;
    border: 1px solid #e3e5e7 !important;
    border-radius: 10px !important;
    background: #f1f2f3 !important;
    color: #18191c !important;
    padding: 0 44px 0 14px !important;
    font-size: 15px !important;
    line-height: 40px !important;
  }

  #app header .search-box input::placeholder,
  #app header .search-input-container input::placeholder {
    color: #9499a0;
  }

  #app header .search-box input:focus,
  #app header .search-input-container input:focus {
    outline: none;
    border-color: #cfd4da !important;
    background: #fff !important;
  }

  #app header .search-btn {
    position: absolute !important;
    right: 8px !important;
    top: 50% !important;
    transform: translateY(-50%) !important;
    width: 28px !important;
    height: 28px !important;
    border: none !important;
    border-radius: 50% !important;
    background: transparent !important;
    color: transparent !important;
    font-size: 0 !important;
    cursor: pointer;
    padding: 0 !important;
  }

  #app header .search-btn::before {
    content: '⌕';
    color: #18191c;
    font-size: 20px;
    line-height: 28px;
    display: block;
    text-align: center;
  }

  #app header .search-dropdown {
    position: absolute !important;
    left: 0;
    right: 0;
    top: calc(100% + 8px);
    z-index: 2200 !important;
    background: #fff !important;
    border: 1px solid #eceef1 !important;
    border-radius: 12px !important;
    box-shadow: 0 10px 26px rgba(24, 25, 28, 0.14) !important;
    max-height: 520px;
    overflow-y: auto;
    padding: 8px 0;
  }

  #app header .hot-search,
  #app header .search-suggestions {
    padding: 6px 0 !important;
  }

  #app header .hot-search-title {
    font-size: 18px !important;
    line-height: 26px !important;
    color: #18191c !important;
    font-weight: 600 !important;
    padding: 4px 20px 8px !important;
  }

  #app header .hot-search-list {
    display: flex !important;
    flex-direction: column !important;
    gap: 0 !important;
  }

  #app header .hot-search-item,
  #app header .search-suggestion-item {
    display: flex !important;
    align-items: center !important;
    gap: 10px !important;
    padding: 9px 20px !important;
    border-radius: 0 !important;
    font-size: 16px;
    color: #18191c;
    cursor: pointer;
  }

  #app header .hot-search-item:hover,
  #app header .search-suggestion-item:hover {
    background: #f6f7f8 !important;
  }

  #app header .hot-search-rank {
    width: 20px;
    flex-shrink: 0;
    color: #9499a0;
    font-weight: 500;
    text-align: right;
  }

  #app header .hot-search-text {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  #app header .hot-tag {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 18px;
    height: 18px;
    padding: 0 5px;
    border-radius: 6px;
    font-size: 12px;
    line-height: 1;
    color: #fff;
    background: #fb7299;
    font-weight: 600;
  }

  /* 全局强制统一登录/注册弹窗样式（覆盖各页面scoped旧样式） */
  #app .login-modal-overlay {
    position: fixed !important;
    inset: 0 !important;
    background: rgba(0, 0, 0, 0.48) !important;
    backdrop-filter: blur(4px) !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    z-index: 9999 !important;
  }

  #app .login-modal {
    width: min(860px, 90vw) !important;
    min-height: 520px !important;
    border-radius: 12px !important;
    background: #fff !important;
    box-shadow: 0 18px 48px rgba(0, 0, 0, 0.22) !important;
    overflow: hidden !important;
    position: relative !important;
  }

  #app .login-header {
    height: 56px !important;
    padding: 12px 16px !important;
    border-bottom: 1px solid #f0f1f3 !important;
    background: #fff !important;
    display: flex !important;
    justify-content: flex-end !important;
    align-items: center !important;
  }

  #app .close-btn {
    width: 34px !important;
    height: 34px !important;
    border: none !important;
    border-radius: 50% !important;
    background: transparent !important;
    font-size: 30px !important;
    line-height: 1 !important;
    color: #18191c !important;
    cursor: pointer !important;
  }

  #app .close-btn:hover {
    background: #f5f6f7 !important;
  }

  #app .login-content {
    min-height: calc(520px - 56px) !important;
    display: flex !important;
    flex-direction: column !important;
  }

  #app .login-tabs {
    display: flex !important;
    justify-content: center !important;
    border-bottom: 1px solid #f0f1f3 !important;
  }

  #app .main-tabs {
    display: flex !important;
    gap: 2px !important;
  }

  #app .login-type-tabs {
    display: flex !important;
    justify-content: center !important;
    gap: 22px !important;
    margin: 4px 0 14px !important;
  }

  #app .type-tab-btn {
    border: none !important;
    background: transparent !important;
    color: #61666d !important;
    font-size: 14px !important;
    cursor: pointer !important;
    border-bottom: 2px solid transparent !important;
    padding: 4px 0 !important;
  }

  #app .type-tab-btn.active {
    color: #00aeec !important;
    border-bottom-color: #00aeec !important;
  }

  #app .tab-btn {
    border: none !important;
    background: transparent !important;
    color: #61666d !important;
    font-size: 22px !important;
    font-weight: 500 !important;
    cursor: pointer !important;
    padding: 10px 18px 8px !important;
    border-bottom: 3px solid transparent !important;
  }

  #app .tab-btn.active {
    color: #00aeec !important;
    border-bottom-color: #00aeec !important;
  }

  #app .login-body {
    flex: 1 !important;
    display: flex !important;
    gap: 26px !important;
    padding: 18px 24px 16px !important;
  }

  #app .qr-code-section {
    width: 250px !important;
    flex-shrink: 0 !important;
    border-right: 1px solid #f0f2f4 !important;
    padding-right: 20px !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
  }

  #app .qr-code {
    width: 100%;
    text-align: center !important;
  }

  #app .qr-code::before {
    content: '扫描二维码登录';
    display: block;
    font-size: 18px;
    color: #18191c;
    margin-bottom: 10px;
    font-weight: 500;
  }

  #app .qr-code img {
    width: 158px !important;
    height: 158px !important;
    border: 1px solid #e3e5e7 !important;
    border-radius: 8px !important;
    margin-bottom: 10px !important;
  }

  #app .qr-code p {
    color: #61666d !important;
    font-size: 14px !important;
    line-height: 1.7 !important;
    text-align: center !important;
  }

  #app .form-section {
    flex: 1 !important;
    display: flex !important;
    flex-direction: column !important;
    justify-content: center !important;
  }

  #app .form-item {
    margin-bottom: 14px !important;
  }

  #app .form-item label {
    display: block !important;
    color: #61666d !important;
    font-size: 14px !important;
    margin-bottom: 8px !important;
  }

  #app .form-item input {
    width: 100% !important;
    height: 40px !important;
    border: 1px solid #e3e5e7 !important;
    border-radius: 10px !important;
    padding: 0 14px !important;
    font-size: 14px !important;
  }

  #app .form-item input:focus {
    outline: none !important;
    border-color: #00aeec !important;
  }

  #app .code-input {
    display: flex !important;
    gap: 10px !important;
  }

  #app .get-code-btn {
    height: 40px !important;
    border: 1px solid #e3e5e7 !important;
    border-radius: 8px !important;
    background: #f6f7f8 !important;
    color: #61666d !important;
    padding: 0 12px !important;
    font-size: 13px !important;
    cursor: pointer !important;
  }

  #app .form-actions {
    display: flex !important;
    justify-content: center !important;
    gap: 14px !important;
    margin-top: 8px !important;
  }


  #app .login-btn,
  #app .register-btn {
    width: 188px !important;
    height: 42px !important;
    border-radius: 10px !important;
    font-size: 15px !important;
    border: none !important;
    cursor: pointer !important;
  }

  #app .login-btn {
    background: #00aeec !important;
    color: #fff !important;
  }

  #app .register-btn {
    background: #fff !important;
    color: #18191c !important;
    border: 1px solid #dfe1e4 !important;
  }

  #app .other-login p {
    text-align: center !important;
    color: #9499a0 !important;
    font-size: 13px !important;
    margin-bottom: 12px !important;
  }

  #app .login-icons {
    display: flex !important;
    justify-content: center !important;
    align-items: center !important;
    gap: 12px !important;
  }

  #app .login-icon {
    width: 32px !important;
    height: 32px !important;
    border-radius: 50% !important;
    border: none !important;
  }

  #app .login-agreement p {
    text-align: center !important;
    color: #9499a0 !important;
    font-size: 12px !important;
    line-height: 1.7 !important;
  }

  #app .login-agreement a {
    color: #00aeec !important;
    text-decoration: none !important;
  }

  /* 让 Element Plus 全局消息提示显示在登录遮罩层之上，避免被虚化 */
  .el-message,
  .el-notification,
  .el-message-box__wrapper {
    z-index: 11000 !important;
  }
</style>
