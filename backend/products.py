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

@products_bp.route('/products', methods=["GET"])
def get_products():
    """Get all active products with their brand, category, and images"""
    try:
        products = Product.query.filter_by(is_active=True).all()
        
        products_list = []
        for product in products:
            # Get first image or None
            first_image = product.images[0].url if product.images else None
            
            products_list.append({
                'id': product.id,
                'title': product.title,
                'slug': product.slug,
                'description': product.description,
                'price_cents': product.price_cents,
                'currency': product.currency,
                'stock': product.stock,
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
                'images': [
                    {
                        'url': img.image_url,
                        'alt': img.alt_text,
                        'order': img.display_order
                    } for img in product.images
                ],
                'created_at': product.created_at.isoformat(),
                'updated_at': product.updated_at.isoformat()
            })
        
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
        
        product_data = {
            'id': product.id,
            'title': product.title,
            'slug': product.slug,
            'description': product.description,
            'price_cents': product.price_cents,
            'currency': product.currency,
            'stock': product.stock,
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
            'images': [
                {
                    'url': img.image_url,
                    'alt': img.alt_text,
                    'order': img.display_order
                } for img in sorted(product.images, key=lambda x: x.display_order)
            ],
            'created_at': product.created_at.isoformat(),
            'updated_at': product.updated_at.isoformat()
        }
        
        return jsonify(product_data), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@products_bp.route('/products/slug/<string:slug>', methods=["GET"])
def get_product_by_slug(slug):
    """Get a single product by slug"""
    try:
        product = Product.query.filter_by(slug=slug, is_active=True).first()
        
        if not product:
            return jsonify({'error': 'Product not found'}), 404
        
        product_data = {
            'id': product.id,
            'title': product.title,
            'slug': product.slug,
            'description': product.description,
            'price_cents': product.price_cents,
            'currency': product.currency,
            'stock': product.stock,
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
        
        return jsonify(product_data), 200
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
        
        products_list = []
        for product in products:
            first_image = product.images[0].url if product.images else None
            
            products_list.append({
                'id': product.id,
                'title': product.title,
                'slug': product.slug,
                'description': product.description,
                'price_cents': product.price_cents,
                'currency': product.currency,
                'stock': product.stock,
                'brand': {
                    'id': product.brand.id,
                    'name': product.brand.name,
                    'slug': product.brand.slug
                } if product.brand else None,
                'category': {
                    'id': category.id,
                    'name': category.name,
                    'slug': category.slug
                },
                'images': [
                    {
                        'url': img.url,
                        'alt': img.alt_text,
                        'order': img.position
                    } for img in product.images
                ],
                'created_at': product.created_at.isoformat(),
                'updated_at': product.updated_at.isoformat()
            })
        
        return jsonify({'products': products_list, 'category': {'name': category.name, 'slug': category.slug}}), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    





