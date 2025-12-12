import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
} from "vue-router";
import { useAuthStore } from "../stores/auth";
import Login from "../views/Login.vue";
import Register from "../views/Register.vue";
import Index from "../views/Index.vue";
import PatientDetail from "../views/PatientDetail.vue";
import OwnerDetail from "@/views/OwnerDetail.vue";
import VisitDetail from "@/views/VisitDetail.vue";

const routes: RouteRecordRaw[] = [
  {
    path: "/login",
    name: "Login",
    component: Login,
    meta: { requiresGuest: true },
  },
  {
    path: "/register",
    name: "Register",
    component: Register,
    meta: { requiresGuest: true },
  },
  {
    path: "/",
    name: "Index",
    component: Index,
    meta: { requiresAuth: true },
  },
  {
    path: "/patients/:id",
    name: "PatientDetail",
    component: PatientDetail,
    meta: { requiresAuth: true },
  },
  {
    path: "/owners/:id",
    name: "OwnerDetail",
    component: OwnerDetail,
    meta: { requiresAuth: true },
  },
  {
    path: "/visits/:visitId",
    name: "visit-details",
    component: VisitDetail,
    meta: { requiresAuth: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// Navigation guard
router.beforeEach((to, _, next) => {
  const authStore = useAuthStore();

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next("/login");
  } else if (to.meta.requiresGuest && authStore.isAuthenticated) {
    next("/");
  } else {
    next();
  }
});

export default router;
