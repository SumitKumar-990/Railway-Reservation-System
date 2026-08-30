import React, { useState, useEffect } from 'react';
import { X, Train, Clock, MapPin, AlertTriangle, CheckCircle, RefreshCw } from 'lucide-react';
import { getTrainLiveStatus } from '../api/client';
import LoadingSpinner from './LoadingSpinner';

const LiveTrackingModal = ({ isOpen, onClose, trainNumber, trainName }) => {
  const [liveData, setLiveData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [refreshing, setRefreshing] = useState(false);

  const fetchLiveStatus = async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError('');

    try {
      const data = await getTrainLiveStatus(trainNumber);
      setLiveData(data);
    } catch (err) {
      setError('Could not fetch real-time live data for this train.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    if (isOpen && trainNumber) {
      fetchLiveStatus();
    }
  }, [isOpen, trainNumber]);

  if (!isOpen) return null;

  const isDelayed = liveData?.delayMinutes > 0;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm overflow-y-auto">
      <div className="bg-white rounded-2xl max-w-2xl w-full shadow-2xl overflow-hidden border border-gray-100 max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="bg-gradient-to-r from-[#1a1a2e] to-[#252542] text-white p-6 flex justify-between items-start">
          <div className="flex items-center gap-3">
            <div className="p-3 bg-white/10 rounded-xl">
              <Train className="h-6 w-6 text-accent-400" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-mono text-xs font-bold px-2 py-0.5 bg-accent-500/20 text-accent-300 rounded border border-accent-500/30">
                  #{trainNumber}
                </span>
                <span className="text-xs text-gray-300">Live RailRadar Sync</span>
              </div>
              <h3 className="text-xl font-bold mt-1 text-white">
                {liveData?.trainName || trainName || 'Train Status'}
              </h3>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => fetchLiveStatus(true)}
              disabled={refreshing || loading}
              className="p-2 text-gray-300 hover:text-white hover:bg-white/10 rounded-lg transition"
              title="Refresh live status"
            >
              <RefreshCw className={`h-5 w-5 ${refreshing ? 'animate-spin' : ''}`} />
            </button>
            <button
              onClick={onClose}
              className="p-2 text-gray-300 hover:text-white hover:bg-white/10 rounded-lg transition"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto custom-scrollbar flex-1 space-y-6">
          {loading ? (
            <div className="py-12 flex flex-col items-center justify-center gap-3">
              <LoadingSpinner size="lg" />
              <p className="text-sm text-gray-500">Connecting to RailRadar live satellite tracking...</p>
            </div>
          ) : error ? (
            <div className="p-4 bg-red-50 text-red-700 rounded-xl border border-red-200 text-sm">
              {error}
            </div>
          ) : (
            <>
              {/* Status Banner */}
              <div
                className={`p-4 rounded-xl flex items-center justify-between border ${
                  isDelayed
                    ? 'bg-amber-50 border-amber-200 text-amber-900'
                    : 'bg-emerald-50 border-emerald-200 text-emerald-900'
                }`}
              >
                <div className="flex items-center gap-3">
                  {isDelayed ? (
                    <AlertTriangle className="h-6 w-6 text-amber-600" />
                  ) : (
                    <CheckCircle className="h-6 w-6 text-emerald-600" />
                  )}
                  <div>
                    <h4 className="font-bold text-base">
                      {liveData.status === 'not-started'
                        ? 'Scheduled (Not yet departed)'
                        : isDelayed
                        ? `Delayed by ${liveData.delayMinutes} mins`
                        : 'Running On Time'}
                    </h4>
                    <p className="text-xs opacity-80">
                      Last updated: {liveData.lastUpdatedAt ? new Date(liveData.lastUpdatedAt).toLocaleTimeString() : 'Just now'}
                    </p>
                  </div>
                </div>
                {liveData.nextHalt?.platform && (
                  <div className="text-right">
                    <span className="text-xs block opacity-75">Platform</span>
                    <span className="text-lg font-bold">PF #{liveData.nextHalt.platform}</span>
                  </div>
                )}
              </div>

              {/* Next Halt & Current Location */}
              {liveData.nextHalt && (
                <div className="bg-surface-50 p-4 rounded-xl border border-gray-200 grid grid-cols-2 gap-4">
                  <div>
                    <span className="text-xs font-semibold text-gray-500 uppercase tracking-wide">Next Halt</span>
                    <p className="font-bold text-gray-900 text-base">{liveData.nextHalt.stationName}</p>
                    <p className="text-xs text-gray-600 font-mono">Code: {liveData.nextHalt.stationCode}</p>
                  </div>
                  <div>
                    <span className="text-xs font-semibold text-gray-500 uppercase tracking-wide">Expected Arrival</span>
                    <p className="font-bold text-gray-900 text-base">
                      {liveData.nextHalt.scheduledArrival ? new Date(liveData.nextHalt.scheduledArrival).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'On Schedule'}
                    </p>
                    {liveData.nextHalt.delayArrival > 0 && (
                      <p className="text-xs text-amber-600 font-medium">+{liveData.nextHalt.delayArrival}m delay</p>
                    )}
                  </div>
                </div>
              )}

              {/* Coach Position */}
              {liveData.coachPosition && (
                <div>
                  <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Coach Position</h4>
                  <div className="p-3 bg-gray-900 text-amber-400 font-mono text-xs rounded-xl overflow-x-auto whitespace-nowrap shadow-inner">
                    {liveData.coachPosition}
                  </div>
                </div>
              )}

              {/* Route Halts Timeline */}
              {liveData.halts && liveData.halts.length > 0 && (
                <div>
                  <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-3">Live Route Halts</h4>
                  <div className="space-y-3 relative before:absolute before:left-3 before:top-2 before:bottom-2 before:w-0.5 before:bg-gray-200">
                    {liveData.halts.map((halt, idx) => {
                      const isPassed = halt.status === 'departed' || halt.status === 'passed';
                      return (
                        <div key={idx} className="flex items-start gap-4 relative pl-8">
                          <div
                            className={`absolute left-1.5 top-1.5 w-3.5 h-3.5 rounded-full border-2 bg-white ${
                              isPassed ? 'border-gray-400 bg-gray-400' : 'border-accent-500 bg-accent-500'
                            }`}
                          />
                          <div className="flex-1 flex justify-between items-center text-sm">
                            <div>
                              <p className={`font-semibold ${isPassed ? 'text-gray-500' : 'text-gray-900'}`}>
                                {halt.stationName} ({halt.stationCode})
                              </p>
                              <p className="text-xs text-gray-400">{halt.distance} km from origin</p>
                            </div>
                            <div className="text-right">
                              <p className="font-mono text-xs font-medium text-gray-700">
                                {halt.scheduledArrival
                                  ? new Date(halt.scheduledArrival).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                                  : halt.scheduledDeparture
                                  ? new Date(halt.scheduledDeparture).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                                  : '--:--'}
                              </p>
                              {halt.delayArrival > 0 && (
                                <span className="text-[10px] text-amber-600 font-bold">+{halt.delayArrival}m</span>
                              )}
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}
            </>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 bg-gray-50 border-t border-gray-200 flex justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2 bg-gray-800 hover:bg-gray-900 text-white rounded-lg text-sm font-medium transition"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default LiveTrackingModal;
