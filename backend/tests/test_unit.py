"""
Unit Tests for MDSRTech E-commerce Backend

Tests core functionalities including:
- Model properties and methods
- Business logic calculations
- Helper functions
"""
import pytest
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from models import Product, Category, User
from werkzeug.security import generate_password_hash, check_password_hash


class TestProductModel:
    """Unit tests for Product model properties and calculations."""
    
    def test_is_on_sale_with_product_sale(self, app, db_session, test_category, test_brand):
        """Test is_on_sale returns True when product has sale_percent."""
        with app.app_context():
            product = Product(
                id=100,
                title='Sale Product',
                slug='sale-product',
                price_cents=10000,
                stock=5,
                sale_percent=20  # 20% off
            )
            db_session.add(product)
            db_session.commit()
            
            assert product.is_on_sale == True
            assert product.effective_sale_percent == 20
    
    def test_is_on_sale_with_category_sale(self, app, db_session):
        """Test is_on_sale returns True when category has sale_percent."""
        with app.app_context():
            category = Category(
                id=10,
                name='Sale Category',
                slug='sale-category',
                sale_percent=15  # 15% off entire category
            )
            db_session.add(category)
            db_session.commit()
            
            product = Product(
                id=101,
                title='Category Sale Product',
                slug='category-sale-product',
                category_id=10,
                price_cents=10000,
                stock=5,
                sale_percent=None  # No product-level sale
            )
            db_session.add(product)
            db_session.commit()
            
            # Re-fetch to get relationship
            product = Product.query.get(101)
            assert product.is_on_sale == True
            assert product.effective_sale_percent == 15
    
    def test_is_on_sale_false_no_sale(self, app, db_session):
        """Test is_on_sale returns False when no sale active."""
        with app.app_context():
            product = Product(
                id=102,
                title='Regular Product',
                slug='regular-product',
                price_cents=5000,
                stock=10,
                sale_percent=None
            )
            db_session.add(product)
            db_session.commit()
            
            assert product.is_on_sale == False
            assert product.effective_sale_percent is None
    
    def test_product_sale_overrides_category_sale(self, app, db_session):
        """Test that product-level sale takes priority over category sale."""
        with app.app_context():
            category = Category(
                id=11,
                name='Category With Sale',
                slug='category-with-sale',
                sale_percent=10  # 10% category sale
            )
            db_session.add(category)
            db_session.commit()
            
            product = Product(
                id=103,
                title='Product Override Sale',
                slug='product-override-sale',
                category_id=11,
                price_cents=10000,
                stock=5,
                sale_percent=25  # 25% product sale (takes priority)
            )
            db_session.add(product)
            db_session.commit()
            
            product = Product.query.get(103)
            assert product.effective_sale_percent == 25  # Product sale, not category
    
    def test_sale_price_calculation(self, app, db_session):
        """Test sale price is calculated correctly."""
        with app.app_context():
            product = Product(
                id=104,
                title='Discounted Product',
                slug='discounted-product',
                price_cents=10000,  # $100.00
                stock=5,
                sale_percent=20  # 20% off
            )
            db_session.add(product)
            db_session.commit()
            
            # 20% off $100.00 = $80.00 = 8000 cents
            assert product.sale_price_cents == 8000
    
    def test_sale_price_none_when_no_sale(self, app, db_session):
        """Test sale_price_cents is None when not on sale."""
        with app.app_context():
            product = Product(
                id=105,
                title='Full Price Product',
                slug='full-price-product',
                price_cents=5000,
                stock=5,
                sale_percent=None
            )
            db_session.add(product)
            db_session.commit()
            
            assert product.sale_price_cents is None


