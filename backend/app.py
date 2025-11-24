from flask import Flask, jsonify
from extensions import db, jwt, cors
from auth import auth_bp
from products import products_bp
from cart import carts_bp
from orders import orders_bp
import os
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

def create_app():
    app = Flask(__name__)
    
    # Get database URL from environment variable or use default
    database_url = os.getenv('DATABASE_URL', 'postgresql://postgres:[YOUR-SUPABASE-PASSWORD]@db.[YOUR-PROJECT-REF].supabase.co:5432/postgres')
    app.config['SQLALCHEMY_DATABASE_URI'] = database_url
    
    app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
    app.config['JWT_SECRET_KEY'] = os.getenv('JWT_SECRET_KEY', 'a-string-secret-at-least-256-bits-long')

    db.init_app(app)
    jwt.init_app(app)
    
    # Configure CORS
    cors_origins = os.getenv('CORS_ORIGINS', 'http://localhost:3000').split(',')
    cors.init_app(app, resources={r"/api/*": {"origins": cors_origins}})

    # Register blueprints
    app.register_blueprint(auth_bp)
    app.register_blueprint(products_bp)
    app.register_blueprint(carts_bp)
    app.register_blueprint(orders_bp)

    return app

if __name__ == "__main__":
    # Running directly: python app.py
    app = create_app()
    app.run(debug=True, port=5000)