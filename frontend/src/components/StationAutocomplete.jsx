import React, { useState, useEffect, useRef } from 'react';
import { MapPin, Search, X } from 'lucide-react';
import { getStations } from '../api/client';

const StationAutocomplete = ({
  label,
  value,
  onChange,
  placeholder = 'Station name or code',
  required = false
}) => {
  const [query, setQuery] = useState(value || '');
  const [stations, setStations] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [isOpen, setIsOpen] = useState(false);
  const [highlightIndex, setHighlightIndex] = useState(-1);
  const containerRef = useRef(null);

  useEffect(() => {
    const fetchAllStations = async () => {
      try {
        const data = await getStations();
        setStations(data);
      } catch (err) {
        console.error('Failed to load stations', err);
      }
    };
    fetchAllStations();
  }, []);

  useEffect(() => {
    setQuery(value || '');
  }, [value]);

  useEffect(() => {
    if (!query.trim()) {
      setFiltered(stations.slice(0, 8));
      return;
    }

    const q = query.trim().toLowerCase();
    const matches = stations.filter(
      (s) =>
        s.code.toLowerCase().includes(q) ||
        s.name.toLowerCase().includes(q) ||
        s.city.toLowerCase().includes(q)
    );
    setFiltered(matches);
  }, [query, stations]);

  // Click outside to close
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSelect = (station) => {
    onChange(station.code);
    setQuery(`${station.name} (${station.code})`);
    setIsOpen(false);
    setHighlightIndex(-1);
  };

  const handleKeyDown = (e) => {
    if (!isOpen) {
      if (e.key === 'ArrowDown' || e.key === 'Enter') {
        setIsOpen(true);
      }
      return;
    }

    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setHighlightIndex((prev) => (prev < filtered.length - 1 ? prev + 1 : 0));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setHighlightIndex((prev) => (prev > 0 ? prev - 1 : filtered.length - 1));
    } else if (e.key === 'Enter') {
      e.preventDefault();
      if (highlightIndex >= 0 && highlightIndex < filtered.length) {
        handleSelect(filtered[highlightIndex]);
      } else if (filtered.length > 0) {
        handleSelect(filtered[0]);
      }
    } else if (e.key === 'Escape') {
      setIsOpen(false);
    }
  };

  return (
    <div className="relative w-full" ref={containerRef}>
      {label && (
        <label className="block text-sm font-medium text-gray-700 mb-1">
          {label}
        </label>
      )}
      <div className="relative">
        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
          <MapPin className="h-5 w-5" />
        </div>
        <input
          type="text"
          required={required}
          value={query}
          onFocus={() => setIsOpen(true)}
          onChange={(e) => {
            setQuery(e.target.value);
            onChange(e.target.value);
            setIsOpen(true);
          }}
          onKeyDown={handleKeyDown}
          placeholder={placeholder}
          className="block w-full rounded-lg border-gray-300 pl-10 pr-8 py-3 shadow-sm focus:border-accent-500 focus:ring-accent-500 font-medium text-gray-900 placeholder:text-gray-400 placeholder:font-normal"
        />
        {query && (
          <button
            type="button"
            onClick={() => {
              setQuery('');
              onChange('');
            }}
            className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
          >
            <X className="h-4 w-4" />
          </button>
        )}
      </div>

      {isOpen && (
        <div className="absolute z-50 mt-1 w-full bg-white rounded-xl shadow-xl border border-gray-200 py-2 max-h-64 overflow-y-auto custom-scrollbar">
          {filtered.length === 0 ? (
            <div className="px-4 py-3 text-sm text-gray-500 text-center">
              No matching stations found
            </div>
          ) : (
            filtered.map((station, idx) => {
              const isSelected = station.code === value;
              const isHighlighted = idx === highlightIndex;
              return (
                <div
                  key={station.code}
                  onClick={() => handleSelect(station)}
                  onMouseEnter={() => setHighlightIndex(idx)}
                  className={`px-4 py-2.5 flex items-center justify-between cursor-pointer transition-colors ${
                    isHighlighted || isSelected
                      ? 'bg-accent-50 text-accent-900'
                      : 'hover:bg-gray-50 text-gray-800'
                  }`}
                >
                  <div className="flex flex-col">
                    <span className="font-semibold text-sm text-gray-900">
                      {station.name}
                    </span>
                    <span className="text-xs text-gray-500">{station.city}</span>
                  </div>
                  <span className="font-mono text-xs font-bold px-2 py-1 bg-surface-100 rounded text-accent-700 border border-gray-200">
                    {station.code}
                  </span>
                </div>
              );
            })
          )}
        </div>
      )}
    </div>
  );
};

export default StationAutocomplete;
