# MDSRTech Architecture Documentation

## System Overview

MDSRTech is a full-stack e-commerce platform built with a **layered architecture** that separates concerns between the presentation layer (Next.js frontend), business logic layer (Flask API), and data layer (PostgreSQL database).

---

## UML Package Diagram

The package diagram shows the high-level structure and dependencies between major components.

```mermaid
graph TB
    subgraph "Client Layer"
        Browser["🌐 Web Browser"]
    end

    subgraph "Presentation Layer (Vercel)"
        subgraph "Next.js Frontend"
            Pages["App Router<br/>Pages"]
            Components["React<br/>Components"]
            Contexts["React<br/>Contexts"]
            API_Client["API Client<br/>Library"]
        end
    end

    subgraph "Business Logic Layer (Railway)"
        subgraph "Flask Backend"
            Auth["auth.py<br/>Authentication"]
            Products["products.py<br/>Product Catalog"]
            Cart["cart.py<br/>Shopping Cart"]
            Wishlist["wishlist.py<br/>Wishlist"]
            Orders["orders.py<br/>Order Management"]
            Checkout["checkout.py<br/>Payment Processing"]
        end
        Extensions["extensions.py<br/>Flask Extensions"]
        Models["models.py<br/>ORM Models"]
    end

    subgraph "Data Layer (Supabase)"
        PostgreSQL[("PostgreSQL<br/>Database")]
    end

    subgraph "External Services"
        Stripe["Stripe API<br/>Payments"]
        Google["Google OAuth<br/>Authentication"]
        GitHub["GitHub OAuth<br/>Authentication"]
        Gmail["Gmail SMTP<br/>Email Service"]
    end

    Browser --> Pages
    Pages --> Components
    Pages --> Contexts
    Contexts --> API_Client
    API_Client -->|"REST API<br/>HTTP/JSON"| Auth
    API_Client -->|"REST API"| Products
    API_Client -->|"REST API"| Cart
    API_Client -->|"REST API"| Wishlist
    API_Client -->|"REST API"| Orders
    API_Client -->|"REST API"| Checkout

    Auth --> Extensions
    Products --> Extensions
    Cart --> Extensions
    Wishlist --> Extensions
    Orders --> Extensions
    Checkout --> Extensions

    Auth --> Models
    Products --> Models
    Cart --> Models
    Wishlist --> Models
    Orders --> Models
    Checkout --> Models

    Models -->|"SQLAlchemy ORM"| PostgreSQL

    Auth -->|"OAuth 2.0"| Google
    Auth -->|"OAuth 2.0"| GitHub
    Auth -->|"SMTP"| Gmail
    Checkout -->|"Payment API"| Stripe
    Orders -->|"SMTP"| Gmail
```

---

## UML Class Diagram

The class diagram shows the database models and their relationships.

