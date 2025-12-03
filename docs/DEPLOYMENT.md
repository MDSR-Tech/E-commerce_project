# MDSRTech Deployment Guide

## Deployment Overview

| Component | Platform | URL |
|-----------|----------|-----|
| Frontend | Vercel | [https://mdsrtech.vercel.app](https://mdsrtech.vercel.app) |
| Backend API | Railway | [https://e-commerce-project-production-dd50.up.railway.app](https://e-commerce-project-production-dd50.up.railway.app) |
| Database | Supabase | PostgreSQL hosted on Supabase |
| Payments | Stripe | Stripe Checkout integration |

### Deployment Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                         GitHub Repository                            │
│                    (github.com/MDSR-Tech/E-commerce_project)       │
└─────────────────────────────────────────────────────────────────────┘
                                   │
                                   │ Push to main branch
                                   │
                    ┌──────────────┴──────────────┐
                    │                             │
                    ▼                             ▼
         ┌─────────────────┐           ┌─────────────────┐
         │     Vercel      │           │     Railway     │
         │   (Frontend)    │           │    (Backend)    │
         │   Auto-deploy   │           │   Auto-deploy   │
         └─────────────────┘           └─────────────────┘
                    │                             │
                    │                             │
                    ▼                             ▼
         ┌─────────────────┐           ┌─────────────────┐
         │  Next.js App    │◄─────────►│   Flask API     │
         │  Static/SSR     │   HTTPS   │   Docker        │
         └─────────────────┘           └────────┬────────┘
                                                │
                                                │
                                                ▼
                                     ┌─────────────────┐
                                     │    Supabase     │
                                     │   PostgreSQL    │
                                     └─────────────────┘
```

### Production Architecture

```
                                    ┌─────────────────┐
                                    │      Users      │
                                    └────────┬────────┘
                                             │
                                             │ HTTPS
                                             │
                              ┌──────────────┴──────────────┐
                              │                             │
                              ▼                             ▼
                   ┌─────────────────┐           ┌─────────────────┐
                   │     Vercel      │           │     Railway     │
                   │     CDN/Edge    │           │   Container     │
                   │                 │           │                 │
                   │  ┌───────────┐  │           │  ┌───────────┐  │
                   │  │ Next.js   │  │──────────►│  │  Flask    │  │
                   │  │ Frontend  │  │   API     │  │  Gunicorn │  │
                   │  └───────────┘  │  Requests │  └───────────┘  │
                   └─────────────────┘           └────────┬────────┘
                                                          │
                         ┌────────────────────────────────┼────────────────────────────────┐
                         │                                │                                │
                         ▼                                ▼                                ▼
              ┌─────────────────┐              ┌─────────────────┐              ┌─────────────────┐
              │    Supabase     │              │     Stripe      │              │   Gmail SMTP    │
              │   PostgreSQL    │              │    Payments     │              │    (Emails)     │
              └─────────────────┘              └─────────────────┘              └─────────────────┘
```

---

## Environment Variables

### Backend (Railway)

| Variable | Description 
|----------|-------------
| `DATABASE_URL` | PostgreSQL connection string
| `JWT_SECRET_KEY` | Secret for JWT tokens
| `STRIPE_SECRET_KEY` | Stripe secret key
| `STRIPE_PUBLISHABLE_KEY` | Stripe publishable key
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook signing secret
| `FRONTEND_URL` | Frontend URL for CORS
| `GOOGLE_CLIENT_ID` | Google OAuth client ID
| `GOOGLE_CLIENT_SECRET` | Google OAuth secret
| `GITHUB_CLIENT_ID` | GitHub OAuth client ID
| `GITHUB_CLIENT_SECRET` | GitHub OAuth secret
| `MAIL_USERNAME` | Gmail address for emails
| `MAIL_PASSWORD` | Gmail app password

### Frontend (Vercel)

| Variable | Description |
|----------|-------------|
| `NEXT_PUBLIC_API_URL` | Backend API URL |
| `NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY` | Stripe publishable key |

---

## CI/CD Pipeline

Both platforms auto-deploy when code is pushed to the `main` branch.

| Event | Vercel | Railway |
|-------|--------|---------|
| Push to `main` | ✅ Auto-deploy | ✅ Auto-deploy |
| Pull Request | Preview deploy | - |

### GitHub Actions

On every push, GitHub Actions runs:
- **Backend**: Lint (flake8) → Tests (pytest)
- **Frontend**: Lint (ESLint) → Build (Next.js)

---

## Database Schema

Tables are auto-created via SQLAlchemy:

`users` · `products` · `categories` · `brands` · `product_images` · `carts` · `cart_items` · `wishlists` · `wishlist_items` · `orders` · `order_items` · `payments` · `addresses` · `promo_codes` · `password_reset_tokens`

---

## Stripe Webhook

The webhook endpoint receives payment events from Stripe:

```
POST /api/checkout/webhook
```

**Events handled:**
- `checkout.session.completed` - Creates order after successful payment
- `payment_intent.payment_failed` - Handles failed payments

---

## Rollback

**Vercel:** Deployments → Find previous → "Promote to Production"

**Railway:** Deployments → Find previous → "Rollback"
