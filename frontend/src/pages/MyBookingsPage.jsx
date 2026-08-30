import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Train, ChevronRight, Calendar } from 'lucide-react';
import { getBookings } from '../api/client';
import LoadingSpinner, { Skeleton } from '../components/LoadingSpinner';
import EmptyState from '../components/EmptyState';
import StatusBadge from '../components/StatusBadge';
import { formatDate, formatTime, formatCurrency } from '../utils/formatters';

const MyBookingsPage = () => {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchBookings = async () => {
      try {
        const data = await getBookings();
        // Sort by created date descending
        data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        setBookings(data);
      } catch (err) {
        setError('Failed to fetch your bookings. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchBookings();
  }, []);

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">My Journeys</h1>
        <div className="space-y-4">
          {[1, 2, 3].map(i => (
            <div key={i} className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <div className="flex justify-between mb-4">
                <Skeleton className="h-6 w-1/4" />
                <Skeleton className="h-6 w-20 rounded-full" />
              </div>
              <div className="flex gap-4">
                <Skeleton className="h-16 w-1/3" />
                <Skeleton className="h-16 w-1/3" />
                <Skeleton className="h-16 w-1/4" />
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-3xl font-bold text-gray-900">My Journeys</h1>
        {!loading && bookings.length > 0 && (
          <span className="bg-surface-200 text-gray-700 py-1 px-3 rounded-full text-sm font-medium">
            {bookings.length} Bookings
          </span>
        )}
      </div>

      {error && (
        <div className="bg-red-50 border-l-4 border-red-500 p-4 mb-6 rounded-r-lg">
          <p className="text-sm text-red-700">{error}</p>
        </div>
      )}

      {!loading && !error && bookings.length === 0 && (
        <EmptyState 
          icon={Train}
          message="You haven't booked any journeys yet."
          actionLabel="Search Trains"
          actionHref="/"
        />
      )}

      {!loading && !error && bookings.length > 0 && (
        <div className="space-y-4">
          {bookings.map((booking) => (
            <Link 
              key={booking.pnr} 
              to={`/bookings/${booking.pnr}`}
              className="block bg-white rounded-xl shadow-sm border border-gray-200 hover:shadow-md transition-shadow overflow-hidden group"
            >
              <div className="p-6">
                <div className="flex justify-between items-start mb-4 border-b border-gray-100 pb-4">
                  <div>
                    <h3 className="text-lg font-bold text-gray-900 group-hover:text-accent-600 transition-colors">
                      {booking.trainName} ({booking.trainNumber})
                    </h3>
                    <p className="text-sm text-gray-500 font-mono mt-1">PNR: {booking.pnr}</p>
                  </div>
                  <div className="text-right flex flex-col items-end gap-2">
                    <StatusBadge status={booking.status} />
                    <span className="font-bold text-gray-900">{formatCurrency(booking.totalFare)}</span>
                  </div>
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <p className="text-xl font-bold text-gray-900">{formatTime(booking.departureTime)}</p>
                    <p className="font-medium text-gray-700">{booking.fromStation}</p>
                    <p className="text-sm text-gray-500 flex items-center gap-1 mt-1">
                      <Calendar className="h-3 w-3" /> {formatDate(booking.date)}
                    </p>
                  </div>
                  
                  <div className="px-6 flex flex-col items-center">
                    <div className="w-16 border-t-2 border-dashed border-gray-300 relative">
                      <ChevronRight className="absolute -top-2.5 -right-2 h-5 w-5 text-gray-300" />
                    </div>
                    <span className="text-xs font-semibold mt-2 px-2 py-0.5 bg-surface-100 rounded text-gray-600">
                      {booking.seatClassCode}
                    </span>
                  </div>

                  <div className="flex-1 text-right">
                    <p className="text-xl font-bold text-gray-900">{formatTime(booking.arrivalTime)}</p>
                    <p className="font-medium text-gray-700">{booking.toStation}</p>
                    <p className="text-sm text-gray-500 flex items-center justify-end gap-1 mt-1">
                      <Calendar className="h-3 w-3" /> {formatDate(booking.date)}
                    </p>
                  </div>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyBookingsPage;
