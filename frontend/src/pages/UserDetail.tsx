import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeftIcon, SaveIcon } from '@heroicons/react/24/outline';
import axios from 'axios';

interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  isActive: boolean;
  role: string;
  createdAt: string;
  updatedAt: string;
}

const UserDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState<Partial<User>>({});

  const { data: user, isLoading, error } = useQuery({
    queryKey: ['user', id],
    queryFn: async () => {
      const response = await axios.get(`/api/v1/users/${id}`);
      return response.data.data as User;
    },
    onSuccess: (data) => {
      setFormData(data);
    },
  });

  const updateUserMutation = useMutation({
    mutationFn: async (updatedUser: Partial<User>) => {
      const response = await axios.put(`/api/v1/users/${id}`, updatedUser);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user', id] });
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setIsEditing(false);
    },
  });

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value,
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateUserMutation.mutate(formData);
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (error || !user) {
    return (
      <div className="text-center py-12">
        <p className="text-red-600">Error loading user</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <button
            onClick={() => navigate('/users')}
            className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-md transition-colors duration-200"
          >
            <ArrowLeftIcon className="w-5 h-5" />
          </button>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">
              {isEditing ? 'Edit User' : user.firstName} {user.lastName}
            </h1>
            <p className="text-gray-600 mt-2">
              {isEditing ? 'Update user information' : 'User details and settings'}
            </p>
          </div>
        </div>
        <div className="flex items-center space-x-3">
          {!isEditing && (
            <button
              onClick={() => setIsEditing(true)}
              className="btn-secondary"
            >
              Edit User
            </button>
          )}
          {isEditing && (
            <>
              <button
                onClick={() => {
                  setIsEditing(false);
                  setFormData(user);
                }}
                className="btn-secondary"
              >
                Cancel
              </button>
              <button
                onClick={handleSubmit}
                disabled={updateUserMutation.isPending}
                className="btn-primary flex items-center space-x-2"
              >
                <SaveIcon className="w-4 h-4" />
                <span>Save Changes</span>
              </button>
            </>
          )}
        </div>
      </div>

      {/* User Form */}
      <div className="card">
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Username
              </label>
              <input
                type="text"
                name="username"
                value={formData.username || ''}
                onChange={handleInputChange}
                disabled={!isEditing}
                className="input-field disabled:bg-gray-100 disabled:cursor-not-allowed"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Email
              </label>
              <input
                type="email"
                name="email"
                value={formData.email || ''}
                onChange={handleInputChange}
                disabled={!isEditing}
                className="input-field disabled:bg-gray-100 disabled:cursor-not-allowed"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                First Name
              </label>
              <input
                type="text"
                name="firstName"
                value={formData.firstName || ''}
                onChange={handleInputChange}
                disabled={!isEditing}
                className="input-field disabled:bg-gray-100 disabled:cursor-not-allowed"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Last Name
              </label>
              <input
                type="text"
                name="lastName"
                value={formData.lastName || ''}
                onChange={handleInputChange}
                disabled={!isEditing}
                className="input-field disabled:bg-gray-100 disabled:cursor-not-allowed"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Role
              </label>
              <select
                name="role"
                value={formData.role || ''}
                onChange={handleInputChange}
                disabled={!isEditing}
                className="input-field disabled:bg-gray-100 disabled:cursor-not-allowed"
              >
                <option value="USER">User</option>
                <option value="ADMIN">Admin</option>
                <option value="MODERATOR">Moderator</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Status
              </label>
              <select
                name="isActive"
                value={formData.isActive?.toString() || ''}
                onChange={handleInputChange}
                disabled={!isEditing}
                className="input-field disabled:bg-gray-100 disabled:cursor-not-allowed"
              >
                <option value="true">Active</option>
                <option value="false">Inactive</option>
              </select>
            </div>
          </div>

          {/* Read-only fields */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-6 border-t border-gray-200">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Created At
              </label>
              <input
                type="text"
                value={new Date(user.createdAt).toLocaleString()}
                disabled
                className="input-field disabled:bg-gray-100 disabled:cursor-not-allowed"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Last Updated
              </label>
              <input
                type="text"
                value={new Date(user.updatedAt).toLocaleString()}
                disabled
                className="input-field disabled:bg-gray-100 disabled:cursor-not-allowed"
              />
            </div>
          </div>
        </form>
      </div>

      {/* Activity Log */}
      <div className="card">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Recent Activity</h3>
        <div className="space-y-3">
          <div className="flex items-center space-x-3">
            <div className="w-2 h-2 bg-green-500 rounded-full"></div>
            <div>
              <p className="text-sm font-medium text-gray-900">User profile updated</p>
              <p className="text-sm text-gray-500">2 hours ago</p>
            </div>
          </div>
          <div className="flex items-center space-x-3">
            <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
            <div>
              <p className="text-sm font-medium text-gray-900">User logged in</p>
              <p className="text-sm text-gray-500">1 day ago</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserDetail; 