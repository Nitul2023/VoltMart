export type Role = "CUSTOMER" | "ADMIN" | "MANAGER" | "SUPPORT" | "EDITOR";

export interface UserView {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  roles: Role[];
  emailVerified: boolean;
}

export interface AuthResponse {
  token: string;
  user: UserView;
}

export interface CategoryView {
  id: number;
  name: string;
  slug: string;
  description?: string;
  imageUrl?: string;
  parentId?: number | null;
  children: CategoryView[];
  seoTitle?: string;
  seoDescription?: string;
}

export interface BrandView {
  id: number;
  name: string;
  slug: string;
  logoUrl?: string;
  description?: string;
}

export interface ImageView {
  id: number;
  url: string;
  altText?: string;
  sortOrder: number;
}

export interface VariantView {
  id: string;
  sku: string;
  name: string;
  color?: string;
  storage?: string;
  otherOption?: string;
  price: number;
  compareAtPrice?: number;
  stockQty: number;
  availableQty: number;
  active: boolean;
}

export interface SpecView {
  groupName: string;
  specKey: string;
  specValue: string;
}

export interface FilterView {
  id: number;
  name: string;
  slug: string;
  filterType: string;
  unit?: string;
}

export interface ProductCard {
  id: string;
  sku: string;
  name: string;
  slug: string;
  shortDescription?: string;
  basePrice: number;
  compareAtPrice?: number;
  discountPercent?: number;
  imageUrl?: string;
  brand?: string;
  category?: string;
  featured: boolean;
  warrantyMonths: number;
  availability: string;
}

export interface ProductDetail {
  id: string;
  sku: string;
  name: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  brand: BrandView;
  category: CategoryView;
  basePrice: number;
  compareAtPrice?: number;
  discountPercent?: number;
  warrantyMonths: number;
  weightGrams?: number;
  featured: boolean;
  seoTitle?: string;
  seoDescription?: string;
  images: ImageView[];
  variants: VariantView[];
  specs: SpecView[];
  averageRating?: number;
  reviewCount: number;
}

export interface ReviewView {
  id: string;
  author: string;
  rating: number;
  title?: string;
  body: string;
  verified: boolean;
  createdAt: string;
}

export interface PageResult<T> {
  items: T[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}

export interface AutocompleteItem {
  id: string;
  name: string;
  sku: string;
  slug: string;
  category?: string;
  imageUrl?: string;
}

export interface BannerView {
  id: number;
  title: string;
  subtitle?: string;
  imageUrl: string;
  ctaLabel?: string;
  ctaUrl?: string;
  placement: string;
}

export interface CmsPageView {
  id: number;
  slug: string;
  title: string;
  body: string;
  seoTitle?: string;
  seoDescription?: string;
}

export interface CartItemView {
  id: string;
  variantId: string;
  productId: string;
  productName: string;
  variantName: string;
  sku: string;
  imageUrl?: string;
  unitPrice: number;
  quantity: number;
  availableQty: number;
  lineTotal: number;
}

export interface CartView {
  id: string;
  items: CartItemView[];
  subtotal: number;
  itemCount: number;
}

export interface AddressView {
  id: string;
  label?: string;
  line1: string;
  line2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  defaultAddress: boolean;
}

export interface OrderItemView {
  id: string;
  productId: string;
  variantId: string;
  sku: string;
  productName: string;
  variantName: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
}

export interface PaymentView {
  id: string;
  provider: string;
  method: string;
  status: string;
  amount: number;
  transactionRef?: string;
  last4?: string;
  brand?: string;
  verified: boolean;
}

export interface ShipmentView {
  id: string;
  carrier: string;
  trackingNumber?: string;
  status: string;
  shippedAt?: string;
  deliveredAt?: string;
}

export interface InvoiceView {
  id: string;
  invoiceNumber: string;
  issuedAt: string;
  html?: string;
}

export interface OrderView {
  id: string;
  orderNumber: string;
  status: string;
  subtotal: number;
  discount: number;
  shippingCost: number;
  tax: number;
  total: number;
  couponCode?: string;
  shippingName: string;
  shippingLine1: string;
  shippingLine2?: string;
  shippingCity: string;
  shippingState: string;
  shippingPostal: string;
  shippingCountry: string;
  shippingMethod: string;
  createdAt: string;
  items: OrderItemView[];
  payment?: PaymentView;
  shipment?: ShipmentView;
  invoice?: InvoiceView;
}

export interface CouponPreview {
  code: string;
  description?: string;
  discount: number;
  valid: boolean;
  message: string;
}

export interface ReportSummary {
  ordersToday: number;
  revenueToday: number;
  customers: number;
  lowStockVariants: number;
  openReturns: number;
}

export interface AuditView {
  id: number;
  actorEmail?: string;
  action: string;
  entityType: string;
  entityId?: string;
  details?: string;
  createdAt: string;
}

export interface CustomerView {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  roles: string[];
  createdAt: string;
}

export interface ReturnView {
  id: string;
  orderId: string;
  orderNumber: string;
  reason: string;
  status: string;
  refundAmount?: number;
  createdAt: string;
}
