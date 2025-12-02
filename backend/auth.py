from flask import Blueprint, jsonify, request, redirect
from models import User
from extensions import db, limiter
from werkzeug.security import generate_password_hash, check_password_hash
from flask_jwt_extended import (
    create_access_token, 
    create_refresh_token,
    jwt_required, 
    get_jwt_identity,
    get_jwt
)
from functools import wraps
from datetime import timedelta
import os
import httpx

auth_bp = Blueprint("auth", __name__, url_prefix="/api/auth")

# Token expiration times
ACCESS_TOKEN_EXPIRES = timedelta(hours=1)
REFRESH_TOKEN_EXPIRES = timedelta(days=30)


def admin_required():
    """
    Decorator that ensures the current user is an admin.
    Must be used AFTER @jwt_required() decorator.
    Returns 403 Forbidden if user is not an admin.
    """
    def decorator(fn):
        @wraps(fn)
        def wrapper(*args, **kwargs):
            claims = get_jwt()
            if claims.get('role') != 'admin':
                return jsonify({'error': 'Admin access required'}), 403
            return fn(*args, **kwargs)
        return wrapper
    return decorator


def create_tokens(user):
    """Create access and refresh tokens for a user"""
    identity = str(user.id)
    additional_claims = {
        'email': user.email,
        'full_name': user.full_name,
        'role': user.role
    }
    access_token = create_access_token(
        identity=identity, 
        additional_claims=additional_claims,
        expires_delta=ACCESS_TOKEN_EXPIRES
    )
    refresh_token = create_refresh_token(
        identity=identity,
        expires_delta=REFRESH_TOKEN_EXPIRES
    )
    return access_token, refresh_token


def user_to_dict(user):
    """Convert user to dictionary for API response"""
    return {
        'id': str(user.id),
        'email': user.email,
        'full_name': user.full_name,
        'role': user.role,
        'oauth_provider': user.oauth_provider
    }


@auth_bp.route('/register', methods=['POST'])
@limiter.limit("5 per hour")  # Rate limit: max 5 registrations per IP per hour
def register():
    """Register a new user with email and password"""
    data = request.get_json()
    
    email = data.get('email', '').lower().strip()
    full_name = data.get('full_name', '').strip()
    password = data.get('password')

    # Validation
    if not email or not full_name or not password:
        return jsonify({"error": "Email, full name, and password are required"}), 400

    if len(password) < 8:
        return jsonify({"error": "Password must be at least 8 characters long"}), 400
    
    if len(full_name) < 2:
        return jsonify({"error": "Full name must be at least 2 characters long"}), 400

    # Check if email already exists
    if User.query.filter_by(email=email).first():
        return jsonify({'error': 'An account with this email already exists'}), 409

    # Create new user
    hashed_password = generate_password_hash(password)
    new_user = User(
        email=email, 
        full_name=full_name, 
        password_hash=hashed_password,
        role='customer'
    )
    
    db.session.add(new_user)
    db.session.commit()

    # Create tokens
    access_token, refresh_token = create_tokens(new_user)

    return jsonify({
        'message': 'User registered successfully',
        'user': user_to_dict(new_user),
        'access_token': access_token,
        'refresh_token': refresh_token
    }), 201


@auth_bp.route('/login', methods=['POST'])
@limiter.limit("10 per minute")  # Rate limit login attempts
def login():
    """Login with email and password"""
    data = request.get_json()
    
    email = data.get('email', '').lower().strip()
    password = data.get('password')

    if not email or not password:
        return jsonify({'error': 'Email and password are required'}), 400

    # Find user by email
    user = User.query.filter_by(email=email).first()

    if not user:
        return jsonify({'error': 'Invalid email or password'}), 401
    
    # Check if user signed up with OAuth (no password)
    if user.oauth_provider and not user.password_hash:
        return jsonify({
            'error': f'This account uses {user.oauth_provider} sign-in. Please use the "{user.oauth_provider.title()}" button to log in.'
        }), 401

    # Verify password
    if not check_password_hash(user.password_hash, password):
        return jsonify({'error': 'Invalid email or password'}), 401

    # Check if user is active
    if not user.is_active:
        return jsonify({'error': 'This account has been deactivated'}), 401

    # Create tokens
    access_token, refresh_token = create_tokens(user)

    return jsonify({
        'message': 'Login successful',
        'user': user_to_dict(user),
        'access_token': access_token,
        'refresh_token': refresh_token
    }), 200


