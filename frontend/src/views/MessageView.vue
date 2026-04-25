<template>
  <div class="message-page">
    <div class="message-panel">
      <aside class="message-nav">
        <h3>消息中心</h3>
        <ul>
          <li :class="{ active: activeTab === 'chat' }" @click="activeTab = 'chat'">我的消息</li>
          <li :class="{ active: activeTab === 'reply' }" @click="activeTab = 'reply'">回复我的</li>
          <li :class="{ active: activeTab === 'mention' }" @click="activeTab = 'mention'">@ 我的</li>
          <li :class="{ active: activeTab === 'system' }" @click="activeTab = 'system'">系统通知</li>
        </ul>
      </aside>

      <section v-if="activeTab === 'chat'" class="conversation-list">
        <div class="section-title">我的消息</div>
        <div
          v-for="item in conversations"
          :key="item.userId"
          class="conversation-item"
          :class="{ active: activeTargetUserId === item.userId }"
          @click="openConversation(item.userId)"
        >
          <img :src="item.avatar || defaultAvatar" class="avatar" alt="avatar" />
          <div class="meta">
            <div class="name-row">
              <span class="name">{{ item.username }}</span>
              <span v-if="item.unreadCount > 0" class="badge">{{ item.unreadCount }}</span>
            </div>
            <div class="preview">{{ item.lastContent }}</div>
          </div>
        </div>
      </section>

      <section v-if="activeTab === 'chat'" class="chat-area">
        <div class="chat-header">{{ activeConversationName || '请选择会话' }}</div>
        <div class="chat-list" ref="chatListRef">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="chat-item"
            :class="{ me: msg.fromUserId === currentUserId }"
          >
            <div class="bubble">{{ msg.content }}</div>
          </div>
        </div>
        <div class="chat-input">
          <textarea
            v-model="inputText"
            :disabled="!activeTargetUserId"
            placeholder="请输入消息内容"
            maxlength="500"
          ></textarea>
          <button :disabled="!activeTargetUserId || !inputText.trim()" @click="sendMessage">发送</button>
        </div>
      </section>

      <section v-else class="placeholder-area">
        <template v-if="activeTab === 'reply'">
          <div class="reply-header-bar">
            <div class="reply-tab active">回复我的</div>
          </div>
          <div class="reply-list" v-if="replyNotices.length > 0">
            <div class="reply-item" v-for="item in replyNotices" :key="item.replyCommentId">
              <img :src="item.replyAvatar || defaultAvatar" class="avatar" alt="avatar" />
              <div class="reply-main" @click="openReplyVideo(item)">
                <div class="reply-line-1">
                  <span class="reply-user">{{ item.replyUsername }}</span>
                  <span class="reply-action-text">回复了我的评论</span>
                </div>
                <div class="reply-content">{{ item.replyContent }}</div>
                <div class="reply-meta-line">
                  <span class="reply-time">{{ formatTime(item.createTime) }}</span>
                  <span class="reply-op">回复</span>
                  <span class="reply-op">点赞</span>
                </div>
              </div>
              <div class="reply-side-content" @click="openReplyVideo(item)">
                {{ item.parentContent }}
              </div>
            </div>
          </div>
          <div class="placeholder-content" v-else>暂无新的回复通知</div>
        </template>
        <template v-else>
          <div class="placeholder-title">{{ activeTab === 'mention' ? '@ 我的' : '系统通知' }}</div>
          <div class="placeholder-content">该模块已预留，后续可接入数据。</div>
        </template>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { nextTick, onMounted, ref, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import axios from 'axios';
  import { ElMessage } from 'element-plus';

  interface Conversation {
    userId: number;
    username: string;
    avatar: string;
    lastContent: string;
    unreadCount: number;
  }

  interface PrivateMessage {
    id: number;
    fromUserId: number;
    toUserId: number;
    content: string;
    createTime: string;
  }

  interface ReplyNotice {
    replyCommentId: number;
    videoId: string;
    replyContent: string;
    parentContent: string;
    createTime: string;
    replyUsername: string;
    replyAvatar: string;
  }

  const route = useRoute();
  const router = useRouter();
  const token = ref(localStorage.getItem('token') || '');
  const currentUserId = Number(localStorage.getItem('userId') || 0);
  const defaultAvatar = '/static/images/202304061680747832129368.jpg';

  const conversations = ref<Conversation[]>([]);
  const messages = ref<PrivateMessage[]>([]);
  const activeTargetUserId = ref<number>(0);
  const activeConversationName = ref<string>('');
  const activeTab = ref<'chat' | 'reply' | 'mention' | 'system'>('chat');
  const inputText = ref('');
  const replyNotices = ref<ReplyNotice[]>([]);
  const chatListRef = ref<HTMLElement | null>(null);

  const requestConfig = () => ({
    headers: token.value
      ? {
          Authorization: `Bearer ${token.value}`,
        }
      : undefined,
  });

  const scrollToBottom = async () => {
    await nextTick();
    if (chatListRef.value) {
      chatListRef.value.scrollTop = chatListRef.value.scrollHeight;
    }
  };

  const fetchConversations = async () => {
    const res = await axios.get('/api/message/conversations', requestConfig());
    if (res.data?.code === 200) {
      conversations.value = res.data.data || [];
    } else {
      throw new Error(res.data?.msg || '获取会话失败');
    }
  };

  const fetchMessages = async () => {
    if (!activeTargetUserId.value) return;
    const res = await axios.get('/api/message/list', {
      params: { targetUserId: activeTargetUserId.value },
      ...requestConfig(),
    });
    if (res.data?.code === 200) {
      messages.value = res.data.data || [];
      await scrollToBottom();
    } else {
      throw new Error(res.data?.msg || '获取消息失败');
    }
  };

  const openConversation = async (targetUserId: number) => {
    activeTargetUserId.value = targetUserId;
    const found = conversations.value.find(item => item.userId === targetUserId);
    activeConversationName.value = found?.username || '会话';
    await fetchMessages();
    await fetchConversations();
  };

  const sendMessage = async () => {
    if (!activeTargetUserId.value || !inputText.value.trim()) return;
    const text = inputText.value.trim();
    const res = await axios.post(
      '/api/message/send',
      {
        toUserId: activeTargetUserId.value,
        content: text,
      },
      requestConfig()
    );
    if (res.data?.code === 200) {
      inputText.value = '';
      await fetchMessages();
      await fetchConversations();
    } else {
      ElMessage.error(res.data?.msg || '发送失败');
    }
  };

  const fetchReplyNotices = async () => {
    const res = await axios.get('/api/message/reply-me', {
      params: {
        limit: 100,
      },
      ...requestConfig(),
    });
    if (res.data?.code === 200) {
      replyNotices.value = res.data.data || [];
    } else {
      throw new Error(res.data?.msg || '加载回复通知失败');
    }
  };

  const openReplyVideo = (item: ReplyNotice) => {
    const videoId = item.videoId || '';
    if (!videoId) {
      ElMessage.warning('该回复未关联视频');
      return;
    }
    router.push(`/video/${encodeURIComponent(videoId)}`);
  };

  const formatTime = (time: string) => {
    if (!time) return '';
    const d = new Date(time);
    const year = d.getFullYear();
    const month = `${d.getMonth() + 1}`.padStart(2, '0');
    const day = `${d.getDate()}`.padStart(2, '0');
    const hh = `${d.getHours()}`.padStart(2, '0');
    const mm = `${d.getMinutes()}`.padStart(2, '0');
    return `${year}年${month}月${day}日 ${hh}:${mm}`;
  };

  onMounted(async () => {
    if (!token.value) {
      ElMessage.warning('请先登录后查看消息');
      return;
    }
    try {
      await fetchConversations();
      const queryTarget = Number(route.query.targetUserId || 0);
      if (queryTarget) {
        await openConversation(queryTarget);
        return;
      }
      if (conversations.value.length > 0) {
        await openConversation(conversations.value[0].userId);
      }
    } catch (e: any) {
      ElMessage.error(e.message || '加载消息失败');
    }
  });

  watch(
    () => activeTab.value,
    async newTab => {
      if (newTab === 'reply') {
        try {
          await fetchReplyNotices();
        } catch (e: any) {
          ElMessage.error(e.message || '加载回复通知失败');
        }
      }
    }
  );
</script>

<style scoped>
  .message-page {
    min-height: 100vh;
    background: #e9f1f5;
    padding: 18px;
  }

  .message-panel {
    max-width: 1260px;
    height: calc(100vh - 36px);
    margin: 0 auto;
    background: #fff;
    border: 1px solid #dde3ea;
    display: grid;
    grid-template-columns: 180px 280px minmax(0, 1fr);
  }

  .message-nav {
    background: #f5f7fa;
    border-right: 1px solid #e7ebf0;
    padding: 16px 12px;
  }

  .message-nav h3 {
    font-size: 16px;
    margin: 0 0 14px;
  }

  .message-nav ul {
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .message-nav li {
    padding: 9px 8px;
    color: #5f6b7a;
    border-radius: 6px;
    margin-bottom: 4px;
    cursor: pointer;
  }

  .message-nav li.active {
    color: #00a1d6;
    background: #eaf7ff;
    font-weight: 600;
  }

  .conversation-list {
    border-right: 1px solid #e7ebf0;
    overflow: auto;
  }

  .section-title {
    font-size: 14px;
    color: #5f6b7a;
    padding: 12px;
    border-bottom: 1px solid #eef2f6;
  }

  .conversation-item {
    display: flex;
    gap: 10px;
    padding: 10px 12px;
    cursor: pointer;
    border-bottom: 1px solid #f3f5f8;
  }

  .conversation-item.active {
    background: #f1f7fb;
  }

  .avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
  }

  .meta {
    min-width: 0;
    flex: 1;
  }

  .name-row {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .name {
    font-size: 14px;
    font-weight: 600;
    color: #1f2d3d;
  }

  .badge {
    font-size: 12px;
    color: #fff;
    background: #ff6699;
    border-radius: 10px;
    padding: 0 6px;
    line-height: 18px;
  }

  .preview {
    margin-top: 4px;
    font-size: 12px;
    color: #7f8b99;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .chat-area {
    display: flex;
    flex-direction: column;
    min-width: 0;
  }

  .chat-header {
    padding: 12px 16px;
    border-bottom: 1px solid #eef2f6;
    font-size: 15px;
    font-weight: 600;
  }

  .chat-list {
    flex: 1;
    overflow: auto;
    padding: 14px 16px;
    background: #fbfcfe;
  }

  .chat-item {
    display: flex;
    margin-bottom: 10px;
  }

  .chat-item.me {
    justify-content: flex-end;
  }

  .bubble {
    max-width: 68%;
    background: #fff;
    border: 1px solid #e6ebf1;
    border-radius: 8px;
    padding: 8px 10px;
    color: #2f3a48;
    font-size: 14px;
    line-height: 1.5;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .chat-item.me .bubble {
    background: #e9f8ff;
    border-color: #cfefff;
  }

  .chat-input {
    border-top: 1px solid #e7ebf0;
    padding: 10px 12px;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .chat-input textarea {
    width: 100%;
    min-height: 72px;
    resize: none;
    border: 1px solid #dfe5ec;
    border-radius: 6px;
    padding: 8px;
    font-size: 14px;
  }

  .chat-input button {
    align-self: flex-end;
    background: #00a1d6;
    color: #fff;
    border: none;
    border-radius: 6px;
    padding: 6px 16px;
    cursor: pointer;
  }

  .chat-input button:disabled {
    background: #b8c6d1;
    cursor: not-allowed;
  }

  .placeholder-area {
    grid-column: 2 / 4;
    display: flex;
    flex-direction: column;
    align-items: stretch;
    color: #7f8b99;
    gap: 0;
    padding: 0;
  }

  .placeholder-title {
    color: #1f2d3d;
    font-size: 18px;
    font-weight: 600;
  }

  .placeholder-content {
    font-size: 13px;
    text-align: center;
    margin-top: 20px;
  }

  .reply-header-bar {
    height: 48px;
    border-bottom: 1px solid #e8edf2;
    display: flex;
    align-items: center;
    padding: 0 16px;
    background: #fff;
  }

  .reply-tab {
    font-size: 14px;
    color: #5f6b7a;
    padding-bottom: 2px;
  }

  .reply-tab.active {
    color: #18191c;
    font-weight: 600;
  }

  .reply-list {
    width: 100%;
    max-height: calc(100vh - 140px);
    overflow: auto;
    background: #fff;
  }

  .reply-item {
    display: grid;
    grid-template-columns: 40px minmax(0, 1fr) 220px;
    gap: 12px;
    padding: 14px 16px;
    border-bottom: 1px solid #eff2f5;
    align-items: start;
  }

  .reply-main {
    min-width: 0;
    cursor: pointer;
  }

  .reply-line-1 {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .reply-user {
    font-size: 14px;
    font-weight: 600;
    color: #18191c;
  }

  .reply-action-text {
    color: #7f8b99;
    font-size: 13px;
  }

  .reply-content {
    margin-top: 4px;
    color: #2a2d33;
    font-size: 15px;
    line-height: 1.5;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .reply-meta-line {
    margin-top: 8px;
    display: flex;
    align-items: center;
    gap: 12px;
    color: #9499a0;
    font-size: 12px;
  }

  .reply-time {
    color: #9499a0;
  }

  .reply-op {
    cursor: pointer;
  }

  .reply-op:hover {
    color: #00a1d6;
  }

  .reply-side-content {
    margin-top: 1px;
    color: #7f8b99;
    font-size: 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    line-height: 1.4;
    cursor: pointer;
    text-align: left;
  }

  @media (max-width: 1100px) {
    .reply-item {
      grid-template-columns: 40px minmax(0, 1fr);
    }

    .reply-side-content {
      grid-column: 2;
      margin-top: 6px;
    }
  }
</style>
