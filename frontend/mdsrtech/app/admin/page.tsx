'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/contexts/AuthContext';
import { Shield, CheckCircle, XCircle } from 'lucide-react';
import Link from 'next/link';
import Navbar from '../components/Navbar';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:5000/api';

export default function AdminPage() {
  const { user, isAuthenticated, isLoading } = useAuth();
  const router = useRouter();
  const [backendVerified, setBackendVerified] = useState<boolean | null>(null);
  const [verifying, setVerifying] = useState(true);

  // Verify admin status with backend
  useEffect(() => {
    const verifyAdminWithBackend = async () => {
      if (!isAuthenticated || user?.role !== 'admin') {
        setVerifying(false);
        return;
      }

      try {
        const token = localStorage.getItem('access_token');
        const response = await fetch(`${API_URL}/auth/admin/verify`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        if (response.ok) {
          setBackendVerified(true);
        } else {
          setBackendVerified(false);
          // If backend says not admin, redirect
          router.push('/');
        }
      } catch (error) {
        console.error('Admin verification failed:', error);
        setBackendVerified(false);
      } finally {
        setVerifying(false);
      }
    };

    if (!isLoading) {
      verifyAdminWithBackend();
    }
  }, [isLoading, isAuthenticated, user, router]);

  // Redirect non-admins
  useEffect(() => {
    if (!isLoading && (!isAuthenticated || user?.role !== 'admin')) {
      router.push('/');
    }
  }, [isLoading, isAuthenticated, user, router]);

  // Show loading while checking auth
  if (isLoading || verifying) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50 gap-4">
        <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-blue-600 border-r-transparent"></div>
        <p className="text-gray-500">Verifying admin access...</p>
      </div>
    );
  }

  // Don't render if not admin
  if (!isAuthenticated || user?.role !== 'admin') {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      
      <main className="pt-24 pb-16 px-4 sm:px-6 lg:px-8 max-w-4xl mx-auto">
        <div className="bg-white rounded-2xl shadow-lg p-8 text-center">
          {/* Admin Badge */}
          <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full mb-6">
            <Shield className="w-10 h-10 text-white" />
          </div>

          {/* Title */}
          <h1 className="text-3xl font-bold text-gray-900 mb-4">
            Admin Dashboard
          </h1>

          {/* Backend Verification Status */}
          {backendVerified === true ? (
            <div className="inline-flex items-center gap-2 px-4 py-2 bg-green-100 text-green-700 rounded-full mb-6">
              <CheckCircle className="w-5 h-5" />
              <span className="font-medium">Backend verified: You are an authorized admin</span>
            </div>
          ) : backendVerified === false ? (
            <div className="inline-flex items-center gap-2 px-4 py-2 bg-red-100 text-red-700 rounded-full mb-6">
              <XCircle className="w-5 h-5" />
              <span className="font-medium">Backend verification failed</span>
            </div>
          ) : null}

          {/* User Info */}
          <div className="bg-gray-50 rounded-xl p-6 mb-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Your Admin Profile</h2>
            <div className="space-y-2 text-left max-w-sm mx-auto">
              <div className="flex justify-between">
                <span className="text-gray-500">Name:</span>
                <span className="font-medium text-gray-900">{user?.full_name}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Email:</span>
                <span className="font-medium text-gray-900">{user?.email}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Role:</span>
                <span className="font-medium text-purple-600 uppercase">{user?.role}</span>
              </div>
            </div>
          </div>

          {/* Placeholder for future admin features */}
          <p className="text-gray-500 mb-6">
            Admin features coming soon: Manage products, view orders, manage users, and more.
          </p>

          <Link 
            href="/"
            className="inline-block px-6 py-3 bg-blue-600 text-white font-medium rounded-xl hover:bg-blue-700 transition-colors"
          >
            Back to Store
          </Link>
        </div>
      </main>
    </div>
  );
}
