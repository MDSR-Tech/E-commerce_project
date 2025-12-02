from extensions import db
from sqlalchemy.dialects.postgresql import UUID
import uuid

class User(db.Model):
    __tablename__ = 'users'
    id = db.Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    email = db.Column(db.String(255), unique=True, nullable=False)
    password_hash = db.Column(db.Text, nullable=True)  # Nullable for OAuth users
    full_name = db.Column(db.Text, nullable=False)
    role = db.Column(db.String(50), nullable=False, default='customer')
    is_active = db.Column(db.Boolean, nullable=False, default=True)
    oauth_provider = db.Column(db.String(50), nullable=True)  # 'google', 'github', or null for email/password
    oauth_id = db.Column(db.String(255), nullable=True)  # Provider's user ID
    created_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=db.func.now())
    
    # Relationships
    carts = db.relationship("Cart", back_populates='user', uselist=False)
    wishlists = db.relationship("Wishlist", back_populates='user', uselist=False)
    orders = db.relationship('Order', back_populates='user')
    addresses = db.relationship('Address', back_populates='user')

class Category(db.Model):
    __tablename__ = 'categories'
    id = db.Column(db.BigInteger, primary_key=True)
    name = db.Column(db.Text, unique=True, nullable=False)
    slug = db.Column(db.Text, unique=True, nullable=False)
    parent_id = db.Column(db.BigInteger, db.ForeignKey('categories.id'))
    created_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=db.func.now())
    # Sale field for category-wide sales
    sale_percent = db.Column(db.Integer, nullable=True)  # e.g., 40 for 40% off
    
    products = db.relationship('Product', back_populates='category')

class Brand(db.Model):
    __tablename__ = 'brands'
    id = db.Column(db.BigInteger, primary_key=True)
    name = db.Column(db.Text, unique=True, nullable=False)
    slug = db.Column(db.Text, unique=True, nullable=False)
    created_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=db.func.now())
    
    products = db.relationship('Product', back_populates='brand')

class Product(db.Model):
    __tablename__ = 'products'
    id = db.Column(db.BigInteger, primary_key=True)
    title = db.Column(db.Text, nullable=False)
    slug = db.Column(db.Text, unique=True, nullable=False)
    brand_id = db.Column(db.BigInteger, db.ForeignKey('brands.id'))
    category_id = db.Column(db.BigInteger, db.ForeignKey('categories.id'))
    description = db.Column(db.Text)
    price_cents = db.Column(db.Integer, nullable=False)
    currency = db.Column(db.String(3), nullable=False, default='CAD')
    stock = db.Column(db.Integer, nullable=False, default=0)
    is_active = db.Column(db.Boolean, nullable=False, default=True)
    created_by = db.Column(UUID(as_uuid=True), db.ForeignKey('users.id'))
    updated_by = db.Column(UUID(as_uuid=True), db.ForeignKey('users.id'))
    created_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=db.func.now())
    updated_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=db.func.now())
    # Product-specific sale (overrides category sale if set)
    sale_percent = db.Column(db.Integer, nullable=True)  # e.g., 25 for 25% off
    
    # Relationships
    brand = db.relationship('Brand', back_populates='products')
    category = db.relationship('Category', back_populates='products')
    images = db.relationship('ProductImage', back_populates='product')
    
    @property
    def is_on_sale(self):
        """Check if product has an active sale (product-level or category-level)"""
        if self.sale_percent:
            return True
        if self.category and self.category.sale_percent:
            return True
        return False
    
    @property
    def effective_sale_percent(self):
        """Get the active sale percentage (product takes priority over category)"""
        if self.sale_percent:
            return self.sale_percent
        if self.category and self.category.sale_percent:
            return self.category.sale_percent
        return None
    
    @property
    def sale_price_cents(self):
        """Calculate the sale price in cents"""
        percent = self.effective_sale_percent
        if percent:
            discount = int(self.price_cents * percent / 100)
            return self.price_cents - discount
        return None

class ProductImage(db.Model):
    __tablename__ = 'product_images'
    id = db.Column(db.BigInteger, primary_key=True)
    product_id = db.Column(db.BigInteger, db.ForeignKey('products.id'), nullable=False)
    url = db.Column(db.Text, nullable=False)  # Changed from image_url to url
    alt_text = db.Column(db.Text)
    position = db.Column(db.Integer, nullable=False, default=0)  # Changed from display_order to position
    is_primary = db.Column(db.Boolean, default=False)
    created_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=db.func.now())
    
    product = db.relationship('Product', back_populates='images')

class Address(db.Model):
    __tablename__ = 'addresses'
    id = db.Column(db.BigInteger, primary_key=True)
    user_id = db.Column(UUID(as_uuid=True), db.ForeignKey('users.id'), nullable=False)
    address_line1 = db.Column(db.Text, nullable=False)
    address_line2 = db.Column(db.Text)
    city = db.Column(db.Text, nullable=False)
    state_province = db.Column(db.Text)
    postal_code = db.Column(db.String(20), nullable=False)
    country = db.Column(db.String(2), nullable=False)
    created_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=db.func.now())
    
    user = db.relationship('User', back_populates='addresses')
    

