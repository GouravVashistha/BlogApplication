import api from './api';

// Fetch all registered users
export const getAllUsers = async () => {
  const response = await api.get('/api/users/AllUsers');
  return response.data;
};

// Promote a user to Admin
export const promoteUserToAdmin = async (userId) => {
  const response = await api.put(`/api/users/${userId}/assign-admin`);
  return response.data;
};

// Delete a user
export const deleteUser = async (userId) => {
  const response = await api.delete(`/api/users/deleteUser/${userId}`);
  return response.data;
};