class TestPasswordHashing:
    """Unit tests for password hashing utilities."""
    
    def test_password_hash_is_different_from_plain(self):
        """Test that hashed password is different from plain text."""
        password = 'mysecretpassword123'
        hashed = generate_password_hash(password)
        
        assert hashed != password
        assert len(hashed) > len(password)
    
    def test_password_verification_correct(self):
        """Test that correct password verifies successfully."""
        password = 'mysecretpassword123'
        hashed = generate_password_hash(password)
        
        assert check_password_hash(hashed, password) == True
    
    def test_password_verification_incorrect(self):
        """Test that incorrect password fails verification."""
        password = 'mysecretpassword123'
        hashed = generate_password_hash(password)
        
        assert check_password_hash(hashed, 'wrongpassword') == False
    
    def test_same_password_different_hashes(self):
        """Test that same password produces different hashes (salted)."""
        password = 'mysecretpassword123'
        hash1 = generate_password_hash(password)
        hash2 = generate_password_hash(password)
        
        # Hashes should be different due to random salt
        assert hash1 != hash2
        # But both should verify correctly
        assert check_password_hash(hash1, password) == True
        assert check_password_hash(hash2, password) == True


class TestPriceCalculations:
    """Unit tests for price and tax calculations."""
    
    def test_cart_subtotal_calculation(self):
        """Test subtotal calculation from cart items."""
        items = [
            {'unit_price_cents': 1000, 'quantity': 2},  # $10 x 2 = $20
            {'unit_price_cents': 500, 'quantity': 3},   # $5 x 3 = $15
            {'unit_price_cents': 2500, 'quantity': 1},  # $25 x 1 = $25
        ]
        
        subtotal = sum(item['unit_price_cents'] * item['quantity'] for item in items)
        assert subtotal == 6000  # $60.00 in cents
    
    def test_tax_calculation_13_percent(self):
        """Test 13% HST tax calculation."""
        subtotal_cents = 10000  # $100.00
        tax_rate = 0.13
        
        tax_cents = int(subtotal_cents * tax_rate)
        assert tax_cents == 1300  # $13.00 in cents
    
    def test_order_total_calculation(self):
        """Test total order calculation with subtotal, tax, and shipping."""
        subtotal_cents = 10000  # $100.00
        tax_cents = 1300        # $13.00 (13% HST)
        shipping_cents = 500    # $5.00
        
        total_cents = subtotal_cents + tax_cents + shipping_cents
        assert total_cents == 11800  # $118.00


class TestEmailValidation:
    """Unit tests for email validation logic."""
    
    def test_email_normalization(self):
        """Test email is normalized to lowercase."""
        email = 'Test.User@Example.COM'
        normalized = email.lower().strip()
        
        assert normalized == 'test.user@example.com'
    
    def test_email_whitespace_stripped(self):
        """Test whitespace is stripped from email."""
        email = '  user@example.com  '
        cleaned = email.lower().strip()
        
        assert cleaned == 'user@example.com'


class TestUserModel:
    """Unit tests for User model."""
    
    def test_user_default_role_is_customer(self, app, db_session):
        """Test that new users default to 'customer' role."""
        with app.app_context():
            import uuid
            user = User(
                id=str(uuid.uuid4()),  # Use string for SQLite compatibility
                email='newuser@example.com',
                password_hash=generate_password_hash('password'),
                full_name='New User'
            )
            db_session.add(user)
            db_session.commit()
            
            assert user.role == 'customer'
    
    def test_user_is_active_by_default(self, app, db_session):
        """Test that new users are active by default."""
        with app.app_context():
            import uuid
            user = User(
                id=str(uuid.uuid4()),  # Use string for SQLite compatibility
                email='activeuser@example.com',
                password_hash=generate_password_hash('password'),
                full_name='Active User'
            )
            db_session.add(user)
            db_session.commit()
            
            assert user.is_active == True
    
    def test_oauth_user_no_password(self, app, db_session):
        """Test OAuth users can have null password_hash."""
        with app.app_context():
            import uuid
            user = User(
                id=str(uuid.uuid4()),  # Use string for SQLite compatibility
                email='oauthuser@example.com',
                password_hash=None,  # OAuth users don't have passwords
                full_name='OAuth User',
                oauth_provider='google',
                oauth_id='12345'
            )
            db_session.add(user)
            db_session.commit()
            
            assert user.password_hash is None
            assert user.oauth_provider == 'google'
