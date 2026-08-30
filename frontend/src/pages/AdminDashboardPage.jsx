import React, { useState, useEffect } from 'react';
import { ShieldCheck, Train, Users, AlertCircle, CheckCircle, RefreshCw, XCircle } from 'lucide-react';
import { getAdminTrains, getAdminTrainOccupancy, cancelAdminTrainRun } from '../api/client';
import LoadingSpinner from '../components/LoadingSpinner';
import ConfirmDialog from '../components/ConfirmDialog';

const AdminDashboardPage = () => {
  const [trains, setTrains] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedTrainRunId, setSelectedTrainRunId] = useState(1);
  const [occupancy, setOccupancy] = useState(null);
  const [occupancyLoading, setOccupancyLoading] = useState(false);
  const [isCancelDialogOpen, setIsCancelDialogOpen] = useState(false);
  const [isCancelling, setIsCancelling] = useState(false);

  const fetchTrains = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getAdminTrains();
      setTrains(data);
    } catch (err) {
      setError('Failed to fetch train fleet data. Ensure you are logged in as ADMIN.');
    } finally {
      setLoading(false);
    }
  };

  const fetchOccupancy = async (runId) => {
    if (!runId) return;
    setOccupancyLoading(true);
    try {
      const data = await getAdminTrainOccupancy(runId);
      setOccupancy(data);
    } catch (err) {
      console.error('Failed to fetch train run occupancy', err);
    } finally {
      setOccupancyLoading(false);
    }
  };

  useEffect(() => {
    fetchTrains();
    fetchOccupancy(selectedTrainRunId);
  }, []);

  const handleSelectTrainRun = (runId) => {
    setSelectedTrainRunId(runId);
    fetchOccupancy(runId);
  };

  const handleCancelRun = async () => {
    setIsCancelling(true);
    try {
      await cancelAdminTrainRun(selectedTrainRunId);
      await fetchOccupancy(selectedTrainRunId);
      setIsCancelDialogOpen(false);
      alert(`Train run #${selectedTrainRunId} has been cancelled and bookings updated.`);
    } catch (err) {
      alert('Failed to cancel train run.');
    } finally {
      setIsCancelling(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-[60vh]">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8">
        <div className="flex items-center gap-3">
          <div className="p-3 bg-amber-100 rounded-xl">
            <ShieldCheck className="h-8 w-8 text-amber-700" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Admin Control Center</h1>
            <p className="text-sm text-gray-500">Fleet management, seat occupancy inspection, and train schedules</p>
          </div>
        </div>

        <button
          onClick={fetchTrains}
          className="inline-flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 rounded-lg text-sm font-semibold hover:bg-gray-50 shadow-sm transition"
        >
          <RefreshCw className="h-4 w-4 text-gray-600" /> Refresh Fleet
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border-l-4 border-red-500 p-4 mb-6 rounded-r-lg flex items-center gap-3">
          <AlertCircle className="h-5 w-5 text-red-600" />
          <p className="text-sm text-red-700">{error}</p>
        </div>
      )}

      {/* Summary Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 mb-8">
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-200 flex items-center justify-between">
          <div>
            <p className="text-xs font-bold text-gray-500 uppercase tracking-wider">Total Active Trains</p>
            <p className="text-3xl font-bold text-gray-900 mt-1">{trains.length}</p>
          </div>
          <div className="p-3 bg-blue-50 rounded-xl">
            <Train className="h-6 w-6 text-blue-600" />
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-200 flex items-center justify-between">
          <div>
            <p className="text-xs font-bold text-gray-500 uppercase tracking-wider">Database Lock Mode</p>
            <p className="text-xl font-bold text-emerald-600 mt-1">PESSIMISTIC_WRITE</p>
          </div>
          <div className="p-3 bg-emerald-50 rounded-xl">
            <CheckCircle className="h-6 w-6 text-emerald-600" />
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-200 flex items-center justify-between">
          <div>
            <p className="text-xs font-bold text-gray-500 uppercase tracking-wider">Live Sync Provider</p>
            <p className="text-xl font-bold text-accent-600 mt-1">RailRadar Live</p>
          </div>
          <div className="p-3 bg-amber-50 rounded-xl">
            <ShieldCheck className="h-6 w-6 text-accent-600" />
          </div>
        </div>
      </div>

      {/* Train Fleet Table */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden mb-8">
        <div className="p-6 border-b border-gray-100 flex justify-between items-center">
          <h2 className="text-xl font-bold text-gray-900">Registered Train Fleet</h2>
          <span className="text-xs text-gray-500 font-mono">Showing {trains.length} trains</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-surface-50 text-gray-600 uppercase text-[11px] font-bold tracking-wider border-b border-gray-200">
              <tr>
                <th className="px-6 py-3">Train Number</th>
                <th className="px-6 py-3">Train Name</th>
                <th className="px-6 py-3">Running Days</th>
                <th className="px-6 py-3">Stops</th>
                <th className="px-6 py-3">Seat Classes</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {trains.map((t) => (
                <tr key={t.id} className="hover:bg-gray-50/80 transition">
                  <td className="px-6 py-4 font-mono font-bold text-accent-700">#{t.trainNumber}</td>
                  <td className="px-6 py-4 font-semibold text-gray-900">{t.name}</td>
                  <td className="px-6 py-4">
                    <span className="px-2.5 py-1 bg-surface-100 rounded-md text-xs font-mono text-gray-700">
                      {t.runningDays}
                    </span>
                  </td>
                  <td className="px-6 py-4 font-medium text-gray-600">{t.stopCount || 4} stops</td>
                  <td className="px-6 py-4 font-mono text-xs text-gray-500">{t.classes?.join(', ') || 'SL, 3A, 2A, 1A'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Occupancy Inspector */}
      {occupancy && (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6 pb-4 border-b border-gray-100">
            <div>
              <div className="flex items-center gap-2">
                <span className="font-mono text-xs font-bold px-2 py-0.5 bg-accent-100 text-accent-800 rounded">
                  #{occupancy.trainNumber}
                </span>
                <span className="text-xs text-gray-500">Run Date: {occupancy.runDate}</span>
                <span className={`text-xs font-bold px-2 py-0.5 rounded ${occupancy.status === 'CANCELLED' ? 'bg-red-100 text-red-700' : 'bg-emerald-100 text-emerald-700'}`}>
                  {occupancy.status}
                </span>
              </div>
              <h3 className="text-xl font-bold text-gray-900 mt-1">{occupancy.trainName}</h3>
            </div>

            {occupancy.status !== 'CANCELLED' && (
              <button
                onClick={() => setIsCancelDialogOpen(true)}
                className="inline-flex items-center gap-2 px-4 py-2 bg-red-50 text-red-700 hover:bg-red-100 border border-red-200 rounded-lg text-sm font-semibold transition"
              >
                <XCircle className="h-4 w-4" /> Cancel Train Run
              </button>
            )}
          </div>

          <h4 className="text-sm font-bold text-gray-700 uppercase tracking-wider mb-4">Class-Wise Real-Time Occupancy</h4>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
            {occupancy.classOccupancies?.map((cls) => {
              const bookedPct = Math.round((cls.confirmedBooked / cls.totalSeats) * 100);
              return (
                <div key={cls.classCode} className="p-4 bg-surface-50 rounded-xl border border-gray-200">
                  <div className="flex justify-between items-center mb-2">
                    <span className="font-bold text-base text-gray-900">{cls.classCode}</span>
                    <span className="text-xs text-gray-500">{cls.classLabel}</span>
                  </div>
                  
                  <div className="space-y-1.5 text-xs text-gray-600 mb-3">
                    <div className="flex justify-between">
                      <span>Confirmed Booked:</span>
                      <span className="font-bold text-gray-900">{cls.confirmedBooked} / {cls.totalSeats}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>RAC Allocated:</span>
                      <span className="font-bold text-amber-700">{cls.racBooked} / {cls.racQuota}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Waitlisted:</span>
                      <span className="font-bold text-red-700">{cls.waitlistCount}</span>
                    </div>
                  </div>

                  <div className="w-full bg-gray-200 h-2 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full ${bookedPct > 90 ? 'bg-red-500' : bookedPct > 50 ? 'bg-amber-500' : 'bg-emerald-500'}`}
                      style={{ width: `${Math.min(100, bookedPct)}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      <ConfirmDialog
        isOpen={isCancelDialogOpen}
        title="Cancel Train Run"
        message={`Are you sure you want to cancel the run for ${occupancy?.trainName} on ${occupancy?.runDate}? All booked passengers will be refunded.`}
        confirmLabel="Confirm Cancellation"
        onConfirm={handleCancelRun}
        onCancel={() => setIsCancelDialogOpen(false)}
        isLoading={isCancelling}
      />
    </div>
  );
};

export default AdminDashboardPage;
