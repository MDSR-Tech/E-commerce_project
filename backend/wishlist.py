from flask import Blueprint, jsonify, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from extensions import db
from models import Wishlist, WishlistItem, Product

wishlist_bp = Blueprint('wishlist', __name__, url_prefix='/api/wishlist')


def get_or_create_wishlist(user_id):
    """Get user's wishlist or create one if it doesn't exist"""
    wishlist = Wishlist.query.filter_by(user_id=user_id).first()
    if not wishlist:
        wishlist = Wishlist(user_id=user_id)
        db.session.add(wishlist)
        db.session.commit()
    return wishlist


def product_to_dict(product):
    """Convert product to dictionary with sale info"""
    primary_image = next((img for img in product.images if img.is_primary), None)
    if not primary_image and product.images:
        primary_image = product.images[0]
    
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
        } if primary_image else None
    }


@wishlist_bp.route('', methods=['GET'])
@jwt_required()
def get_wishlist():
    """Get all items in user's wishlist"""
    user_id = get_jwt_identity()
    wishlist = get_or_create_wishlist(user_id)
    
    items = []
    for item in wishlist.items:
        if item.product and item.product.is_active:
            items.append({
                'id': item.id,
                'product_id': item.product_id,
                'added_at': item.added_at.isoformat(),
                'product': product_to_dict(item.product)
            })
    
    return jsonify({
        'wishlist_id': wishlist.id,
        'items': items,
        'count': len(items)
    }), 200


@wishlist_bp.route('/ids', methods=['GET'])
@jwt_required()
def get_wishlist_product_ids():
    """Get just the product IDs in user's wishlist (for checking if items are wishlisted)"""
    user_id = get_jwt_identity()
    wishlist = get_or_create_wishlist(user_id)
    
    product_ids = [item.product_id for item in wishlist.items]
    
    return jsonify({
        'product_ids': product_ids
    }), 200


@wishlist_bp.route('/add', methods=['POST'])
@jwt_required()
def add_to_wishlist():
    """Add a product to wishlist"""
    user_id = get_jwt_identity()
    data = request.get_json()
    
    product_id = data.get('product_id')
    if not product_id:
        return jsonify({'error': 'Product ID is required'}), 400
    
    # Check if product exists and is active
    product = Product.query.filter_by(id=product_id, is_active=True).first()
    if not product:
        return jsonify({'error': 'Product not found'}), 404
    
    wishlist = get_or_create_wishlist(user_id)
    
    # Check if already in wishlist
    existing = WishlistItem.query.filter_by(
        wishlist_id=wishlist.id,
        product_id=product_id
    ).first()
    
    if existing:
        return jsonify({
            'message': 'Product already in wishlist',
            'product_id': product_id
        }), 200
    
    # Add to wishlist
    item = WishlistItem(
        wishlist_id=wishlist.id,
        product_id=product_id
    )
    db.session.add(item)
    db.session.commit()
    
    return jsonify({
        'message': 'Added to wishlist',
        'product_id': product_id,
        'item_id': item.id
    }), 201


@wishlist_bp.route('/remove', methods=['DELETE'])
@jwt_required()
def remove_from_wishlist():
    """Remove a product from wishlist"""
    user_id = get_jwt_identity()
    data = request.get_json()
    
    product_id = data.get('product_id')
    if not product_id:
        return jsonify({'error': 'Product ID is required'}), 400
    
    wishlist = Wishlist.query.filter_by(user_id=user_id).first()
    if not wishlist:
        return jsonify({'error': 'Wishlist not found'}), 404
    
    item = WishlistItem.query.filter_by(
        wishlist_id=wishlist.id,
        product_id=product_id
    ).first()
    
    if not item:
        return jsonify({'error': 'Product not in wishlist'}), 404
    
    db.session.delete(item)
    db.session.commit()
    
    return jsonify({
        'message': 'Removed from wishlist',
        'product_id': product_id
    }), 200


@wishlist_bp.route('/toggle', methods=['POST'])
@jwt_required()
def toggle_wishlist():
    """Toggle a product in wishlist (add if not present, remove if present)"""
    user_id = get_jwt_identity()
    data = request.get_json()
    
    product_id = data.get('product_id')
    if not product_id:
        return jsonify({'error': 'Product ID is required'}), 400
    
    # Check if product exists and is active
    product = Product.query.filter_by(id=product_id, is_active=True).first()
    if not product:
        return jsonify({'error': 'Product not found'}), 404
    
    wishlist = get_or_create_wishlist(user_id)
    
    # Check if in wishlist
    existing = WishlistItem.query.filter_by(
        wishlist_id=wishlist.id,
        product_id=product_id
    ).first()
    
    if existing:
        # Remove from wishlist
        db.session.delete(existing)
        db.session.commit()
        return jsonify({
            'message': 'Removed from wishlist',
            'product_id': product_id,
            'action': 'removed',
            'in_wishlist': False
        }), 200
    else:
        # Add to wishlist
        item = WishlistItem(
            wishlist_id=wishlist.id,
            product_id=product_id
        )
        db.session.add(item)
        db.session.commit()
        return jsonify({
            'message': 'Added to wishlist',
            'product_id': product_id,
            'item_id': item.id,
            'action': 'added',
            'in_wishlist': True
        }), 200


@wishlist_bp.route('/check/<int:product_id>', methods=['GET'])
@jwt_required()
def check_in_wishlist(product_id):
    """Check if a specific product is in wishlist"""
    user_id = get_jwt_identity()
    
    wishlist = Wishlist.query.filter_by(user_id=user_id).first()
    if not wishlist:
        return jsonify({'in_wishlist': False}), 200
    
    exists = WishlistItem.query.filter_by(
        wishlist_id=wishlist.id,
        product_id=product_id
    ).first() is not None
    
    return jsonify({'in_wishlist': exists}), 200
