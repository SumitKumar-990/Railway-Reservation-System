import React from 'react';
import { Train, Clock, MapPin } from 'lucide-react';
import { formatTime, formatDate, formatCurrency } from '../utils/formatters';

const TicketCard = ({ booking }) => {
  return (
    <div className="bg-[#fdfbf7] rounded-xl shadow-md overflow-hidden border border-gray-200 flex flex-col md:flex-row relative">
      {/* Decorative cutouts */}
      <div className="hidden md:block absolute -top-3 left-[70%] w-6 h-6 bg-surface-50 rounded-full border-b border-gray-200 z-10"></div>
      <div className="hidden md:block absolute -bottom-3 left-[70%] w-6 h-6 bg-surface-50 rounded-full border-t border-gray-200 z-10"></div>
      
      {/* Left panel - Info */}
      <div className="p-6 md:w-[70%] md:border-r-2 md:border-dashed border-gray-300">
        <div className="flex justify-between items-start mb-6 border-b border-gray-200 pb-4">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">{booking.trainName}</h2>
            <p className="text-gray-500 font-mono flex items-center gap-1 mt-1">
              <Train className="h-4 w-4" /> {booking.trainNumber}
            </p>
          </div>
          <div className="text-right">
            <span className="text-xs uppercase tracking-wider text-gray-500 font-medium block mb-1">PNR Number</span>
            <span className="text-xl font-bold text-accent-600 tracking-wider font-mono">{booking.pnr}</span>
          </div>
        </div>

        <div className="flex items-center justify-between mb-8">
          <div className="flex-1">
            <span className="text-xs text-gray-500 uppercase tracking-wide block mb-1">Departure</span>
            <div className="font-bold text-3xl text-gray-900 mb-1">{formatTime(booking.departureTime)}</div>
            <div className="font-medium text-gray-800">{booking.fromStation}</div>
            <div className="text-sm text-gray-500">{formatDate(booking.date)}</div>
          </div>
          
          <div className="px-4 flex flex-col items-center text-gray-300">
            <div className="w-16 border-t-2 border-dashed border-gray-300 mb-1"></div>
            <Train className="h-5 w-5" />
          </div>
          
          <div className="flex-1 text-right">
            <span className="text-xs text-gray-500 uppercase tracking-wide block mb-1">Arrival</span>
            <div className="font-bold text-3xl text-gray-900 mb-1">{formatTime(booking.arrivalTime)}</div>
            <div className="font-medium text-gray-800">{booking.toStation}</div>
            {/* Simple date assumption for demo, a real app would calculate arrival date */}
            <div className="text-sm text-gray-500">{formatDate(booking.date)}</div>
          </div>
        </div>

        <div className="bg-white/60 p-4 rounded-lg border border-gray-100">
          <h4 className="text-sm font-semibold text-gray-800 mb-3 border-b border-gray-200 pb-2">Passenger Details</h4>
          <div className="space-y-2">
            {booking.passengers.map((p, idx) => (
              <div key={idx} className="flex justify-between items-center text-sm">
                <div className="font-medium text-gray-900">{p.name} <span className="text-gray-500 font-normal">({p.age}, {p.gender[0]})</span></div>
                <div className="font-mono bg-surface-100 px-2 py-1 rounded text-gray-800">
                  {p.seatNumber || (booking.waitlistPosition ? `WL-${booking.waitlistPosition}` : 'Pending')}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Right panel - QR & Meta */}
      <div className="p-6 md:w-[30%] bg-[#fdfbf7] flex flex-col justify-between items-center text-center border-t-2 border-dashed border-gray-300 md:border-none">
        <div className="w-full">
          <div className="grid grid-cols-2 gap-4 mb-6">
            <div>
              <span className="text-[10px] uppercase tracking-wider text-gray-500 block mb-1">Class</span>
              <span className="font-bold text-gray-900 text-lg">{booking.seatClassCode}</span>
            </div>
            <div>
              <span className="text-[10px] uppercase tracking-wider text-gray-500 block mb-1">Status</span>
              <span className="font-bold text-emerald-600 text-lg">{booking.status}</span>
            </div>
          </div>
          
          {booking.qrCodeDataUri ? (
            <div className="bg-white p-2 rounded-lg border border-gray-200 inline-block mb-4 shadow-sm">
              <img src={booking.qrCodeDataUri} alt="Ticket QR Code" className="w-32 h-32 object-contain" />
            </div>
          ) : (
            <div className="w-32 h-32 bg-gray-100 border border-gray-200 rounded-lg flex items-center justify-center mb-4 mx-auto">
              <span className="text-xs text-gray-400">QR Unavailable</span>
            </div>
          )}
          
          <div className="text-xs text-gray-500 mt-2">
            Scan to verify ticket
          </div>
        </div>
        
        <div className="w-full mt-6 pt-4 border-t border-gray-200">
          <span className="text-xs uppercase tracking-wider text-gray-500 block mb-1">Total Fare</span>
          <span className="text-xl font-bold text-gray-900">{formatCurrency(booking.totalFare)}</span>
        </div>
      </div>
    </div>
  );
};

export default TicketCard;
