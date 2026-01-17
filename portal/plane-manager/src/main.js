import './assets/main.css'
import router from '@/router';

import { createApp } from 'vue'
import App from './App.vue'
import { createLx } from '@wntr/lx-ui';

const myApp = createApp(App);

myApp.use(createLx, {});
myApp.use(router).mount('#app')