@auth_bp.route('/refresh', methods=['POST'])
@jwt_required(refresh=True)
def refresh():
    """Get a new access token using refresh token"""
    current_user_id = get_jwt_identity()
    user = User.query.get(current_user_id)
    
    if not user or not user.is_active:
        return jsonify({'error': 'User not found or inactive'}), 401

    additional_claims = {
        'email': user.email,
        'full_name': user.full_name,
        'role': user.role
    }
    
    access_token = create_access_token(
        identity=current_user_id,
        additional_claims=additional_claims,
        expires_delta=ACCESS_TOKEN_EXPIRES
    )
    
    return jsonify({'access_token': access_token}), 200


@auth_bp.route('/me', methods=['GET'])
@jwt_required()
def get_current_user():
    """Get current authenticated user's info"""
    current_user_id = get_jwt_identity()
    user = User.query.get(current_user_id)
    
    if not user:
        return jsonify({'error': 'User not found'}), 404
    
    return jsonify({'user': user_to_dict(user)}), 200


@auth_bp.route('/logout', methods=['POST'])
@jwt_required()
def logout():
    """Logout user (client should delete tokens)"""
    return jsonify({'message': 'Logout successful'}), 200


# ==================== OAUTH ROUTES ====================

@auth_bp.route('/google', methods=['GET'])
def google_login():
    """Redirect to Google OAuth"""
    client_id = os.getenv('GOOGLE_CLIENT_ID')
    redirect_uri = os.getenv('GOOGLE_REDIRECT_URI', 'http://localhost:5000/api/auth/google/callback')
    
    if not client_id:
        return jsonify({'error': 'Google OAuth not configured'}), 500
    
    google_auth_url = (
        f"https://accounts.google.com/o/oauth2/v2/auth?"
        f"client_id={client_id}&"
        f"redirect_uri={redirect_uri}&"
        f"response_type=code&"
        f"scope=openid%20email%20profile&"
        f"access_type=offline"
    )
    
    return jsonify({'auth_url': google_auth_url}), 200


@auth_bp.route('/google/callback', methods=['GET', 'POST'])
def google_callback():
    """Handle Google OAuth callback"""
    if request.method == 'GET':
        code = request.args.get('code')
    else:
        code = request.json.get('code')
    
    if not code:
        return jsonify({'error': 'Authorization code missing'}), 400

    client_id = os.getenv('GOOGLE_CLIENT_ID')
    client_secret = os.getenv('GOOGLE_CLIENT_SECRET')
    redirect_uri = os.getenv('GOOGLE_REDIRECT_URI', 'http://localhost:5000/api/auth/google/callback')
    
    try:
        # Exchange code for tokens
        token_response = httpx.post(
            'https://oauth2.googleapis.com/token',
            data={
                'code': code,
                'client_id': client_id,
                'client_secret': client_secret,
                'redirect_uri': redirect_uri,
                'grant_type': 'authorization_code'
            }
        )
        token_data = token_response.json()
        
        if 'error' in token_data:
            return jsonify({'error': token_data.get('error_description', 'Failed to get tokens')}), 400
        
        # Get user info from Google
        access_token = token_data['access_token']
        user_response = httpx.get(
            'https://www.googleapis.com/oauth2/v2/userinfo',
            headers={'Authorization': f'Bearer {access_token}'}
        )
        user_info = user_response.json()
        
        google_id = user_info['id']
        email = user_info['email'].lower()
        full_name = user_info.get('name', email.split('@')[0])
        
        # Check if user exists with this Google ID
        user = User.query.filter_by(oauth_provider='google', oauth_id=google_id).first()
        
        if not user:
            # Check if email exists (user signed up with email/password)
            user = User.query.filter_by(email=email).first()
            if user:
                # Link Google to existing account
                user.oauth_provider = 'google'
                user.oauth_id = google_id
                db.session.commit()
            else:
                # Create new user
                user = User(
                    email=email,
                    full_name=full_name,
                    oauth_provider='google',
                    oauth_id=google_id,
                    role='customer'
                )
                db.session.add(user)
                db.session.commit()
        
        # Create tokens
        jwt_access_token, jwt_refresh_token = create_tokens(user)
        
        # For GET requests (redirect from Google), redirect to frontend with tokens
        if request.method == 'GET':
            frontend_url = os.getenv('FRONTEND_URL', 'http://localhost:3000')
            return redirect(
                f"{frontend_url}/auth/callback?"
                f"access_token={jwt_access_token}&"
                f"refresh_token={jwt_refresh_token}"
            )
        
        return jsonify({
            'message': 'Google login successful',
            'user': user_to_dict(user),
            'access_token': jwt_access_token,
            'refresh_token': jwt_refresh_token
        }), 200
        
    except Exception as e:
        return jsonify({'error': f'OAuth error: {str(e)}'}), 500


