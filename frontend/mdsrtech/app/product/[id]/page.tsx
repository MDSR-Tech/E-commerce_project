import Navbar from '../../components/Navbar';
import ProductPageClient from './ProductPageClient';
import { getProductById } from '@/lib/api/products';

export default async function ProductPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const product = await getProductById(parseInt(id));
  
  if (!product) {
    return (
      <div className="min-h-screen">
        <Navbar />
        <div className="flex items-center justify-center h-[calc(100vh-4rem)]">
          <p className="text-xl text-gray-600">Product not found</p>
        </div>
      </div>
    );
  }
  
  // Transform API product to component format with sale info
  const productForClient = {
    id: product.id,
    name: product.title,
    price: product.is_on_sale && product.sale_price_cents 
      ? product.sale_price_cents / 100 
      : product.price_cents / 100,
    originalPrice: product.is_on_sale ? product.price_cents / 100 : undefined,
    isOnSale: product.is_on_sale,
    salePercent: product.sale_percent || undefined,
    stock: product.stock,
    image: product.image?.url || product.images?.[0]?.url || '/placeholder-product.jpg',
    description: product.description || 'No description available',
    brand: product.brand?.name,
    category: product.category?.name,
  };
  
  return <ProductPageClient product={productForClient} />;
}

