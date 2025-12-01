from models import User, CartItem, Cart
from extensions import db
from flask import Blueprint, request, jsonify
from flask_jwt_extended.exceptions import NoAuthorizationError
from jwt import ExpiredSignatureError
from flask_jwt_extended import verify_jwt_in_request, get_jwt_identity


carts_bp = Blueprint("cart", __name__, url_prefix="/cart")

@carts_bp.route('', methods=["GET"])
def get_user_cart():
    if request.method == "GET":
        token = request.headers.get('Authorization')
        if not token:
            return jsonify({'error': 'Token is missing'}), 400
        
        if token.startswith('Bearer '):
            token = token[7:]

        try:
            verify_jwt_in_request()
            username = get_jwt_identity()
        except ExpiredSignatureError:
            return jsonify({'error': 'Token has expired'}), 401
        except NoAuthorizationError:
            return jsonify({'error': 'Invalid token'}), 401

        user = User.query.filter_by(username=username).first()
        cartItem = CartItem.query.filter_by(user_id=user.id).all()

        item_list = [] # Initialize an empty list to hold item data
        for i in cartItem:
            item_list.append({
                'id': i.id,
                'product_id': i.product_id,
                'quantity': i.quantity
            })

        return jsonify({'cartItems': item_list}), 200
    


@carts_bp.route('', methods=["POST"])
def post_user_cart():
    if request.method == "POST":
        token = request.headers.get('Authorization')
        if not token:
            return jsonify({'error': 'Token is missing'}), 400
        
        if token.startswith('Bearer '):
            token = token[7:]

        try:
            verify_jwt_in_request()
            username = get_jwt_identity()
        except ExpiredSignatureError:
            return jsonify({'error': 'Token has expired'}), 401
        except NoAuthorizationError:
            return jsonify({'error': 'Invalid token'}), 401

        user = User.query.filter_by(username=username).first()
        data = request.get_json()
        product_id = data.get('product_id')
        quantity = data.get('quantity')

        cart_list = CartItem(user_id=user.id, product_id=product_id, quantity=quantity)

        db.session.add(cart_list)
        db.session.commit()
        
        return jsonify({
            'id': cart_list.id,
            'product_id': cart_list.product_id,
            'quantity': cart_list.quantity
        }), 201
    


@carts_bp.route('/<int:item_id>', methods=["PUT"])
def put_user_cart(item_id):
    if request.method == "PUT":
        token = request.headers.get('Authorization')
        if not token:
            return jsonify({'error': 'Token is missing'}), 400
        
        if token.startswith('Bearer '):
            token = token[7:]

        try:
            verify_jwt_in_request()
            username = get_jwt_identity()
        except ExpiredSignatureError:
            return jsonify({'error': 'Token has expired'}), 401
        except NoAuthorizationError:
            return jsonify({'error': 'Invalid token'}), 401

        data = request.get_json()
        user = User.query.filter_by(username=username).first()
        cartItem = CartItem.query.filter_by(id=item_id, user_id=user.id).first()
        
        if 'quantity' in data:
            cartItem.quantity = data['quantity']
        
        db.session.commit()
        
        return jsonify({
            'id': cartItem.id,
            'product_id': cartItem.product_id,
            'quantity': cartItem.quantity
        }), 200


@carts_bp.route('/<int:item_id>', methods=["DELETE"])
def delete_user_cart(item_id):
    token = request.headers.get('Authorization')
    if not token:
        return jsonify({'error': 'Token is missing'}), 404
    
    # Extract token
    if token.startswith('Bearer '):
        token = token[7:]

    try:
        verify_jwt_in_request()
        username = get_jwt_identity()
    except ExpiredSignatureError:
        return jsonify({'error': 'Token has expired'}), 401
    except NoAuthorizationError:
        return jsonify({'error': 'Invalid token'}), 401

    user = User.query.filter_by(username=username).first()
    cartItem = CartItem.query.filter_by(id=item_id, user_id=user.id).first()

    if not cartItem:
        return jsonify({'error': 'Item not found'}), 400


    db.session.delete(cartItem)
    db.session.commit()

    return jsonify({"message": "Item has been deleted from Cart"}), 200






