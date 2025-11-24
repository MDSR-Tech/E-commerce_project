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
  
  // Transform API product to component format
  const productForClient = {
    id: product.id,
    name: product.title,
    price: product.price_cents / 100,
    stock: product.stock,
    image: product.images?.[0]?.url || '/placeholder-product.jpg',
    description: product.description || 'No description available',
    brand: product.brand?.name,
    category: product.category?.name,
  };
  
  return <ProductPageClient product={productForClient} />;
}