@auth_bp.route('/github', methods=['GET'])
def github_login():
    """Redirect to GitHub OAuth"""
    client_id = os.getenv('GITHUB_CLIENT_ID')
    redirect_uri = os.getenv('GITHUB_REDIRECT_URI', 'http://localhost:5000/api/auth/github/callback')
    
    if not client_id:
        return jsonify({'error': 'GitHub OAuth not configured'}), 500
    
    github_auth_url = (
        f"https://github.com/login/oauth/authorize?"
        f"client_id={client_id}&"
        f"redirect_uri={redirect_uri}&"
        f"scope=user:email"
    )
    
    return jsonify({'auth_url': github_auth_url}), 200


@auth_bp.route('/github/callback', methods=['GET', 'POST'])
def github_callback():
    """Handle GitHub OAuth callback"""
    if request.method == 'GET':
        code = request.args.get('code')
    else:
        code = request.json.get('code')
    
    if not code:
        return jsonify({'error': 'Authorization code missing'}), 400

    client_id = os.getenv('GITHUB_CLIENT_ID')
    client_secret = os.getenv('GITHUB_CLIENT_SECRET')
    
    try:
        # Exchange code for access token
        token_response = httpx.post(
            'https://github.com/login/oauth/access_token',
            headers={'Accept': 'application/json'},
            data={
                'code': code,
                'client_id': client_id,
                'client_secret': client_secret
            }
        )
        token_data = token_response.json()
        
        if 'error' in token_data:
            return jsonify({'error': token_data.get('error_description', 'Failed to get token')}), 400
        
        access_token = token_data['access_token']
        
        # Get user info from GitHub
        user_response = httpx.get(
            'https://api.github.com/user',
            headers={
                'Authorization': f'Bearer {access_token}',
                'Accept': 'application/json'
            }
        )
        user_info = user_response.json()
        
        github_id = str(user_info['id'])
        full_name = user_info.get('name') or user_info.get('login')
        
        # Get primary email (might be private)
        email = user_info.get('email')
        if not email:
            emails_response = httpx.get(
                'https://api.github.com/user/emails',
                headers={
                    'Authorization': f'Bearer {access_token}',
                    'Accept': 'application/json'
                }
            )
            emails = emails_response.json()
            for e in emails:
                if e.get('primary'):
                    email = e['email']
                    break
            if not email and emails:
                email = emails[0]['email']
        
        if not email:
            return jsonify({'error': 'Could not retrieve email from GitHub'}), 400
        
        email = email.lower()
        
        # Check if user exists with this GitHub ID
        user = User.query.filter_by(oauth_provider='github', oauth_id=github_id).first()
        
        if not user:
            user = User.query.filter_by(email=email).first()
            if user:
                user.oauth_provider = 'github'
                user.oauth_id = github_id
                db.session.commit()
            else:
                user = User(
                    email=email,
                    full_name=full_name,
                    oauth_provider='github',
                    oauth_id=github_id,
                    role='customer'
                )
                db.session.add(user)
                db.session.commit()
        
        jwt_access_token, jwt_refresh_token = create_tokens(user)
        
        if request.method == 'GET':
            frontend_url = os.getenv('FRONTEND_URL', 'http://localhost:3000')
            return redirect(
                f"{frontend_url}/auth/callback?"
                f"access_token={jwt_access_token}&"
                f"refresh_token={jwt_refresh_token}"
            )
        
        return jsonify({
            'message': 'GitHub login successful',
            'user': user_to_dict(user),
            'access_token': jwt_access_token,
            'refresh_token': jwt_refresh_token
        }), 200
        
    except Exception as e:
        return jsonify({'error': f'OAuth error: {str(e)}'}), 500


# ==================== ADMIN ROUTES ====================

@auth_bp.route('/admin/verify', methods=['GET'])
@jwt_required()
@admin_required()
def verify_admin():
    """
    Verify that the current user is an admin.
    This endpoint is protected - only admins can access it.
    Returns 403 if not admin, 200 if admin.
    """
    claims = get_jwt()
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    
    if not user:
        return jsonify({'error': 'User not found'}), 404
    
    return jsonify({
        'message': 'Admin access verified',
        'admin': {
            'id': str(user.id),
            'email': user.email,
            'full_name': user.full_name,
            'role': user.role
        }
    }), 200
