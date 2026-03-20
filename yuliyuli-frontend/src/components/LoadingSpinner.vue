<template>
  <div class="loading-overlay" v-if="visible">
    <div class="loading-spinner">
      <div class="pulse-container">
        <div class="pulse"></div>
        <div class="pulse"></div>
        <div class="pulse"></div>
      </div>
      <p class="loading-text">加载中...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
  defineProps<{
    visible: boolean;
  }>();
</script>

<style scoped>
  .loading-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(255, 255, 255, 0.8);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
  }

  .loading-spinner {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .pulse-container {
    display: flex;
    gap: 10px;
  }

  .pulse {
    width: 15px;
    height: 15px;
    border-radius: 50%;
    background-color: #3498db;
    animation: pulse 1.4s ease-in-out infinite both;
  }

  .pulse:nth-child(1) {
    animation-delay: -0.32s;
  }

  .pulse:nth-child(2) {
    animation-delay: -0.16s;
  }

  .loading-text {
    margin-top: 20px;
    font-size: 16px;
    color: #333;
  }

  @keyframes pulse {
    0%,
    80%,
    100% {
      transform: scale(0);
    }
    40% {
      transform: scale(1);
    }
  }
</style>
