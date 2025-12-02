from flask import Blueprint, jsonify, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from extensions import db, limiter
from models import Cart, CartItem, Product, PromoCode


carts_bp = Blueprint('cart', __name__, url_prefix='/api/cart')


def get_or_create_cart(user_id):
    """Get user's cart or create one if it doesn't exist"""
    cart = Cart.query.filter_by(user_id=user_id).first()
    if not cart:
        cart = Cart(user_id=user_id)
        db.session.add(cart)
        db.session.commit()
    return cart


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


def get_effective_price(product):
    """Get the effective price (sale price if on sale, otherwise regular price)"""
    if product.is_on_sale and product.sale_price_cents:
        return product.sale_price_cents
    return product.price_cents


@carts_bp.route('', methods=['GET'])
@jwt_required()
def get_cart():
    """Get all items in user's cart with full product details"""
    user_id = get_jwt_identity()
    cart = get_or_create_cart(user_id)
    
    items = []
    subtotal_cents = 0
    original_subtotal_cents = 0  # Track original price for showing savings
    total_items = 0
    
    for item in cart.items:
        if item.product and item.product.is_active:
            # Use sale price if available
            effective_price = get_effective_price(item.product)
            line_total = effective_price * item.quantity
            original_line_total = item.product.price_cents * item.quantity
            
            subtotal_cents += line_total
            original_subtotal_cents += original_line_total
            total_items += item.quantity
            
            items.append({
                'id': item.id,
                'product_id': item.product_id,
                'quantity': item.quantity,
                'unit_price_cents': effective_price,
                'original_price_cents': item.product.price_cents,
                'line_total_cents': line_total,
                'original_line_total_cents': original_line_total,
                'added_at': item.added_at.isoformat(),
                'product': product_to_dict(item.product)
            })
    
    # Calculate tax (13% HST for Ontario)
    tax_rate = 0.13
    tax_cents = int(subtotal_cents * tax_rate)
    
    # Shipping (free over $100, otherwise $9.99)
    shipping_cents = 0 if subtotal_cents >= 10000 else 999
    
    # Total
    total_cents = subtotal_cents + tax_cents + shipping_cents
    
    # Calculate savings from sales
    sale_savings_cents = original_subtotal_cents - subtotal_cents
    
    return jsonify({
        'cart_id': cart.id,
        'items': items,
        'item_count': len(items),
        'total_items': total_items,
        'subtotal_cents': subtotal_cents,
        'original_subtotal_cents': original_subtotal_cents,
        'sale_savings_cents': sale_savings_cents,
        'tax_cents': tax_cents,
        'tax_rate': tax_rate,
        'shipping_cents': shipping_cents,
        'total_cents': total_cents,
        'currency': 'CAD'
    }), 200


@carts_bp.route('/count', methods=['GET'])
@limiter.exempt
@jwt_required()
def get_cart_count():
    """Get just the count of items in cart (for navbar badge)"""
    user_id = get_jwt_identity()
    cart = Cart.query.filter_by(user_id=user_id).first()
    
    if not cart:
        return jsonify({'count': 0, 'total_items': 0}), 200
    
    # Count unique items and total quantity
    count = len(cart.items)
    total_items = sum(item.quantity for item in cart.items)
    
    return jsonify({
        'count': count,
        'total_items': total_items
    }), 200


