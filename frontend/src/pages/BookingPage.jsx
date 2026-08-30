import React, { useState, useEffect } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { Users, Plus, CheckCircle2, ArrowRight } from 'lucide-react';
import { searchTrains, createBooking } from '../api/client';
import PassengerForm from '../components/PassengerForm';
import LoadingSpinner from '../components/LoadingSpinner';
import { formatTime, formatDuration, formatCurrency } from '../utils/formatters';

const BookingPage = () => {
  const { trainNumber } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const fromCode = searchParams.get('from');
  const toCode = searchParams.get('to');
  const date = searchParams.get('date');
  const initialClass = searchParams.get('class');

  const [train, setTrain] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [selectedClass, setSelectedClass] = useState(initialClass || '');
  const [passengers, setPassengers] = useState([
    { name: '', age: '', gender: '', berthPreference: 'NO_PREFERENCE' }
  ]);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const fetchTrain = async () => {
      try {
        const results = await searchTrains(fromCode, toCode, date);
        const currentTrain = results.find(t => t.trainNumber === trainNumber);
        
        if (currentTrain) {
          setTrain(currentTrain);
          if (!selectedClass && currentTrain.classAvailabilities.length > 0) {
            setSelectedClass(currentTrain.classAvailabilities[0].classCode);
          }
        } else {
          setError('Train not found for the selected route and date.');
        }
      } catch (err) {
        setError('Failed to fetch train details.');
      } finally {
        setLoading(false);
      }
    };

    fetchTrain();
  }, [trainNumber, fromCode, toCode, date]);

  const handleAddPassenger = () => {
    setPassengers([...passengers, { name: '', age: '', gender: '', berthPreference: 'NO_PREFERENCE' }]);
  };

  const handleRemovePassenger = (index) => {
    if (passengers.length > 1) {
      setPassengers(passengers.filter((_, i) => i !== index));
    }
  };

  const handlePassengerChange = (index, field, value) => {
    const newPassengers = [...passengers];
    newPassengers[index][field] = value;
    setPassengers(newPassengers);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError('');
    
    try {
      const formattedPassengers = passengers.map(p => ({
        ...p,
        age: parseInt(p.age, 10)
      }));

      const bookingData = {
        trainNumber,
        date,
        fromStationCode: fromCode,
        toStationCode: toCode,
        seatClassCode: selectedClass,
        passengers: formattedPassengers
      };

      const response = await createBooking(bookingData);
      navigate(`/payment/${response.pnr}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create booking.');
      setIsSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-[60vh]">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (error || !train) {
    return (
      <div className="max-w-3xl mx-auto mt-12 p-6 bg-red-50 text-red-700 rounded-xl border border-red-200">
        <h3 className="text-lg font-semibold mb-2">Error</h3>
        <p>{error}</p>
        <button 
          onClick={() => navigate('/')}
          className="mt-4 px-4 py-2 bg-red-100 hover:bg-red-200 rounded-lg text-sm font-medium transition"
        >
          Go Back
        </button>
      </div>
    );
  }

  const selectedClassInfo = train.classAvailabilities.find(c => c.classCode === selectedClass);
  const totalFare = selectedClassInfo ? selectedClassInfo.fare * passengers.length : 0;

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex items-center gap-2 text-sm text-gray-500 mb-6">
        <span>Search</span>
        <ArrowRight className="h-4 w-4" />
        <span className="font-semibold text-gray-900">Passenger Details</span>
        <ArrowRight className="h-4 w-4" />
        <span>Payment</span>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-6">
          {/* Train Info Summary */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <div className="flex justify-between items-start mb-4 pb-4 border-b border-gray-100">
              <div>
                <h2 className="text-xl font-bold text-gray-900">{train.trainName}</h2>
                <p className="text-gray-500 font-mono text-sm">#{train.trainNumber}</p>
              </div>
              <div className="text-right">
                <div className="text-sm font-semibold text-accent-600 bg-accent-50 px-3 py-1 rounded-full">
                  {new Date(date).toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })}
                </div>
              </div>
            </div>
            
            <div className="flex justify-between items-center">
              <div>
                <p className="text-2xl font-bold text-gray-900">{formatTime(train.departureTime)}</p>
                <p className="font-medium text-gray-700">{train.fromCode}</p>
              </div>
              <div className="flex flex-col items-center px-4">
                <span className="text-xs text-gray-500 mb-1">{formatDuration(train.departureTime, train.arrivalTime, train.dayOffset)}</span>
                <div className="w-32 border-t-2 border-dashed border-gray-300 relative">
                  <div className="absolute -top-1.5 -left-1 w-3 h-3 rounded-full bg-gray-300"></div>
                  <div className="absolute -top-1.5 -right-1 w-3 h-3 rounded-full bg-accent-500"></div>
                </div>
              </div>
              <div className="text-right">
                <p className="text-2xl font-bold text-gray-900">{formatTime(train.arrivalTime)}</p>
                <p className="font-medium text-gray-700">{train.toCode}</p>
              </div>
            </div>
          </div>

          {/* Class Selection */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Select Class</h3>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              {train.classAvailabilities.map(cls => {
                const isSelected = selectedClass === cls.classCode;
                const isFull = cls.statusLabel === 'FULL';
                
                return (
                  <div 
                    key={cls.classCode}
                    onClick={() => !isFull && setSelectedClass(cls.classCode)}
                    className={`border rounded-lg p-3 cursor-pointer transition-all ${
                      isSelected 
                        ? 'border-accent-500 bg-accent-50 ring-1 ring-accent-500' 
                        : isFull 
                          ? 'border-gray-200 bg-gray-50 opacity-60 cursor-not-allowed'
                          : 'border-gray-200 hover:border-accent-300'
                    }`}
                  >
                    <div className="flex justify-between items-center mb-2">
                      <span className="font-semibold text-gray-900">{cls.classCode}</span>
                      {isSelected && <CheckCircle2 className="h-5 w-5 text-accent-600" />}
                    </div>
                    <div className="text-sm font-bold text-gray-900 mb-1">{formatCurrency(cls.fare)}</div>
                    <div className={`text-xs font-medium ${
                      isFull ? 'text-gray-500' :
                      cls.statusLabel.startsWith('AVL') ? 'text-emerald-600' : 
                      cls.statusLabel.startsWith('RAC') ? 'text-amber-600' : 'text-red-600'
                    }`}>
                      {cls.statusLabel}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Passengers */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
                <Users className="h-5 w-5 text-gray-500" />
                Passenger Details
              </h3>
              <span className="text-sm text-gray-500">{passengers.length} Passenger(s)</span>
            </div>
            
            <form id="booking-form" onSubmit={handleSubmit} className="space-y-4">
              {passengers.map((p, index) => (
                <PassengerForm 
                  key={index}
                  index={index}
                  data={p}
                  onChange={handlePassengerChange}
                  onRemove={handleRemovePassenger}
                  canRemove={passengers.length > 1}
                />
              ))}
              
              {passengers.length < 6 && (
                <button
                  type="button"
                  onClick={handleAddPassenger}
                  className="flex items-center gap-2 text-accent-600 hover:text-accent-700 font-medium text-sm mt-4 p-2 rounded-lg hover:bg-accent-50 transition"
                >
                  <Plus className="h-4 w-4" /> Add Passenger
                </button>
              )}
            </form>
          </div>
        </div>

        {/* Sidebar Summary */}
        <div className="lg:col-span-1">
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 sticky top-24">
            <h3 className="text-lg font-semibold text-gray-900 mb-4 pb-4 border-b border-gray-100">Fare Summary</h3>
            
            <div className="space-y-3 mb-6">
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Ticket Fare ({passengers.length} x {formatCurrency(selectedClassInfo?.fare)})</span>
                <span className="font-medium">{formatCurrency(totalFare)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Convenience Fee</span>
                <span className="font-medium text-emerald-600">Free</span>
              </div>
              
              <div className="pt-3 border-t border-gray-100 flex justify-between items-center">
                <span className="font-bold text-gray-900">Total Amount</span>
                <span className="text-xl font-bold text-accent-600">{formatCurrency(totalFare)}</span>
              </div>
            </div>

            <button
              form="booking-form"
              type="submit"
              disabled={isSubmitting || !selectedClass}
              className="w-full bg-accent-500 hover:bg-accent-600 text-white py-3 px-4 rounded-lg font-semibold shadow-sm transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-accent-500 disabled:opacity-70 disabled:cursor-not-allowed flex justify-center items-center gap-2"
            >
              {isSubmitting ? (
                <>
                  <LoadingSpinner size="sm" className="text-white" />
                  Processing...
                </>
              ) : (
                'Proceed to Payment'
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BookingPage;
