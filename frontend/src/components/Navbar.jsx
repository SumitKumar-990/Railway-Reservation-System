import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Train, Menu, X, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
    setIsMobileMenuOpen(false);
  };

  const isAdmin = user?.role === 'ADMIN';

  return (
    <nav className="bg-white border-b border-gray-200 sticky top-0 z-50 shadow-sm">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex">
            <Link to="/" className="flex-shrink-0 flex items-center gap-2">
              <Train className="h-8 w-8 text-accent-500" />
              <span className="font-bold text-xl tracking-tight text-gray-900">RailYatra</span>
            </Link>
          </div>
          
          {/* Desktop Menu */}
          <div className="hidden sm:ml-6 sm:flex sm:items-center sm:space-x-6">
            <Link to="/" className="text-gray-700 hover:text-accent-600 font-medium transition text-sm">
              Search Trains
            </Link>

            {isAuthenticated ? (
              <>
                <Link to="/bookings" className="text-gray-700 hover:text-accent-600 font-medium transition text-sm">
                  My Bookings
                </Link>

                {isAdmin && (
                  <Link
                    to="/admin"
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-amber-50 text-amber-800 hover:bg-amber-100 border border-amber-300/80 rounded-lg text-xs font-bold transition"
                  >
                    <ShieldCheck className="h-4 w-4 text-amber-600" /> Admin Console
                  </Link>
                )}

                <div className="flex items-center space-x-3 ml-2 border-l border-gray-200 pl-4">
                  <div className="text-right">
                    <span className="text-xs font-bold text-gray-800 block leading-tight">{user?.firstName}</span>
                    <span className="text-[10px] uppercase font-mono text-gray-400 font-semibold">{user?.role}</span>
                  </div>
                  <button 
                    onClick={handleLogout}
                    className="text-xs font-semibold px-3 py-1.5 bg-gray-100 hover:bg-red-50 hover:text-red-600 text-gray-700 rounded-lg transition"
                  >
                    Logout
                  </button>
                </div>
              </>
            ) : (
              <Link 
                to="/login" 
                className="bg-accent-500 hover:bg-accent-600 text-white px-4 py-2 rounded-lg font-bold text-sm transition shadow-sm"
              >
                Login / Register
              </Link>
            )}
          </div>

          {/* Mobile menu button */}
          <div className="flex items-center sm:hidden">
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100"
            >
              {isMobileMenuOpen ? (
                <X className="block h-6 w-6" />
              ) : (
                <Menu className="block h-6 w-6" />
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu */}
      {isMobileMenuOpen && (
        <div className="sm:hidden border-t border-gray-200 bg-white">
          <div className="pt-2 pb-3 space-y-1">
            <Link
              to="/"
              onClick={() => setIsMobileMenuOpen(false)}
              className="block px-4 py-2 text-base font-medium text-gray-700 hover:text-accent-600 hover:bg-gray-50"
            >
              Search Trains
            </Link>

            {isAuthenticated ? (
              <>
                <div className="px-4 py-2 border-b border-gray-100">
                  <p className="text-sm font-bold text-gray-900">{user?.firstName} {user?.lastName}</p>
                  <p className="text-xs text-gray-500 font-mono">{user?.email} ({user?.role})</p>
                </div>
                <Link 
                  to="/bookings" 
                  onClick={() => setIsMobileMenuOpen(false)}
                  className="block px-4 py-2 text-base font-medium text-gray-700 hover:text-accent-600 hover:bg-gray-50"
                >
                  My Bookings
                </Link>
                {isAdmin && (
                  <Link 
                    to="/admin" 
                    onClick={() => setIsMobileMenuOpen(false)}
                    className="block px-4 py-2 text-base font-bold text-amber-700 hover:bg-amber-50"
                  >
                    Admin Console
                  </Link>
                )}
                <button
                  onClick={handleLogout}
                  className="block w-full text-left px-4 py-2 text-base font-semibold text-red-600 hover:bg-red-50"
                >
                  Logout
                </button>
              </>
            ) : (
              <Link
                to="/login"
                onClick={() => setIsMobileMenuOpen(false)}
                className="block px-4 py-2 text-base font-bold text-accent-600 hover:bg-accent-50"
              >
                Login / Register
              </Link>
            )}
          </div>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
