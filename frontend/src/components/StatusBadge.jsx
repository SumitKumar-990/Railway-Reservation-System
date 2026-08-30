import React from 'react';
import { getStatusColor } from '../utils/formatters';

const StatusBadge = ({ status }) => {
  const colorClass = getStatusColor(status);
  
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium uppercase tracking-wide ${colorClass}`}>
      {status}
    </span>
  );
};

export default StatusBadge;