@carts_bp.route('/add', methods=['POST'])
@jwt_required()
def add_to_cart():
    """Add a product to cart or update quantity if already exists"""
    user_id = get_jwt_identity()
    data = request.get_json()
    
    product_id = data.get('product_id')
    quantity = data.get('quantity', 1)
    
    if not product_id:
        return jsonify({'error': 'Product ID is required'}), 400
    
    if quantity < 1:
        return jsonify({'error': 'Quantity must be at least 1'}), 400
    
    # Check if product exists and is active
    product = Product.query.filter_by(id=product_id, is_active=True).first()
    if not product:
        return jsonify({'error': 'Product not found'}), 404
    
    # Check stock
    if quantity > product.stock:
        return jsonify({'error': f'Only {product.stock} items available'}), 400
    
    cart = get_or_create_cart(user_id)
    
    # Get effective price (sale price if on sale)
    effective_price = get_effective_price(product)
    
    # Check if already in cart
    existing = CartItem.query.filter_by(
        cart_id=cart.id,
        product_id=product_id
    ).first()
    
    if existing:
        # Update quantity
        new_quantity = existing.quantity + quantity
        if new_quantity > product.stock:
            return jsonify({'error': f'Cannot add more. Only {product.stock} items available, you have {existing.quantity} in cart'}), 400
        
        existing.quantity = new_quantity
        existing.unit_price_cents = effective_price  # Update to current sale price
        db.session.commit()
        
        return jsonify({
            'message': 'Cart updated',
            'product_id': product_id,
            'quantity': existing.quantity,
            'item_id': existing.id,
            'action': 'updated'
        }), 200
    else:
        # Add new item
        item = CartItem(
            cart_id=cart.id,
            product_id=product_id,
            quantity=quantity,
            unit_price_cents=effective_price
        )
        db.session.add(item)
        db.session.commit()
        
        return jsonify({
            'message': 'Added to cart',
            'product_id': product_id,
            'quantity': quantity,
            'item_id': item.id,
            'action': 'added'
        }), 201


@carts_bp.route('/update', methods=['PUT'])
@jwt_required()
def update_cart_item():
    """Update quantity of an item in cart"""
    user_id = get_jwt_identity()
    data = request.get_json()
    
    product_id = data.get('product_id')
    quantity = data.get('quantity')
    
    if not product_id:
        return jsonify({'error': 'Product ID is required'}), 400
    
    if quantity is None or quantity < 1:
        return jsonify({'error': 'Quantity must be at least 1'}), 400
    
    cart = Cart.query.filter_by(user_id=user_id).first()
    if not cart:
        return jsonify({'error': 'Cart not found'}), 404
    
    item = CartItem.query.filter_by(
        cart_id=cart.id,
        product_id=product_id
    ).first()
    
    if not item:
        return jsonify({'error': 'Item not in cart'}), 404
    
    # Check stock
    product = Product.query.get(product_id)
    if quantity > product.stock:
        return jsonify({'error': f'Only {product.stock} items available'}), 400
    
    item.quantity = quantity
    db.session.commit()
    
    return jsonify({
        'message': 'Cart updated',
        'product_id': product_id,
        'quantity': quantity
    }), 200


@carts_bp.route('/remove', methods=['DELETE'])
@jwt_required()
def remove_from_cart():
    """Remove a product from cart"""
    user_id = get_jwt_identity()
    data = request.get_json()
    
    product_id = data.get('product_id')
    if not product_id:
        return jsonify({'error': 'Product ID is required'}), 400
    
    cart = Cart.query.filter_by(user_id=user_id).first()
    if not cart:
        return jsonify({'error': 'Cart not found'}), 404
    
    item = CartItem.query.filter_by(
        cart_id=cart.id,
        product_id=product_id
    ).first()
    
    if not item:
        return jsonify({'error': 'Item not in cart'}), 404
    
    db.session.delete(item)
    db.session.commit()
    
    return jsonify({
        'message': 'Removed from cart',
        'product_id': product_id
    }), 200


@carts_bp.route('/clear', methods=['DELETE'])
@jwt_required()
def clear_cart():
    """Remove all items from cart"""
    user_id = get_jwt_identity()
    
    cart = Cart.query.filter_by(user_id=user_id).first()
    if not cart:
        return jsonify({'message': 'Cart already empty'}), 200
    
    CartItem.query.filter_by(cart_id=cart.id).delete()
    db.session.commit()
    
    return jsonify({'message': 'Cart cleared'}), 200


@carts_bp.route('/apply-promo', methods=['POST'])
@jwt_required()
def apply_promo():
    """Apply a promo code to cart"""
    user_id = get_jwt_identity()
    data = request.get_json()
    
    promo_code = data.get('promo_code', '').strip().upper()
    
    if not promo_code:
        return jsonify({'error': 'Promo code is required'}), 400
    
    # Look up promo code in database
    promo = PromoCode.query.filter_by(code=promo_code, is_active=True).first()
    
    if not promo:
        return jsonify({'error': 'Invalid promo code'}), 400
    
    return jsonify({
        'message': 'Promo code applied!',
        'promo_code': promo.code,
        'discount_percent': promo.discount_percent
    }), 200






