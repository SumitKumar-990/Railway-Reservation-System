import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Train, ShieldCheck, UserCheck, KeyRound } from 'lucide-react';

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleAuth = async (loginEmail, loginPassword) => {
    setError('');
    setIsSubmitting(true);
    
    try {
      await login({ email: loginEmail, password: loginPassword });
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please check your credentials.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    handleAuth(email, password);
  };

  const handleQuickLogin = (demoEmail, demoPassword) => {
    setEmail(demoEmail);
    setPassword(demoPassword);
    handleAuth(demoEmail, demoPassword);
  };

  return (
    <div className="min-h-[80vh] flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="flex justify-center">
          <div className="bg-accent-100 p-3 rounded-full">
            <Train className="h-10 w-10 text-accent-600" />
          </div>
        </div>
        <h2 className="mt-6 text-center text-3xl font-bold tracking-tight text-gray-900">
          Sign in to your account
        </h2>
        <p className="mt-2 text-center text-sm text-gray-600">
          Or{' '}
          <Link to="/register" className="font-medium text-accent-600 hover:text-accent-500 transition-colors">
            create a new account
          </Link>
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md space-y-6">
        {/* Quick Demo Credentials Panel */}
        <div className="bg-gradient-to-br from-gray-900 to-[#1a1a2e] text-white p-5 rounded-2xl shadow-md border border-gray-800">
          <div className="flex items-center gap-2 mb-3 text-xs font-bold uppercase tracking-wider text-accent-400">
            <KeyRound className="h-4 w-4" /> Ready-to-use Demo Account
          </div>
          <button
            type="button"
            onClick={() => handleQuickLogin('user@railway.com', 'user1234')}
            className="w-full p-3.5 bg-white/10 hover:bg-white/15 rounded-xl border border-white/10 text-left transition flex items-center justify-between"
          >
            <div>
              <div className="flex items-center gap-1.5 font-semibold text-xs text-emerald-400 mb-1">
                <UserCheck className="h-3.5 w-3.5" /> Demo Customer Account
              </div>
              <p className="text-xs font-mono text-gray-300">user@railway.com</p>
              <p className="text-[11px] text-gray-400">Password: user1234</p>
            </div>
            <span className="text-xs font-bold text-accent-400 px-3 py-1.5 bg-accent-500/20 rounded-lg border border-accent-500/30">1-Click Sign In →</span>
          </button>
        </div>

        <div className="bg-white py-8 px-4 shadow-sm sm:rounded-2xl border border-gray-200 sm:px-10">
          <form className="space-y-6" onSubmit={handleSubmit}>
            {error && (
              <div className="bg-red-50 border-l-4 border-red-500 p-4 mb-4 rounded-r-lg">
                <p className="text-sm text-red-700">{error}</p>
              </div>
            )}
            
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                Email address
              </label>
              <div className="mt-1">
                <input
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="block w-full appearance-none rounded-lg border border-gray-300 px-3 py-2.5 shadow-sm focus:border-accent-500 focus:outline-none focus:ring-accent-500 sm:text-sm font-medium text-gray-900"
                />
              </div>
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                Password
              </label>
              <div className="mt-1">
                <input
                  id="password"
                  name="password"
                  type="password"
                  autoComplete="current-password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="block w-full appearance-none rounded-lg border border-gray-300 px-3 py-2.5 shadow-sm focus:border-accent-500 focus:outline-none focus:ring-accent-500 sm:text-sm font-medium text-gray-900"
                />
              </div>
            </div>

            <div>
              <button
                type="submit"
                disabled={isSubmitting}
                className="flex w-full justify-center rounded-lg border border-transparent bg-accent-500 py-3 px-4 text-sm font-bold text-white shadow-sm hover:bg-accent-600 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:ring-offset-2 disabled:opacity-70 disabled:cursor-not-allowed transition-colors"
              >
                {isSubmitting ? (
                  <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                ) : (
                  'Sign in'
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
