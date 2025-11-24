'use client';

import { useState } from 'react';
import { Heart, Plus, Minus, ShoppingCart } from 'lucide-react';
import Link from 'next/link';

interface Product {
  id: number;
  name: string;
  price: number;
  stock: number;
  image: string;
}

interface ProductCardProps {
  product: Product;
}

export default function ProductCard({ product }: ProductCardProps) {
  const [isWishlisted, setIsWishlisted] = useState(false);
  const [isQuickAddOpen, setIsQuickAddOpen] = useState(false);
  const [quantity, setQuantity] = useState(1);

  const handleQuickAdd = () => {
    setIsQuickAddOpen(true);
  };

  const handleAddToCart = () => {
    // TODO: Add to cart functionality
    console.log(`Adding ${quantity} of ${product.name} to cart`);
    setIsQuickAddOpen(false);
    setQuantity(1);
  };

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

  return (
    <div className="group relative bg-white rounded-2xl shadow-md hover:shadow-xl transition-all duration-300 overflow-hidden border border-gray-100">
      {/* Wishlist Button */}
      <button
        onClick={() => setIsWishlisted(!isWishlisted)}
        className="absolute top-4 right-4 z-10 p-2 bg-white/90 backdrop-blur-sm rounded-full shadow-md hover:scale-110 transition-transform"
        aria-label="Add to wishlist"
      >
        <Heart
          className={`w-5 h-5 ${
            isWishlisted
              ? 'fill-red-500 text-red-500'
              : 'text-gray-600 hover:text-red-500'
          } transition-colors`}
        />
      </button>

      {/* Clickable Product Image and Name Area */}
      <Link href={`/product/${product.id}`} className="block">
        {/* Product Image */}
        <div className="relative h-64 bg-gray-100 overflow-hidden">
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="w-40 h-40 bg-gray-200 rounded-lg flex items-center justify-center">
              <span className="text-gray-400 text-sm">Product Image</span>
            </div>
          </div>
        </div>

        {/* Product Info */}
        <div className="p-6 pb-0">
          <h3 className="text-lg font-semibold text-gray-900 mb-2 line-clamp-2 hover:text-blue-600 transition-colors">
            {product.name}
          </h3>
        </div>
      </Link>

      {/* Price and Stock - Not Clickable */}
      <div className="px-6">
        <div className="flex items-center justify-between mb-4">
          <p className="text-2xl font-bold text-blue-600">
            ${product.price.toFixed(2)}
          </p>
          <p
            className={`text-sm font-medium ${
              product.stock < 10 ? 'text-red-500' : 'text-gray-500'
            }`}
          >
            {product.stock} in stock
          </p>
        </div>
      </div>

      {/* Quick Add Button / Counter */}
      <div className="px-6 pb-6">
        {!isQuickAddOpen ? (
          <button
            onClick={handleQuickAdd}
            className="w-full py-3 bg-blue-600 text-white rounded-xl font-medium hover:bg-blue-700 transition-colors flex items-center justify-center gap-2  cursor-pointer"
          >
            <Plus className="w-5 h-5" />
            Quick Add
          </button>
        ) : (
          <div className="space-y-3">
            <div className="flex items-center gap-2">
              <button
                onClick={decrementQuantity}
                disabled={quantity <= 1}
                className="p-2 bg-gray-100 hover:bg-gray-200 disabled:bg-gray-50 disabled:text-gray-300 rounded-lg transition-colors cursor-pointer"
                aria-label="Decrease quantity"
              >
                <Minus className="w-5 h-5" />
              </button>

              <input
                type="number"
                value={quantity}
                onChange={(e) => handleQuantityInput(e.target.value)}
                min="1"
                max={product.stock}
                className="flex-1 text-center py-2 border-2 border-gray-200 rounded-lg font-semibold text-lg focus:outline-none focus:border-blue-500 transition-colors"
              />

              <button
                onClick={incrementQuantity}
                disabled={quantity >= product.stock}
                className="p-2 bg-gray-100 hover:bg-gray-200 disabled:bg-gray-50 disabled:text-gray-300 rounded-lg transition-colors cursor-pointer"
                aria-label="Increase quantity"
              >
                <Plus className="w-5 h-5" />
              </button>
            </div>

            <button
              onClick={handleAddToCart}
              className="w-full py-3 bg-green-600 text-white rounded-xl font-medium hover:bg-green-700 transition-colors flex items-center justify-center gap-2 cursor-pointer"
            >
              <ShoppingCart className="w-5 h-5" />
              Add to Cart
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