```mermaid
classDiagram
    class User {
        +UUID id
        +String email
        +String password_hash
        +String full_name
        +String role
        +Boolean is_active
        +String oauth_provider
        +String oauth_id
        +DateTime created_at
        +carts: Cart
        +wishlists: Wishlist
        +orders: List~Order~
        +addresses: List~Address~
    }

    class Product {
        +BigInteger id
        +String title
        +String slug
        +BigInteger brand_id
        +BigInteger category_id
        +String description
        +Integer price_cents
        +String currency
        +Integer stock
        +Boolean is_active
        +Integer sale_percent
        +DateTime created_at
        +DateTime updated_at
        +is_on_sale() Boolean
        +effective_sale_percent() Integer
        +sale_price_cents() Integer
    }

    class Category {
        +BigInteger id
        +String name
        +String slug
        +BigInteger parent_id
        +Integer sale_percent
        +DateTime created_at
        +products: List~Product~
    }

    class Brand {
        +BigInteger id
        +String name
        +String slug
        +DateTime created_at
        +products: List~Product~
    }

    class ProductImage {
        +BigInteger id
        +BigInteger product_id
        +String url
        +String alt_text
        +Integer position
        +Boolean is_primary
        +DateTime created_at
    }

    class Cart {
        +BigInteger id
        +UUID user_id
        +DateTime created_at
        +user: User
        +items: List~CartItem~
    }

    class CartItem {
        +BigInteger id
        +BigInteger cart_id
        +BigInteger product_id
        +Integer quantity
        +Integer unit_price_cents
        +DateTime added_at
        +cart: Cart
        +product: Product
    }

    class Wishlist {
        +BigInteger id
        +UUID user_id
        +DateTime created_at
        +user: User
        +items: List~WishlistItem~
    }

    class WishlistItem {
        +BigInteger id
        +BigInteger wishlist_id
        +BigInteger product_id
        +DateTime added_at
        +wishlist: Wishlist
        +product: Product
    }

    class Order {
        +BigInteger id
        +UUID user_id
        +Integer subtotal_cents
        +Integer tax_cents
        +Integer shipping_cents
        +Integer total_cents
        +String currency
        +BigInteger payment_id
        +BigInteger shipping_address_id
        +BigInteger billing_address_id
        +DateTime placed_at
        +DateTime created_at
        +user: User
        +items: List~OrderItem~
        +payment: Payment
    }

    class OrderItem {
        +BigInteger id
        +BigInteger order_id
        +BigInteger product_id
        +String title_snapshot
        +Integer unit_price_cents
        +Integer quantity
        +Integer line_total_cents
        +DateTime created_at
        +order: Order
        +product: Product
    }

    class Payment {
        +BigInteger id
        +String provider
        +String provider_payment_id
        +String status
        +Integer amount_cents
        +String currency
        +JSON raw_response
        +DateTime created_at
    }

    class Address {
        +BigInteger id
        +UUID user_id
        +String address_line1
        +String address_line2
        +String city
        +String state_province
        +String postal_code
        +String country
        +DateTime created_at
        +user: User
    }

    class PasswordResetToken {
        +BigInteger id
        +UUID user_id
        +String token
        +DateTime expires_at
        +Boolean used
        +DateTime created_at
        +user: User
    }

    class PromoCode {
        +BigInteger id
        +String code
        +Integer discount_percent
        +Boolean is_active
        +DateTime created_at
    }

    User "1" --> "0..1" Cart : has
    User "1" --> "0..1" Wishlist : has
    User "1" --> "*" Order : places
    User "1" --> "*" Address : has
    User "1" --> "*" PasswordResetToken : has

    Cart "1" --> "*" CartItem : contains
    CartItem "*" --> "1" Product : references

    Wishlist "1" --> "*" WishlistItem : contains
    WishlistItem "*" --> "1" Product : references

    Order "1" --> "*" OrderItem : contains
    Order "*" --> "0..1" Payment : paid_by
    OrderItem "*" --> "1" Product : references

    Category "1" --> "*" Product : contains
    Brand "1" --> "*" Product : manufactures
    Product "1" --> "*" ProductImage : has
```

---

## Technology Stack Summary

| Layer | Technology | Purpose |
|-------|------------|---------|
| **Frontend** | Next.js 15, React 19, TypeScript | Server-side rendering, UI components |
| **Styling** | TailwindCSS, Lucide Icons | Responsive design, iconography |
| **State Management** | React Context API | Auth, Cart, Wishlist state |
| **Backend** | Flask 3.0, Python 3.11 | REST API, business logic |
| **ORM** | SQLAlchemy | Database abstraction |
| **Authentication** | Flask-JWT-Extended, OAuth 2.0 | Token-based auth, social login |
| **Database** | PostgreSQL (Supabase) | Data persistence |
| **Payments** | Stripe Checkout | Secure payment processing |
| **Deployment** | Vercel, Railway, Docker | Cloud hosting, containerization |
