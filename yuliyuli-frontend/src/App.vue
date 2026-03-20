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

body {
  font-family: 'Arial', sans-serif;
  background-color: #f5f5f5;
}

#app {
  width: 100%;
  min-height: 100vh;
}
</style>