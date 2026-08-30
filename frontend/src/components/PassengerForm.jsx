import React from 'react';
import { Trash2 } from 'lucide-react';

const PassengerForm = ({ index, data, onChange, onRemove, canRemove }) => {
  const handleChange = (e) => {
    const { name, value } = e.target;
    onChange(index, name, value);
  };

  return (
    <div className="bg-gray-50 p-4 rounded-lg border border-gray-200 relative group">
      <div className="flex justify-between items-center mb-3">
        <h4 className="font-medium text-gray-700">Passenger {index + 1}</h4>
        {canRemove && (
          <button
            type="button"
            onClick={() => onRemove(index)}
            className="text-gray-400 hover:text-red-500 transition"
            title="Remove Passenger"
          >
            <Trash2 className="h-4 w-4" />
          </button>
        )}
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="md:col-span-1">
          <label className="block text-xs font-medium text-gray-600 mb-1">Full Name</label>
          <input
            type="text"
            name="name"
            value={data.name}
            onChange={handleChange}
            required
            className="w-full rounded-md border-gray-300 shadow-sm focus:border-accent-500 focus:ring-accent-500 sm:text-sm"
            placeholder="Name as per ID"
          />
        </div>
        
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">Age</label>
          <input
            type="number"
            name="age"
            value={data.age}
            onChange={handleChange}
            required
            min="1"
            max="120"
            className="w-full rounded-md border-gray-300 shadow-sm focus:border-accent-500 focus:ring-accent-500 sm:text-sm"
            placeholder="Age"
          />
        </div>
        
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">Gender</label>
          <select
            name="gender"
            value={data.gender}
            onChange={handleChange}
            required
            className="w-full rounded-md border-gray-300 shadow-sm focus:border-accent-500 focus:ring-accent-500 sm:text-sm"
          >
            <option value="">Select Gender</option>
            <option value="Male">Male</option>
            <option value="Female">Female</option>
            <option value="Other">Other</option>
          </select>
        </div>
        
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">Berth Pref</label>
          <select
            name="berthPreference"
            value={data.berthPreference}
            onChange={handleChange}
            className="w-full rounded-md border-gray-300 shadow-sm focus:border-accent-500 focus:ring-accent-500 sm:text-sm"
          >
            <option value="NO_PREFERENCE">No Preference</option>
            <option value="LOWER">Lower</option>
            <option value="MIDDLE">Middle</option>
            <option value="UPPER">Upper</option>
            <option value="SIDE_LOWER">Side Lower</option>
            <option value="SIDE_UPPER">Side Upper</option>
          </select>
        </div>
      </div>
    </div>
  );
};

export default PassengerForm;
