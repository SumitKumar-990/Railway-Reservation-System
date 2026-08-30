import React, { useState, useEffect } from 'react';
import { Search, ArrowRightLeft, Calendar, Sparkles, TrendingUp } from 'lucide-react';
import TrainCard from '../components/TrainCard';
import StationAutocomplete from '../components/StationAutocomplete';
import { Skeleton } from '../components/LoadingSpinner';
import EmptyState from '../components/EmptyState';
import { searchTrains } from '../api/client';

const POPULAR_ROUTES = [
  { from: 'HWH', fromLabel: 'Howrah', to: 'NDLS', toLabel: 'New Delhi' },
  { from: 'BCT', fromLabel: 'Mumbai', to: 'NDLS', toLabel: 'New Delhi' },
  { from: 'JP', fromLabel: 'Jaipur', to: 'LKO', toLabel: 'Lucknow' },
  { from: 'MAS', fromLabel: 'Chennai', to: 'NDLS', toLabel: 'New Delhi' },
  { from: 'SBC', fromLabel: 'Bengaluru', to: 'NDLS', toLabel: 'New Delhi' },
  { from: 'PNBE', fromLabel: 'Patna', to: 'NDLS', toLabel: 'New Delhi' },
];

const SearchPage = () => {
  const [fromCode, setFromCode] = useState('HWH');
  const [toCode, setToCode] = useState('NDLS');
  const [date, setDate] = useState(() => {
    const today = new Date();
    // Default to tomorrow or today
    return today.toISOString().split('T')[0];
  });
  const [trains, setTrains] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [error, setError] = useState('');

  // Initial search on mount to immediately show trains
  useEffect(() => {
    performSearch('HWH', 'NDLS', date);
  }, []);

  const performSearch = async (from, to, searchDate) => {
    if (!from || !to || !searchDate) return;
    setLoading(true);
    setError('');

    try {
      const data = await searchTrains(from, to, searchDate);
      setTrains(data);
      setSearched(true);
    } catch (err) {
      setError('Failed to fetch trains. Please try another route or date.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSwap = () => {
    const temp = fromCode;
    setFromCode(toCode);
    setToCode(temp);
  };

  const handleSearch = (e) => {
    if (e) e.preventDefault();
    performSearch(fromCode, toCode, date);
  };

  const handlePopularRouteClick = (route) => {
    setFromCode(route.from);
    setToCode(route.to);
    performSearch(route.from, route.to, date);
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-12">
      {/* Hero / Search Section */}
      <div className="mb-10">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
          <div>
            <h1 className="text-4xl sm:text-5xl font-bold text-gray-900 tracking-tight mb-2">
              Where are you headed?
            </h1>
            <p className="text-base sm:text-lg text-gray-600 max-w-2xl">
              Book tickets, check seat availability, and track live trains powered by RailRadar.
            </p>
          </div>
        </div>

        {/* Popular Quick Routes */}
        <div className="mb-6 flex items-center gap-2 overflow-x-auto pb-2 custom-scrollbar">
          <span className="text-xs font-bold text-gray-500 uppercase tracking-wider flex items-center gap-1 flex-shrink-0">
            <TrendingUp className="h-3.5 w-3.5 text-accent-500" /> Popular Routes:
          </span>
          {POPULAR_ROUTES.map((r, i) => (
            <button
              key={i}
              type="button"
              onClick={() => handlePopularRouteClick(r)}
              className={`text-xs px-3 py-1.5 rounded-full border transition flex-shrink-0 flex items-center gap-1.5 ${
                fromCode === r.from && toCode === r.to
                  ? 'bg-accent-500 text-white font-semibold border-accent-600 shadow-sm'
                  : 'bg-white text-gray-700 hover:bg-gray-50 border-gray-200'
              }`}
            >
              <span>{r.fromLabel}</span>
              <span className="text-[10px] opacity-60">→</span>
              <span>{r.toLabel}</span>
            </button>
          ))}
        </div>

        <div className="bg-white p-4 sm:p-6 rounded-2xl shadow-sm border border-gray-200 relative z-20">
          <form onSubmit={handleSearch} className="flex flex-col lg:flex-row gap-4 items-end">
            <div className="w-full lg:w-[32%]">
              <StationAutocomplete
                label="From Station"
                value={fromCode}
                onChange={(code) => setFromCode(code)}
                placeholder="e.g. Howrah (HWH)"
                required
              />
            </div>

            <div className="flex justify-center -my-2 lg:my-0 lg:-mx-2 z-10">
              <button
                type="button"
                onClick={handleSwap}
                className="bg-surface-100 hover:bg-surface-200 p-2.5 rounded-full border border-gray-200 shadow-sm transition-colors focus:outline-none"
                title="Swap stations"
              >
                <ArrowRightLeft className="h-4 w-4 text-gray-600" />
              </button>
            </div>

            <div className="w-full lg:w-[32%]">
              <StationAutocomplete
                label="To Station"
                value={toCode}
                onChange={(code) => setToCode(code)}
                placeholder="e.g. New Delhi (NDLS)"
                required
              />
            </div>

            <div className="w-full lg:w-[22%]">
              <label className="block text-sm font-medium text-gray-700 mb-1">Journey Date</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                  <Calendar className="h-5 w-5" />
                </div>
                <input
                  type="date"
                  required
                  min={new Date().toISOString().split('T')[0]}
                  value={date}
                  onChange={(e) => setDate(e.target.value)}
                  className="block w-full rounded-lg border-gray-300 pl-10 py-3 shadow-sm focus:border-accent-500 focus:ring-accent-500 font-medium text-gray-900"
                />
              </div>
            </div>

            <div className="w-full lg:w-auto">
              <button
                type="submit"
                disabled={loading}
                className="w-full bg-accent-500 hover:bg-accent-600 text-white px-8 py-3 rounded-lg font-bold shadow-sm transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-accent-500 disabled:opacity-70 disabled:cursor-not-allowed flex items-center justify-center gap-2"
              >
                {loading ? (
                  <>
                    <svg className="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Searching
                  </>
                ) : (
                  <>Search Trains</>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>

      {/* Results Section */}
      <div>
        {error && (
          <div className="bg-red-50 border-l-4 border-red-500 p-4 mb-6 rounded-r-lg">
            <p className="text-sm text-red-700">{error}</p>
          </div>
        )}

        {loading && (
          <div className="space-y-4">
            {[1, 2, 3].map(i => (
              <div key={i} className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 w-full">
                <div className="flex justify-between items-start mb-6">
                  <div className="flex items-center gap-3 w-1/3">
                    <Skeleton className="h-10 w-10 rounded-lg" />
                    <div className="w-full">
                      <Skeleton className="h-5 w-3/4 mb-2" />
                      <Skeleton className="h-4 w-1/4" />
                    </div>
                  </div>
                </div>
                <div className="flex justify-between items-center mb-6">
                  <Skeleton className="h-12 w-1/4" />
                  <Skeleton className="h-8 w-1/3" />
                  <Skeleton className="h-12 w-1/4" />
                </div>
                <div className="flex gap-3">
                  <Skeleton className="h-20 w-32 rounded-lg" />
                  <Skeleton className="h-20 w-32 rounded-lg" />
                  <Skeleton className="h-20 w-32 rounded-lg" />
                </div>
              </div>
            ))}
          </div>
        )}

        {!loading && searched && trains.length === 0 && (
          <div className="bg-white rounded-2xl p-10 border border-gray-200 text-center shadow-sm max-w-xl mx-auto">
            <div className="p-4 bg-amber-50 rounded-full w-16 h-16 flex items-center justify-center mx-auto mb-4">
              <Search className="h-8 w-8 text-amber-600" />
            </div>
            <h3 className="text-lg font-bold text-gray-900 mb-1">No trains found for this day</h3>
            <p className="text-sm text-gray-500 mb-6">
              There are no scheduled runs between <span className="font-semibold text-gray-800">{fromCode}</span> and{' '}
              <span className="font-semibold text-gray-800">{toCode}</span> on {date}. Try switching dates or picking a popular daily route above.
            </p>
            <div className="flex justify-center gap-3">
              <button
                type="button"
                onClick={() => handlePopularRouteClick(POPULAR_ROUTES[0])}
                className="px-4 py-2 bg-accent-500 text-white rounded-lg text-sm font-semibold hover:bg-accent-600 transition shadow-sm"
              >
                Try Howrah → Delhi
              </button>
              <button
                type="button"
                onClick={() => handlePopularRouteClick(POPULAR_ROUTES[2])}
                className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg text-sm font-semibold hover:bg-gray-200 transition"
              >
                Try Jaipur → Lucknow
              </button>
            </div>
          </div>
        )}

        {!loading && trains.length > 0 && (
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-bold text-gray-900">
                Found {trains.length} train{trains.length > 1 ? 's' : ''} on this route
              </h2>
              <span className="text-xs text-gray-500">
                Real-time seat availability & live RailRadar tracking enabled
              </span>
            </div>
            <div className="space-y-4">
              {trains.map(train => (
                <TrainCard key={train.trainNumber} train={train} searchDate={date} />
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default SearchPage;
