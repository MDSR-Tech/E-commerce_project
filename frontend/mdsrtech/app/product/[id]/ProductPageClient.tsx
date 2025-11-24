'use client';

import { useRouter } from 'next/navigation';
import { ArrowLeft, Plus, Minus, ShoppingCart } from 'lucide-react';
import { useState } from 'react';
import Navbar from '../../components/Navbar';

interface Product {
  id: number;
  name: string;
  price: number;
  stock: number;
  image: string;
  description?: string;
  brand?: string;
  category?: string;
}

export default function ProductPageClient({ product }: { product: Product }) {
  const router = useRouter();
  const [quantity, setQuantity] = useState(1);

  const incrementQuantity = () => {
    if (quantity < product.stock) {
      setQuantity(quantity + 1);
    }
  };

  const decrementQuantity = () => {
    if (quantity > 1) {
      setQuantity(quantity - 1);
    }
  };

  const handleQuantityInput = (value: string) => {
    const num = parseInt(value);
    if (!isNaN(num) && num >= 1 && num <= product.stock) {
      setQuantity(num);
    } else if (value === '') {
      setQuantity(1);
    }
  };

  const handleAddToCart = () => {
    console.log(`Adding ${quantity} of ${product.name} to cart`);
  };

  return (
    <div className="min-h-screen bg-white">
      <Navbar />
      
      {/* Back Button */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-20">
        <button
          onClick={() => router.back()}
          className="flex items-center gap-2 text-gray-600 hover:text-gray-900 transition-colors group cursor-pointer"
        >
          <ArrowLeft className="w-5 h-5 group-hover:-translate-x-1 transition-transform" />
          <span className="font-medium">Back</span>
        </button>
      </div>

      {/* Product Content */}
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="flex flex-col items-center text-center space-y-8">
          {/* Product Image */}
          <div className="w-full max-w-md">
            <div className="aspect-square bg-gray-100 rounded-2xl flex items-center justify-center">
              <div className="w-48 h-48 bg-gray-200 rounded-lg flex items-center justify-center">
                <span className="text-gray-400">Product Image</span>
              </div>
            </div>
          </div>

          {/* Brand & Category */}
          {(product.brand || product.category) && (
            <div className="flex gap-3 text-sm text-gray-500">
              {product.brand && <span className="font-medium">{product.brand}</span>}
              {product.brand && product.category && <span>•</span>}
              {product.category && <span>{product.category}</span>}
            </div>
          )}

          {/* Product Name */}
          <h1 className="text-4xl font-bold text-gray-900">{product.name}</h1>

          {/* Description */}
          {product.description && (
            <p className="text-lg text-gray-600 max-w-2xl">
              {product.description}
            </p>
          )}

          {/* Price */}
          <p className="text-5xl font-bold text-blue-600">
            ${product.price.toFixed(2)}
          </p>

          {/* Stock */}
          <p
            className={`text-lg font-medium ${
              product.stock < 10 ? 'text-red-500' : 'text-gray-600'
            }`}
          >
            {product.stock} in stock
          </p>

          {/* Quantity Selector */}
          <div className="w-full max-w-xs">
            <div className="flex items-center gap-3 mb-4">
              <button
                onClick={decrementQuantity}
                disabled={quantity <= 1}
                className="p-3 bg-gray-100 hover:bg-gray-200 disabled:bg-gray-50 disabled:text-gray-300 rounded-lg transition-colors cursor-pointer"
                aria-label="Decrease quantity"
              >
                <Minus className="w-6 h-6" />
              </button>

              <input
                type="number"
                value={quantity}
                onChange={(e) => handleQuantityInput(e.target.value)}
                min="1"
                max={product.stock}
                className="flex-1 text-center py-3 border-2 border-gray-200 rounded-lg font-semibold text-xl focus:outline-none focus:border-blue-500 transition-colors"
              />

              <button
                onClick={incrementQuantity}
                disabled={quantity >= product.stock}
                className="p-3 bg-gray-100 hover:bg-gray-200 disabled:bg-gray-50 disabled:text-gray-300 rounded-lg transition-colors cursor-pointer"
                aria-label="Increase quantity"
              >
                <Plus className="w-6 h-6" />
              </button>
            </div>

            {/* Add to Cart Button */}
            <button
              onClick={handleAddToCart}
              className="w-full py-4 bg-blue-600 text-white rounded-xl font-semibold text-lg hover:bg-blue-700 transition-colors flex items-center justify-center gap-2 cursor-pointer"
            >
              <ShoppingCart className="w-6 h-6" />
              Add to Cart
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
