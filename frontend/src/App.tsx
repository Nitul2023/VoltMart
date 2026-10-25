import { Navigate, Route, Routes } from "react-router-dom";
import { AdminLayout } from "./admin/AdminLayout";
import {
  AdminBanners, AdminCategories, AdminCmsPages, AdminCustomers, AdminDashboard,
  AdminInventory, AdminOrders, AdminProducts, AdminPromotions, AdminReports, AdminReturns
} from "./admin/AdminPages";
import { Layout } from "./components/Layout";
import { AccountHome, CmsPageView, OrderDetailPage, WishlistPage } from "./pages/AccountPages";
import { LoginPage, RegisterPage } from "./pages/AuthPages";
import { CartPage } from "./pages/CartPage";
import { CatalogPage } from "./pages/CatalogPage";
import { CheckoutPage } from "./pages/CheckoutPage";
import { ComparePage } from "./pages/ComparePage";
import { HomePage } from "./pages/HomePage";
import { ProductPage } from "./pages/ProductPage";

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/catalog" element={<CatalogPage />} />
        <Route path="/p/:slug" element={<ProductPage />} />
        <Route path="/compare" element={<ComparePage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/checkout" element={<CheckoutPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/account" element={<AccountHome />} />
        <Route path="/account/wishlist" element={<WishlistPage />} />
        <Route path="/account/orders" element={<AccountHome />} />
        <Route path="/account/orders/:orderNumber" element={<OrderDetailPage />} />
        <Route path="/pages/:slug" element={<CmsPageView />} />
      </Route>
      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<AdminDashboard />} />
        <Route path="products" element={<AdminProducts />} />
        <Route path="categories" element={<AdminCategories />} />
        <Route path="inventory" element={<AdminInventory />} />
        <Route path="orders" element={<AdminOrders />} />
        <Route path="returns" element={<AdminReturns />} />
        <Route path="banners" element={<AdminBanners />} />
        <Route path="pages" element={<AdminCmsPages />} />
        <Route path="promotions" element={<AdminPromotions />} />
        <Route path="customers" element={<AdminCustomers />} />
        <Route path="reports" element={<AdminReports />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
