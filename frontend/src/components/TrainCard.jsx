import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Clock, MapPin, ChevronRight, Train as TrainIcon, Activity } from 'lucide-react';
import { formatTime, formatDuration, formatCurrency } from '../utils/formatters';
import LiveTrackingModal from './LiveTrackingModal';

const TrainCard = ({ train, searchDate }) => {
  const navigate = useNavigate();
  const [isLiveModalOpen, setIsLiveModalOpen] = useState(false);

  const handleBook = (classCode) => {
    navigate(`/book/${train.trainNumber}?from=${train.fromCode}&to=${train.toCode}&date=${searchDate}&class=${classCode}`);
  };

  return (
    <>
      <div className="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow border-l-4 border-l-accent-500 overflow-hidden mb-4">
        <div className="p-6">
          {/* Header */}
          <div className="flex justify-between items-start mb-6">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-surface-100 rounded-lg">
                <TrainIcon className="h-6 w-6 text-accent-600" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-gray-900">{train.trainName}</h3>
                <p className="text-sm text-gray-500 font-mono">#{train.trainNumber}</p>
              </div>
            </div>

            <button
              onClick={() => setIsLiveModalOpen(true)}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-accent-50 text-accent-700 hover:bg-accent-100 border border-accent-200/60 rounded-lg text-xs font-semibold transition"
            >
              <Activity className="h-3.5 w-3.5 text-accent-600 animate-pulse" />
              Live Status
            </button>
          </div>

          {/* Route Info */}
          <div className="flex items-center justify-between mb-6 relative">
            {/* From */}
            <div className="flex flex-col w-1/3">
              <span className="text-2xl font-bold text-gray-900">{formatTime(train.departureTime)}</span>
              <span className="text-sm font-semibold text-gray-700 mt-1">{train.fromCode}</span>
              <span className="text-xs text-gray-500 truncate">{train.fromStation}</span>
            </div>

            {/* Duration line */}
            <div className="flex flex-col items-center justify-center w-1/3 px-4">
              <span className="text-xs font-medium text-gray-500 mb-2">{formatDuration(train.departureTime, train.arrivalTime, train.dayOffset)}</span>
              <div className="w-full flex items-center relative">
                <div className="h-1.5 w-1.5 rounded-full bg-gray-300"></div>
                <div className="flex-grow border-t-2 border-dashed border-gray-300"></div>
                <div className="h-1.5 w-1.5 rounded-full bg-accent-500"></div>
              </div>
              <span className="text-xs text-gray-400 mt-2">{train.distanceKm} km</span>
            </div>

            {/* To */}
            <div className="flex flex-col w-1/3 items-end text-right">
              <div className="flex items-center gap-2">
                <span className="text-2xl font-bold text-gray-900">{formatTime(train.arrivalTime)}</span>
                {train.dayOffset > 0 && (
                  <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-amber-100 text-amber-800">
                    +{train.dayOffset}D
                  </span>
                )}
              </div>
              <span className="text-sm font-semibold text-gray-700 mt-1">{train.toCode}</span>
              <span className="text-xs text-gray-500 truncate">{train.toStation}</span>
            </div>
          </div>

          {/* Classes */}
          <div className="flex gap-3 overflow-x-auto pb-2 custom-scrollbar">
            {train.classAvailabilities.map((cls) => {
              const isFull = cls.statusLabel === 'FULL';
              return (
                <div 
                  key={cls.classCode}
                  className={`flex-shrink-0 min-w-[140px] p-3 rounded-lg border ${isFull ? 'bg-gray-50 border-gray-200' : 'bg-white border-gray-200 cursor-pointer hover:border-accent-400 transition-colors'}`}
                  onClick={() => !isFull && handleBook(cls.classCode)}
                >
                  <div className="flex justify-between items-center mb-2">
                    <span className={`font-semibold ${isFull ? 'text-gray-400' : 'text-gray-900'}`}>{cls.classLabel}</span>
                    <span className={`font-bold ${isFull ? 'text-gray-400' : 'text-accent-600'}`}>{formatCurrency(cls.fare)}</span>
                  </div>
                  
                  <div className={`text-xs font-medium ${
                    isFull ? 'text-gray-400' : 
                    cls.statusLabel.startsWith('AVAILABLE') ? 'text-emerald-600 font-bold' :
                    cls.statusLabel.startsWith('RAC') ? 'text-amber-600 font-bold' : 'text-red-600 font-bold'
                  }`}>
                    {cls.statusLabel}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      <LiveTrackingModal
        isOpen={isLiveModalOpen}
        onClose={() => setIsLiveModalOpen(false)}
        trainNumber={train.trainNumber}
        trainName={train.trainName}
      />
    </>
  );
};

export default TrainCard;
