"""
Pytest configuration and fixtures for testing the MDSRTech E-commerce backend.
"""
import pytest
import os
import sys

# Add parent directory to path for imports
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from flask import Flask
from flask_jwt_extended import JWTManager, create_access_token
from sqlalchemy import String
from extensions import db
from models import User, Product, Category, Brand, Cart, CartItem, Order, OrderItem, Wishlist, WishlistItem
from werkzeug.security import generate_password_hash
import uuid


@pytest.fixture(scope='session')
def app():
    """Create and configure a test Flask application instance."""
    app = Flask(__name__)
    
    # Test configuration
    app.config['TESTING'] = True
    app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///:memory:'
    app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
    app.config['JWT_SECRET_KEY'] = 'test-secret-key'
    app.config['WTF_CSRF_ENABLED'] = False
    
    # Initialize extensions
    db.init_app(app)
    JWTManager(app)
    
    # Import and register blueprints
    from auth import auth_bp
    from products import products_bp
    from cart import carts_bp
    from orders import order_bp
    from wishlist import wishlist_bp
    
    app.register_blueprint(auth_bp)
    app.register_blueprint(products_bp)
    app.register_blueprint(carts_bp)
    app.register_blueprint(order_bp)
    app.register_blueprint(wishlist_bp)
    
    # Patch UUID columns to use String for SQLite compatibility
    with app.app_context():
        # Modify the User.id column type for SQLite
        User.__table__.c.id.type = String(36)
        if hasattr(Product.__table__.c, 'created_by'):
            Product.__table__.c.created_by.type = String(36)
        if hasattr(Product.__table__.c, 'updated_by'):
            Product.__table__.c.updated_by.type = String(36)
        if hasattr(Cart.__table__.c, 'user_id'):
            Cart.__table__.c.user_id.type = String(36)
        if hasattr(Wishlist.__table__.c, 'user_id'):
            Wishlist.__table__.c.user_id.type = String(36)
        if hasattr(Order.__table__.c, 'user_id'):
            Order.__table__.c.user_id.type = String(36)
        
        db.create_all()
    
    yield app
    
    # Cleanup
    with app.app_context():
        db.drop_all()


@pytest.fixture(scope='function')
def client(app):
    """Create a test client for the Flask application."""
    return app.test_client()


@pytest.fixture(scope='function')
def db_session(app):
    """Create a database session for testing."""
    with app.app_context():
        # Clear all data before each test
        for table in reversed(db.metadata.sorted_tables):
            db.session.execute(table.delete())
        db.session.commit()
        yield db.session
        db.session.rollback()


@pytest.fixture
def test_user(app, db_session):
    """Create a test user."""
    with app.app_context():
        user_id = str(uuid.uuid4())
        user = User(
            id=user_id,
            email='test@example.com',
            password_hash=generate_password_hash('password123'),
            full_name='Test User',
            role='customer',
            is_active=True
        )
        db_session.add(user)
        db_session.commit()
        
        # Return a dict since we can't pass the model outside context easily
        return {
            'id': user_id,
            'email': user.email,
            'password': 'password123',
            'full_name': user.full_name,
            'role': user.role
        }


@pytest.fixture
def admin_user(app, db_session):
    """Create an admin user."""
    with app.app_context():
        user_id = str(uuid.uuid4())
        user = User(
            id=user_id,
            email='admin@example.com',
            password_hash=generate_password_hash('adminpass123'),
            full_name='Admin User',
            role='admin',
            is_active=True
        )
        db_session.add(user)
        db_session.commit()
        
        return {
            'id': user_id,
            'email': user.email,
            'password': 'adminpass123',
            'full_name': user.full_name,
            'role': user.role
        }


@pytest.fixture
def test_category(app, db_session):
    """Create a test category."""
    with app.app_context():
        category = Category(
            id=1,
            name='Electronics',
            slug='electronics',
            sale_percent=None
        )
        db_session.add(category)
        db_session.commit()
        
        return {
            'id': category.id,
            'name': category.name,
            'slug': category.slug
        }


@pytest.fixture
def test_brand(app, db_session):
    """Create a test brand."""
    with app.app_context():
        brand = Brand(
            id=1,
            name='TestBrand',
            slug='testbrand'
        )
        db_session.add(brand)
        db_session.commit()
        
        return {
            'id': brand.id,
            'name': brand.name,
            'slug': brand.slug
        }


@pytest.fixture
def test_product(app, db_session, test_category, test_brand):
    """Create a test product."""
    with app.app_context():
        # Re-fetch category and brand in this context
        category = Category.query.get(test_category['id'])
        brand = Brand.query.get(test_brand['id'])
        
        product = Product(
            id=1,
            title='Test Product',
            slug='test-product',
            brand_id=brand.id if brand else None,
            category_id=category.id if category else None,
            description='A test product description',
            price_cents=9999,  # $99.99
            currency='CAD',
            stock=10,
            is_active=True,
            sale_percent=None
        )
        db_session.add(product)
        db_session.commit()
        
        return {
            'id': product.id,
            'title': product.title,
            'slug': product.slug,
            'price_cents': product.price_cents,
            'stock': product.stock
        }


@pytest.fixture
def auth_headers(app, client, test_user):
    """Get authentication headers for test user."""
    with app.app_context():
        # Create token directly instead of going through login
        # This avoids UUID conversion issues
        access_token = create_access_token(
            identity=test_user['id'],
            additional_claims={
                'email': test_user['email'],
                'full_name': test_user['full_name'],
                'role': test_user['role']
            }
        )
        return {'Authorization': f'Bearer {access_token}'}


@pytest.fixture
def admin_auth_headers(app, client, admin_user):
    """Get authentication headers for admin user."""
    with app.app_context():
        access_token = create_access_token(
            identity=admin_user['id'],
            additional_claims={
                'email': admin_user['email'],
                'full_name': admin_user['full_name'],
                'role': admin_user['role']
            }
        )
        return {'Authorization': f'Bearer {access_token}'}
