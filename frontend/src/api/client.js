import axios from 'axios';

const client = axios.create({
  baseURL: '',
  headers: {
    'Content-Type': 'application/json',
  },
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const login = async (credentials) => {
  const response = await client.post('/api/auth/login', credentials);
  return response.data;
};

export const register = async (data) => {
  const response = await client.post('/api/auth/register', data);
  return response.data;
};

export const getStations = async () => {
  const response = await client.get('/api/stations');
  return response.data;
};

export const searchStations = async (query) => {
  const response = await client.get(`/api/stations/search?q=${encodeURIComponent(query || '')}`);
  return response.data;
};

export const searchTrains = async (from, to, date) => {
  const response = await client.get(`/api/trains/search?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&date=${date}`);
  return response.data;
};

export const getTrainLiveStatus = async (trainNumber) => {
  const response = await client.get(`/api/trains/${trainNumber}/live`);
  return response.data;
};

export const getTrainDetails = async (trainNumber) => {
  const response = await client.get(`/api/trains/${trainNumber}/details`);
  return response.data;
};

export const createBooking = async (data) => {
  const response = await client.post('/api/bookings', data);
  return response.data;
};

export const getBookings = async () => {
  const response = await client.get('/api/bookings');
  return response.data;
};

export const getBooking = async (pnr) => {
  const response = await client.get(`/api/bookings/${pnr}`);
  return response.data;
};

export const cancelBooking = async (pnr) => {
  const response = await client.delete(`/api/bookings/${pnr}`);
  return response.data;
};

export const initiatePayment = async (data) => {
  const response = await client.post('/api/payments/initiate', data);
  return response.data;
};

export const confirmPayment = async (data) => {
  const response = await client.post('/api/payments/webhook', data);
  return response.data;
};

export const getAdminTrains = async () => {
  const response = await client.get('/api/admin/trains');
  return response.data;
};

export const getAdminTrainOccupancy = async (trainRunId) => {
  const response = await client.get(`/api/admin/train-runs/${trainRunId}/occupancy`);
  return response.data;
};

export const cancelAdminTrainRun = async (trainRunId) => {
  const response = await client.post(`/api/admin/train-runs/${trainRunId}/cancel`);
  return response.data;
};

export default client;
