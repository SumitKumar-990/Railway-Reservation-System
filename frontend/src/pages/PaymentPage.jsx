import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { CreditCard, CheckCircle2, Clock } from 'lucide-react';
import { getBooking, initiatePayment, confirmPayment } from '../api/client';
import LoadingSpinner from '../components/LoadingSpinner';
import { formatCurrency } from '../utils/formatters';

const PaymentPage = () => {
  const { pnr } = useParams();
  const navigate = useNavigate();
  
  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [paymentMethod, setPaymentMethod] = useState('UPI');
  const [isProcessing, setIsProcessing] = useState(false);
  const [paymentSuccess, setPaymentSuccess] = useState(false);
  const [timeLeft, setTimeLeft] = useState(600); // 10 minutes

  useEffect(() => {
    const fetchBooking = async () => {
      try {
        const data = await getBooking(pnr);
        setBooking(data);
        
        if (data.paid) {
          setPaymentSuccess(true);
        } else if (data.status === 'EXPIRED') {
          setError('This booking hold has expired. Please book again.');
        } else if (data.holdExpiresAt) {
          // Calculate time left
          const expiresAt = new Date(data.holdExpiresAt).getTime();
          const now = new Date().getTime();
          const diff = Math.max(0, Math.floor((expiresAt - now) / 1000));
          setTimeLeft(diff);
        }
      } catch (err) {
        setError('Failed to fetch booking details.');
      } finally {
        setLoading(false);
      }
    };

    fetchBooking();
  }, [pnr]);

  useEffect(() => {
    if (timeLeft > 0 && !paymentSuccess && !error) {
      const timer = setInterval(() => {
        setTimeLeft(prev => {
          if (prev <= 1) {
            clearInterval(timer);
            setError('Payment time expired. Please try booking again.');
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
      return () => clearInterval(timer);
    }
  }, [timeLeft, paymentSuccess, error]);

  const handlePayment = async () => {
    setIsProcessing(true);
    setError('');
    
    try {
      // 1. Initiate payment
      const initRes = await initiatePayment({ pnr, method: paymentMethod });
      
      // Simulating a delay for the payment gateway
      await new Promise(resolve => setTimeout(resolve, 2000));
      
      // 2. Confirm payment via webhook (simulating gateway callback)
      await confirmPayment({
        transactionRef: initRes.transactionRef,
        status: 'SUCCESS',
        pnr
      });
      
      setPaymentSuccess(true);
    } catch (err) {
      setError(err.response?.data?.message || 'Payment failed. Please try again.');
    } finally {
      setIsProcessing(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-[60vh]">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (error && !booking) {
    return (
      <div className="max-w-md mx-auto mt-12 p-6 bg-red-50 text-red-700 rounded-xl border border-red-200 text-center">
        <h3 className="text-lg font-semibold mb-2">Error</h3>
        <p>{error}</p>
        <button 
          onClick={() => navigate('/')}
          className="mt-4 px-4 py-2 bg-red-100 hover:bg-red-200 rounded-lg text-sm font-medium transition"
        >
          Go to Home
        </button>
      </div>
    );
  }

  if (paymentSuccess) {
    return (
      <div className="max-w-md mx-auto mt-12 p-8 bg-white rounded-xl border border-gray-200 text-center shadow-sm">
        <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-emerald-100 mb-6">
          <CheckCircle2 className="h-10 w-10 text-emerald-600" />
        </div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Payment Successful!</h2>
        <p className="text-gray-500 mb-8">
          Your booking is confirmed. Your PNR is <span className="font-bold text-gray-900">{pnr}</span>
        </p>
        <button 
          onClick={() => navigate(`/bookings/${pnr}`)}
          className="w-full bg-accent-500 hover:bg-accent-600 text-white py-3 px-4 rounded-lg font-semibold transition"
        >
          View Ticket
        </button>
      </div>
    );
  }

  const formatTimeLeft = () => {
    const m = Math.floor(timeLeft / 60);
    const s = timeLeft % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  };

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="text-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Complete Payment</h1>
        
        {error ? (
          <div className="mt-4 inline-flex items-center gap-2 bg-red-50 text-red-700 px-4 py-2 rounded-lg border border-red-200">
            {error}
          </div>
        ) : (
          <div className="mt-4 inline-flex items-center gap-2 bg-amber-50 text-amber-800 px-4 py-2 rounded-lg border border-amber-200 font-medium">
            <Clock className="h-5 w-5" />
            Payment required within {formatTimeLeft()}
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* Booking Summary */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 self-start">
          <h3 className="text-lg font-semibold text-gray-900 mb-4 pb-4 border-b border-gray-100">Booking Summary</h3>
          
          <div className="space-y-4 mb-6">
            <div className="flex justify-between">
              <span className="text-gray-500">PNR</span>
              <span className="font-bold text-gray-900">{booking.pnr}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Train</span>
              <span className="font-medium text-gray-900">{booking.trainNumber} - {booking.trainName}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Route</span>
              <span className="font-medium text-gray-900">{booking.fromStation} to {booking.toStation}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Class</span>
              <span className="font-medium text-gray-900">{booking.seatClassLabel}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Passengers</span>
              <span className="font-medium text-gray-900">{booking.passengers.length}</span>
            </div>
          </div>
          
          <div className="pt-4 border-t border-gray-100 flex justify-between items-center">
            <span className="text-lg font-bold text-gray-900">Amount to Pay</span>
            <span className="text-2xl font-bold text-accent-600">{formatCurrency(booking.totalFare)}</span>
          </div>
        </div>

        {/* Payment Methods */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Select Payment Method</h3>
          
          <div className="space-y-3 mb-8">
            {['UPI', 'Card', 'NetBanking'].map((method) => (
              <label 
                key={method}
                className={`flex items-center p-4 border rounded-lg cursor-pointer transition-colors ${
                  paymentMethod === method ? 'border-accent-500 bg-accent-50' : 'border-gray-200 hover:bg-gray-50'
                }`}
              >
                <input 
                  type="radio" 
                  name="paymentMethod" 
                  value={method}
                  checked={paymentMethod === method}
                  onChange={() => setPaymentMethod(method)}
                  className="h-4 w-4 text-accent-600 focus:ring-accent-500 border-gray-300"
                />
                <div className="ml-4 flex items-center gap-2">
                  <CreditCard className={`h-5 w-5 ${paymentMethod === method ? 'text-accent-600' : 'text-gray-400'}`} />
                  <span className={`font-medium ${paymentMethod === method ? 'text-accent-900' : 'text-gray-700'}`}>
                    {method === 'UPI' ? 'UPI / QR' : method === 'Card' ? 'Credit / Debit Card' : 'Net Banking'}
                  </span>
                </div>
              </label>
            ))}
          </div>
          
          <button
            onClick={handlePayment}
            disabled={isProcessing || !!error || timeLeft === 0}
            className="w-full bg-accent-500 hover:bg-accent-600 text-white py-4 px-4 rounded-lg font-bold shadow-sm transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-accent-500 disabled:opacity-70 disabled:cursor-not-allowed flex justify-center items-center gap-2 text-lg"
          >
            {isProcessing ? (
              <>
                <LoadingSpinner size="sm" className="text-white" />
                Processing Payment...
              </>
            ) : (
              `Pay ${formatCurrency(booking.totalFare)}`
            )}
          </button>
          
          <p className="text-center text-xs text-gray-500 mt-4">
            Secured by RailYatra Payments. All transactions are encrypted.
          </p>
        </div>
      </div>
    </div>
  );
};

export default PaymentPage;
