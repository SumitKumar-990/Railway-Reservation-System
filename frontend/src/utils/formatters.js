export const formatTime = (timeString) => {
  if (!timeString) return '';
  const [hours, minutes] = timeString.split(':');
  const date = new Date();
  date.setHours(parseInt(hours, 10));
  date.setMinutes(parseInt(minutes, 10));
  
  return date.toLocaleTimeString('en-US', {
    hour: 'numeric',
    minute: '2-digit',
    hour12: true
  });
};

export const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  });
};

export const formatCurrency = (amount) => {
  if (amount === undefined || amount === null) return '';
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2
  }).format(amount);
};

export const formatDuration = (dep, arr, dayOffset = 0) => {
  if (!dep || !arr) return '';
  
  const [depH, depM] = dep.split(':').map(Number);
  const [arrH, arrM] = arr.split(':').map(Number);
  
  let depTotalMinutes = depH * 60 + depM;
  let arrTotalMinutes = arrH * 60 + arrM + (dayOffset * 24 * 60);
  
  const diffMinutes = arrTotalMinutes - depTotalMinutes;
  
  const h = Math.floor(diffMinutes / 60);
  const m = diffMinutes % 60;
  
  return `${h}h ${m}m`;
};

export const getStatusColor = (status) => {
  switch (status?.toUpperCase()) {
    case 'CONFIRMED': return 'bg-emerald-100 text-emerald-800';
    case 'RAC': return 'bg-amber-100 text-amber-800';
    case 'WAITLISTED': return 'bg-red-100 text-red-800';
    case 'CANCELLED': return 'bg-gray-100 text-gray-600';
    case 'EXPIRED': return 'bg-gray-100 text-gray-500';
    default: return 'bg-gray-100 text-gray-600';
  }
};
