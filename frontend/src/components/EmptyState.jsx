import React from 'react';
import { Link } from 'react-router-dom';

const EmptyState = ({ icon: Icon, message, actionLabel, actionHref }) => {
  return (
    <div className="flex flex-col items-center justify-center p-12 bg-white rounded-xl shadow-sm border border-gray-100 text-center">
      <div className="h-16 w-16 bg-surface-100 rounded-full flex items-center justify-center mb-4 text-gray-400">
        <Icon className="h-8 w-8" />
      </div>
      <h3 className="text-lg font-medium text-gray-900 mb-2">{message}</h3>
      {actionLabel && actionHref && (
        <Link 
          to={actionHref}
          className="mt-4 inline-flex items-center px-4 py-2 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-accent-500 hover:bg-accent-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-accent-500"
        >
          {actionLabel}
        </Link>
      )}
    </div>
  );
};

export default EmptyState;
