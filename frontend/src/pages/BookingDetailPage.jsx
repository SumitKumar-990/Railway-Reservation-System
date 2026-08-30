import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Ban, Activity } from 'lucide-react';
import { getBooking, cancelBooking } from '../api/client';
import TicketCard from '../components/TicketCard';
import StatusBadge from '../components/StatusBadge';
import LoadingSpinner from '../components/LoadingSpinner';
import ConfirmDialog from '../components/ConfirmDialog';
import LiveTrackingModal from '../components/LiveTrackingModal';
import { formatCurrency, formatDate, formatTime } from '../utils/formatters';

const BookingDetailPage = () => {
  const { pnr } = useParams();
  const navigate = useNavigate();
  
  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [isCancelDialogOpen, setIsCancelDialogOpen] = useState(false);
  const [isCancelling, setIsCancelling] = useState(false);
  const [isLiveModalOpen, setIsLiveModalOpen] = useState(false);

  useEffect(() => {
    fetchBooking();
  }, [pnr]);

  const fetchBooking = async () => {
    setLoading(true);
    try {
      const data = await getBooking(pnr);
      setBooking(data);
    } catch (err) {
      setError('Failed to fetch booking details.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async () => {
    setIsCancelling(true);
    try {
      const data = await cancelBooking(pnr);
      setBooking(data);
      setIsCancelDialogOpen(false);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to cancel booking.');
    } finally {
      setIsCancelling(false);
    }
  };

  if (loading && !booking) {
    return (
      <div className="flex justify-center items-center h-[60vh]">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (error || !booking) {
    return (
      <div className="max-w-3xl mx-auto mt-12 px-4">
        <div className="p-6 bg-red-50 text-red-700 rounded-xl border border-red-200">
          <h3 className="text-lg font-semibold mb-2">Error</h3>
          <p>{error}</p>
          <button 
            onClick={() => navigate('/bookings')}
            className="mt-4 px-4 py-2 bg-red-100 hover:bg-red-200 rounded-lg text-sm font-medium transition"
          >
            Back to My Bookings
          </button>
        </div>
      </div>
    );
  }

  const canCancel = ['CONFIRMED', 'RAC', 'WAITLISTED'].includes(booking.status);
  const isBoardable = booking.boardable;

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex justify-between items-center mb-6">
        <Link 
          to="/bookings" 
          className="inline-flex items-center gap-2 text-sm font-medium text-gray-500 hover:text-accent-600 transition"
        >
          <ArrowLeft className="h-4 w-4" /> Back to My Bookings
        </Link>

        <button
          onClick={() => setIsLiveModalOpen(true)}
          className="inline-flex items-center gap-2 px-4 py-2 bg-accent-50 text-accent-700 hover:bg-accent-100 border border-accent-200 rounded-lg text-sm font-bold shadow-sm transition"
        >
          <Activity className="h-4 w-4 text-accent-600 animate-pulse" />
          Track Live Train
        </button>
      </div>

      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Journey Details</h1>
        {canCancel && (
          <button
            onClick={() => setIsCancelDialogOpen(true)}
            className="inline-flex items-center gap-2 px-4 py-2 bg-white border border-red-200 text-red-600 rounded-lg text-sm font-medium hover:bg-red-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition"
          >
            <Ban className="h-4 w-4" /> Cancel Ticket
          </button>
        )}
      </div>

      {booking.status === 'WAITLISTED' && (
        <div className="bg-amber-50 border border-amber-200 rounded-xl p-6 mb-8 flex items-center justify-between">
          <div>
            <h3 className="text-lg font-bold text-amber-900">Waitlist Status</h3>
            <p className="text-amber-700">Your ticket is currently waitlisted. It will be confirmed if there are cancellations.</p>
          </div>
          <div className="text-center bg-white px-6 py-3 rounded-lg border border-amber-200 shadow-sm">
            <span className="block text-xs uppercase tracking-wide text-amber-600 font-bold mb-1">Current Position</span>
            <span className="text-2xl font-bold text-amber-900">WL-{booking.waitlistPosition}</span>
          </div>
        </div>
      )}

      {isBoardable ? (
        <div className="mb-12">
          <TicketCard booking={booking} />
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-8">
          <div className="flex justify-between items-start mb-6 border-b border-gray-100 pb-4">
            <div>
              <h2 className="text-xl font-bold text-gray-900">{booking.trainName}</h2>
              <p className="text-gray-500 font-mono text-sm mt-1">#{booking.trainNumber}</p>
            </div>
            <div className="text-right flex flex-col items-end gap-2">
              <span className="text-sm text-gray-500">PNR: <span className="font-bold text-gray-900">{booking.pnr}</span></span>
              <StatusBadge status={booking.status} />
            </div>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
            <div>
              <p className="text-xs text-gray-500 uppercase">Departure</p>
              <p className="font-bold text-gray-900">{formatTime(booking.departureTime)}</p>
              <p className="text-sm">{booking.fromStation}</p>
              <p className="text-sm text-gray-500">{formatDate(booking.date)}</p>
            </div>
            <div>
              <p className="text-xs text-gray-500 uppercase">Arrival</p>
              <p className="font-bold text-gray-900">{formatTime(booking.arrivalTime)}</p>
              <p className="text-sm">{booking.toStation}</p>
              <p className="text-sm text-gray-500">{formatDate(booking.date)}</p>
            </div>
            <div>
              <p className="text-xs text-gray-500 uppercase">Class & Fare</p>
              <p className="font-bold text-gray-900">{booking.seatClassLabel}</p>
              <p className="text-sm">{formatCurrency(booking.totalFare)}</p>
              <p className="text-sm font-medium text-gray-600">{booking.paid ? 'Paid' : 'Unpaid'}</p>
            </div>
          </div>

          <div className="mt-6 pt-6 border-t border-gray-100">
            <h3 className="text-sm font-semibold text-gray-900 mb-3">Passenger Details</h3>
            <div className="bg-surface-50 rounded-lg p-4">
              {booking.passengers.map((p, idx) => (
                <div key={idx} className="flex justify-between items-center py-2 border-b border-gray-200 last:border-0 last:pb-0">
                  <div className="font-medium text-gray-900">{p.name} <span className="text-gray-500 font-normal">({p.age}, {p.gender})</span></div>
                  <div className="text-sm font-medium bg-white px-2 py-1 rounded border border-gray-200">
                    {p.seatNumber || (booking.waitlistPosition ? `WL-${booking.waitlistPosition}` : 'N/A')}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* If unpaid and not cancelled/expired, show pay button */}
      {!booking.paid && booking.status !== 'CANCELLED' && booking.status !== 'EXPIRED' && (
        <div className="bg-accent-50 rounded-xl border border-accent-200 p-6 flex items-center justify-between">
          <div>
            <h3 className="text-lg font-bold text-accent-900">Payment Pending</h3>
            <p className="text-accent-700">Please complete your payment before the hold expires to secure this ticket.</p>
          </div>
          <Link 
            to={`/payment/${booking.pnr}`}
            className="bg-accent-600 hover:bg-accent-700 text-white px-6 py-3 rounded-lg font-bold transition shadow-sm"
          >
            Pay Now
          </Link>
        </div>
      )}

      <ConfirmDialog 
        isOpen={isCancelDialogOpen}
        title="Cancel Ticket"
        message={`Are you sure you want to cancel the ticket for PNR ${booking.pnr}? This action cannot be undone.`}
        confirmLabel="Cancel Ticket"
        onConfirm={handleCancel}
        onCancel={() => setIsCancelDialogOpen(false)}
        isLoading={isCancelling}
      />

      <LiveTrackingModal
        isOpen={isLiveModalOpen}
        onClose={() => setIsLiveModalOpen(false)}
        trainNumber={booking.trainNumber}
        trainName={booking.trainName}
      />
    </div>
  );
};

export default BookingDetailPage;
