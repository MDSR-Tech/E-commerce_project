from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from models import Product, Category, Brand, ProductImage
from extensions import db

products_bp = Blueprint("products", __name__, url_prefix="/api")

"""
GET /api/products: List all active products
GET /api/products/:id: Fetch product details by ID
GET /api/products/slug/:slug: Fetch product details by slug
GET /api/categories/:slug/products: Fetch products by category
"""

def product_to_dict(product):
    """Convert a product to dictionary with sale info"""
    # Get first/primary image
    primary_image = next((img for img in product.images if img.is_primary), None)
    if not primary_image and product.images:
        primary_image = product.images[0]
    
    # Calculate sale info
    is_on_sale = product.is_on_sale
    sale_percent = product.effective_sale_percent if is_on_sale else None
    sale_price_cents = product.sale_price_cents if is_on_sale else None
    
    return {
        'id': product.id,
        'title': product.title,
        'slug': product.slug,
        'description': product.description,
        'price_cents': product.price_cents,
        'currency': product.currency,
        'stock': product.stock,
        # Sale info
        'is_on_sale': is_on_sale,
        'sale_percent': sale_percent,
        'sale_price_cents': sale_price_cents,
        'brand': {
            'id': product.brand.id,
            'name': product.brand.name,
            'slug': product.brand.slug
        } if product.brand else None,
        'category': {
            'id': product.category.id,
            'name': product.category.name,
            'slug': product.category.slug
        } if product.category else None,
        'image': {
            'url': primary_image.url,
            'alt_text': primary_image.alt_text
        } if primary_image else None,
        'images': [
            {
                'url': img.url,
                'alt': img.alt_text,
                'order': img.position
            } for img in sorted(product.images, key=lambda x: x.position)
        ],
        'created_at': product.created_at.isoformat(),
        'updated_at': product.updated_at.isoformat()
    }

@products_bp.route('/products', methods=["GET"])
def get_products():
    """Get all active products with their brand, category, and images"""
    try:
        products = Product.query.filter_by(is_active=True).order_by(Product.id).all()
        products_list = [product_to_dict(p) for p in products]
        return jsonify({'products': products_list}), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@products_bp.route('/products/<int:product_id>', methods=["GET"])
def get_product_by_id(product_id):
    """Get a single product by ID"""
    try:
        product = Product.query.filter_by(id=product_id, is_active=True).first()
        
        if not product:
            return jsonify({'error': 'Product not found'}), 404
        
        return jsonify(product_to_dict(product)), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@products_bp.route('/products/slug/<string:slug>', methods=["GET"])
def get_product_by_slug(slug):
    """Get a single product by slug"""
    try:
        product = Product.query.filter_by(slug=slug, is_active=True).first()
        
        if not product:
            return jsonify({'error': 'Product not found'}), 404
        
        return jsonify(product_to_dict(product)), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@products_bp.route('/categories/<string:category_slug>/products', methods=["GET"])
def get_products_by_category(category_slug):
    """Get all products in a specific category"""
    try:
        category = Category.query.filter_by(slug=category_slug).first()
        
        if not category:
            return jsonify({'error': 'Category not found'}), 404
        
        products = Product.query.filter_by(category_id=category.id, is_active=True).all()
        products_list = [product_to_dict(p) for p in products]
        
        return jsonify({
            'products': products_list, 
            'category': {
                'name': category.name, 
                'slug': category.slug,
                'sale_percent': category.sale_percent
            }
        }), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    





