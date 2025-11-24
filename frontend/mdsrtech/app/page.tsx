import Navbar from './components/Navbar';
import HeroSection from './components/HeroSection';
import FeaturedItems from './components/FeaturedItems';

export default function Home() {
  return (
    <div className="min-h-screen">
      <Navbar />
      <HeroSection />
      <FeaturedItems />
    </div>
  );
}