class Wishlist(db.Model):
    __tablename__ = 'wishlists'
    id = db.Column(db.BigInteger, primary_key=True)
    user_id = db.Column(UUID(as_uuid=True), db.ForeignKey('users.id'), unique=True, nullable=False)
    created_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=db.func.now())
    
    user = db.relationship('User', back_populates='wishlists')
    items = db.relationship('WishlistItem', back_populates='wishlist')

class WishlistItem(db.Model):
    __tablename__ = 'wishlist_items'
    id = db.Column(db.BigInteger, primary_key=True)
    wishlist_id = db.Column(db.BigInteger, db.ForeignKey('wishlists.id'), nullable=False)
    product_id = db.Column(db.BigInteger, db.ForeignKey('products.id'), nullable=False)
    added_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=db.func.now())
    
    wishlist = db.relationship('Wishlist', back_populates='items')
    product = db.relationship('Product')

class Cart(db.Model):
    __tablename__ = 'carts'
    id = db.Column(db.BigInteger, primary_key=True)
    user_id = db.Column(UUID(as_uuid=True), db.ForeignKey('users.id'), unique=True, nullable=False)
    created_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=db.func.now())
    
    user = db.relationship('User', back_populates='carts')
    items = db.relationship('CartItem', back_populates='cart', order_by='CartItem.added_at')

class CartItem(db.Model):
    __tablename__ = 'cart_items'
    id = db.Column(db.BigInteger, primary_key=True)
    cart_id = db.Column(db.BigInteger, db.ForeignKey('carts.id'), nullable=False)
    product_id = db.Column(db.BigInteger, db.ForeignKey('products.id'), nullable=False)
    quantity = db.Column(db.Integer, nullable=False, default=1)
    unit_price_cents = db.Column(db.Integer, nullable=False)
    added_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=db.func.now())
    
    cart = db.relationship('Cart', back_populates='items')
    product = db.relationship('Product')

class Payment(db.Model):
    __tablename__ = 'payments'
    id = db.Column(db.BigInteger, primary_key=True)
    provider = db.Column(db.Text, nullable=False, default='stripe')
    provider_payment_id = db.Column(db.Text, unique=True, nullable=False)
    status = db.Column(db.String(50), nullable=False, default='succeeded')
    amount_cents = db.Column(db.Integer, nullable=False)
    currency = db.Column(db.String(3), nullable=False, default='CAD')
    raw_response = db.Column(db.JSON)
    created_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=db.func.now())

class Order(db.Model):
    __tablename__ = 'orders'
    id = db.Column(db.BigInteger, primary_key=True)
    user_id = db.Column(UUID(as_uuid=True), db.ForeignKey('users.id'), nullable=False)
    subtotal_cents = db.Column(db.Integer, nullable=False)
    tax_cents = db.Column(db.Integer, nullable=False, default=0)
    shipping_cents = db.Column(db.Integer, nullable=False, default=0)
    total_cents = db.Column(db.Integer, nullable=False)
    currency = db.Column(db.String(3), nullable=False, default='CAD')
    payment_id = db.Column(db.BigInteger, db.ForeignKey('payments.id'))
    shipping_address_id = db.Column(db.BigInteger, db.ForeignKey('addresses.id'))
    billing_address_id = db.Column(db.BigInteger, db.ForeignKey('addresses.id'))
    placed_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=db.func.now())
    created_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=db.func.now())
    
    user = db.relationship('User', back_populates='orders')
    items = db.relationship('OrderItem', back_populates='order')
    payment = db.relationship('Payment')

class OrderItem(db.Model):
    __tablename__ = 'order_items'
    id = db.Column(db.BigInteger, primary_key=True)
    order_id = db.Column(db.BigInteger, db.ForeignKey('orders.id'), nullable=False)
    product_id = db.Column(db.BigInteger, db.ForeignKey('products.id'))
    title_snapshot = db.Column(db.Text, nullable=False)
    unit_price_cents = db.Column(db.Integer, nullable=False)
    quantity = db.Column(db.Integer, nullable=False)
    line_total_cents = db.Column(db.Integer, nullable=False)
    created_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=db.func.now())
    
    order = db.relationship('Order', back_populates='items')
    product = db.relationship('Product')


class PromoCode(db.Model):
    __tablename__ = 'promo_codes'
    id = db.Column(db.BigInteger, primary_key=True)
    code = db.Column(db.String(50), unique=True, nullable=False)
    discount_percent = db.Column(db.Integer, nullable=False)  # e.g., 20 for 20% off
    is_active = db.Column(db.Boolean, nullable=False, default=True)
    created_at = db.Column(db.DateTime(timezone=True), nullable=False, server_default=db.func.now())


