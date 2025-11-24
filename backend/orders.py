from models import User, Order, OrderItem, Cart
from extensions import db
from flask import Blueprint, request, jsonify
from flask_jwt_extended.exceptions import NoAuthorizationError
from jwt import ExpiredSignatureError
from flask_jwt_extended import verify_jwt_in_request, get_jwt_identity


"""
POST /order: Create new order from cart
GET /order/:id: Fetch order details
DELETE /order/:id: Cancel order
"""


order_bp = Blueprint("order", __name__, url_prefix="/order")



@order_bp.route('', methods=["POST"])
def post_user_order():
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
        cart = Cart.query.filter_by(user_id=user.id).first()

        new_order = Order(user_id=user.id)
        db.session.add(new_order)
        db.session.flush()

        order_lists = []
        for item in cart.cartitems:
            order_list = OrderItem(
                order_id = new_order.id,
                product_id = item.product_id,
                quantity = item.quantity
            )


            db.session.add(order_lists)
            order_lists.append(order_list)
        
    # Clear the cart change
    for item in cart.cartitems:
        db.session.delete(item)

    db.session.commit()

    items = []
    for o in order_lists:
        items.append({
            'order_id': new_order.id,
            'id': o.id,
            'product_id': o.product_id,
            'quantity': o.quantity
        })

    return jsonify({'items': items}), 201
    



@order_bp.route('/<int:order_id>', methods=["GET"])
def get_user_order(order_id):
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
        order = Order.query.filter_by(id=order_id, user_id=user.id).first()

        order_item = [] # Initialize an empty list to hold item data
        for i in order.orderitems:
            order_item.append({
                'product_id': i.product_id,
                'quantity': i.quantity
            })

        return jsonify({
            'order_id': order.id,
            'items': order_item
        }), 200



@order_bp.route('/<int:order_id>', methods=["DELETE"])
def delete_user_order(order_id):
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
    order = Order.query.filter_by(id=order_id, user_id=user.id).first()

    if not order:
        return jsonify({'error': 'Order not found'}), 404


    db.session.delete(order)
    db.session.commit()

    return jsonify({"message": "Order has been deleted"}), 200

