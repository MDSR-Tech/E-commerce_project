import { getAllProducts } from '@/lib/api/products';
import ProductCard from './ProductCard';

interface Product {
  id: number;
  name: string;
  price: number;
  originalPrice?: number;
  isOnSale?: boolean;
  salePercent?: number;
  stock: number;
  image: string;
}

export default async function FeaturedItems() {
  // Server-side data fetch with automatic caching
  const dbProducts = await getAllProducts();
  
  // Transform database products to component format
  const products: Product[] = dbProducts.map((dbProduct) => ({
    id: dbProduct.id,
    name: dbProduct.title,
    // Use sale price if on sale, otherwise regular price
    price: dbProduct.is_on_sale && dbProduct.sale_price_cents 
      ? dbProduct.sale_price_cents / 100 
      : dbProduct.price_cents / 100,
    originalPrice: dbProduct.is_on_sale ? dbProduct.price_cents / 100 : undefined,
    isOnSale: dbProduct.is_on_sale,
    salePercent: dbProduct.sale_percent || undefined,
    stock: dbProduct.stock,
    image: dbProduct.image?.url || dbProduct.images?.[0]?.url || '/placeholder-product.jpg',
  }));

  return (
    <section className="py-16 px-4 sm:px-6 lg:px-8 bg-gray-50">
      <div className="max-w-7xl mx-auto">
        {/* Section Header */}
        <div className="mb-12">
          <h2 className="text-4xl md:text-5xl font-bold text-gray-900 mb-2">
            Featured Items
          </h2>
          <div className="w-24 h-1 bg-linear-to-r from-blue-600 to-purple-600 rounded-full"></div>
        </div>

        {/* Product Grid */}
        {products.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-600 text-lg">No products available at the moment.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {products.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        )}
      </div>
    </section>
  );
}
