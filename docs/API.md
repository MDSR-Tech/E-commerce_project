# MDSRTech API Documentation

## Base URL

| Environment | Base URL |
|-------------|----------|
| Production | `https://e-commerce-project-production-dd50.up.railway.app/api` |
| Local Development | `http://localhost:5000/api` |

## Authentication

Most endpoints require JWT authentication. Include the token in the request header:

```
Authorization: Bearer <access_token>
```

**Token Types:**
- **Access Token**: Short-lived (15 minutes) for API requests
- **Refresh Token**: Long-lived (30 days) for obtaining new access tokens

---

## Authentication Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/auth/register` | Register new user | No |
| POST | `/auth/login` | Login with email/password | No |
| POST | `/auth/refresh` | Get new access token | Refresh |
| GET | `/auth/me` | Get current user info | Yes |
| POST | `/auth/logout` | Invalidate token | Yes |
| GET | `/auth/google` | Initiate Google OAuth | No |
| GET | `/auth/google/callback` | Google OAuth callback | No |
| GET | `/auth/github` | Initiate GitHub OAuth | No |
| GET | `/auth/github/callback` | GitHub OAuth callback | No |
| POST | `/auth/forgot-password` | Request password reset email | No |
| POST | `/auth/verify-reset-token` | Validate reset token | No |
| POST | `/auth/reset-password` | Reset password with token | No |
| GET | `/auth/admin/verify` | Check if user is admin | Yes |

### Register / Login Request
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "first_name": "John",      // register only
  "last_name": "Doe"         // register only
}
```

### Auth Response
```json
{
  "user": { "id": 1, "email": "...", "first_name": "...", "last_name": "...", "is_admin": false },
  "access_token": "eyJ...",
  "refresh_token": "eyJ..."
}
```

---

## Products Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/products` | List all products (supports filters) |
| GET | `/products/{id}` | Get product by ID |
| GET | `/products/slug/{slug}` | Get product by URL slug |
| GET | `/categories/{slug}/products` | Get products in category |
| GET | `/search?q={query}` | Search products |
| GET | `/categories` | List all categories |
| GET | `/brands` | List all brands |

### Product Filters (query params)
- `category` - Filter by category slug
- `brand` - Filter by brand slug
- `min_price` / `max_price` - Price range
- `sort` - `price_asc`, `price_desc`, `newest`

### Product Response
```json
{
  "id": 1,
  "name": "iPhone 15 Pro",
  "slug": "iphone-15-pro",
  "description": "...",
  "price": 999.99,
  "sale_price": null,
  "stock": 50,
  "category": { "id": 1, "name": "Smartphones", "slug": "smartphones" },
  "brand": { "id": 1, "name": "Apple", "slug": "apple" },
  "images": [{ "id": 1, "url": "...", "is_primary": true }]
}
```

---

## Cart Endpoints (Auth Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/cart` | Get user's cart with items |
| GET | `/cart/count` | Get item count |
| POST | `/cart/add` | Add product to cart |
| PUT | `/cart/update` | Update item quantity |
| DELETE | `/cart/remove` | Remove item from cart |
| DELETE | `/cart/clear` | Clear entire cart |
| POST | `/cart/apply-promo` | Apply promo code |

### Cart Request Body
```json
{ "product_id": 1, "quantity": 2 }
```

### Cart Response
```json
{
  "items": [{ "product": {...}, "quantity": 2, "unit_price": 999.99 }],
  "subtotal": 1999.98,
  "tax": 259.99,
  "total": 2259.97
}
```

---

## Wishlist Endpoints (Auth Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/wishlist` | Get user's wishlist |
| GET | `/wishlist/ids` | Get product IDs only |
| POST | `/wishlist/add` | Add to wishlist |
| DELETE | `/wishlist/remove` | Remove from wishlist |
| POST | `/wishlist/toggle` | Toggle product in wishlist |
| GET | `/wishlist/check/{product_id}` | Check if product in wishlist |

---

## Orders Endpoints (Auth Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/orders` | List user's orders |
| GET | `/orders/{id}` | Get order details |
| DELETE | `/orders/{id}` | Cancel order (if pending) |

### Order Response
```json
{
  "id": 1,
  "order_number": "ORD-20240115-ABC123",
  "status": "completed",
  "subtotal": 1999.98,
  "tax": 259.99,
  "total": 2259.97,
  "items": [{ "product": {...}, "quantity": 2, "unit_price": 999.99 }],
  "shipping_address": { "line1": "...", "city": "...", "state": "...", "postal_code": "...", "country": "..." }
}
```

---

## Checkout Endpoints (Auth Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/checkout/create-session` | Create Stripe checkout session |
| GET | `/checkout/session/{session_id}` | Get session details |
| POST | `/checkout/webhook` | Stripe webhook (internal) |

### Create Session Request
```json
{
  "shipping_address": {
    "line1": "123 Main St",
    "city": "Toronto",
    "state": "ON",
    "postal_code": "M5V 1A1",
    "country": "CA"
  }
}
```

### Create Session Response
```json
{
  "session_id": "cs_xxx",
  "url": "https://checkout.stripe.com/pay/cs_xxx"
}
```

---

## Error Responses

```json
{ "error": "Error message description" }
```

| Code | Description |
|------|-------------|
| 400 | Bad Request - Invalid input |
| 401 | Unauthorized - Missing/invalid token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found |
| 409 | Conflict - Resource already exists |
| 500 | Internal Server Error |
