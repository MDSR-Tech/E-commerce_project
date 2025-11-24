from flask import Blueprint, jsonify, request
from models import User
from extensions import db
from werkzeug.security import generate_password_hash, check_password_hash
from flask_jwt_extended import create_access_token

auth_bp = Blueprint("auth", __name__, url_prefix="/auth")


@auth_bp.route('/register', methods=['POST'])
def register():
    email = request.json.get('email')
    username = request.json.get('username')
    password = request.json.get('password') 

    if not email or not username or not password:
        return jsonify({"error": "email, username and password are required"}), 400

    # Note: User model uses email for login, not username
    if User.query.filter_by(email=email).first():
        return jsonify({'error' : 'email already exists'}), 409

    hashed_password = generate_password_hash(password)
    new_user = User(email=email, full_name=username, password_hash=hashed_password)
    db.session.add(new_user)
    db.session.commit()

    return jsonify({'message' : 'User registered successfully'}), 201


@auth_bp.route('/login', methods=['POST'])
def login():
    username = request.json.get('username')
    password = request.json.get('password')

    if not username or not password:
        return jsonify({'error': 'username and password required'}), 400
    

    # Note: User model uses email for login
    user = User.query.filter_by(email=username).first()

    if user and check_password_hash(user.password_hash, password):
        token = create_access_token(identity=user.email)
        return jsonify({'message' : f'{token} is a success'}), 200
    else:
        return jsonify({'error': 'Invalid credentials'}), 401
