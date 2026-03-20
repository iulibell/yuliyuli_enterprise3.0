import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/hot',
      name: 'hot',
      component: HomeView,
    },
    {
      path: '/category/:id',
      name: 'category',
      component: HomeView,
    },
    {
      path: '/video/:id',
      name: 'video',
      component: () => import('../views/VideoDetailView.vue'),
    },
    {
      path: '/author/:userId/:authorName',
      name: 'author',
      component: () => import('../views/AuthorView.vue'),
    },
    {
      path: '/author/:userId/:authorName/videos',
      name: 'author-videos',
      component: () => import('../views/AuthorView.vue'),
    },
    {
      path: '/author/:userId/:authorName/playlists',
      name: 'author-playlists',
      component: () => import('../views/AuthorView.vue'),
    },
    {
      path: '/upload',
      name: 'upload',
      component: () => import('../views/UploadView.vue'),
    },
    {
      path: '/video-manager',
      name: 'video-manager',
      component: () => import('../views/VideoManagerView.vue'),
    },
    {
      path: '/video-manager/:userId',
      name: 'video-manager-with-id',
      component: () => import('../views/VideoManagerView.vue'),
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
    },
    {
      path: '/info',
      name: 'info',
      component: () => import('../views/ProfileView.vue'),
    },
    {
      path: '/security',
      name: 'security',
      component: () => import('../views/ProfileView.vue'),
    },
    {
      path: '/home',
      name: 'user-home',
      component: () => import('../views/ProfileView.vue'),
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('../views/SearchResultView.vue'),
    },
  ],
});

export default router;
