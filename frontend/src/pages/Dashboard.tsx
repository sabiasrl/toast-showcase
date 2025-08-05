import React from 'react';
import { 
  UsersIcon, 
  ChartBarIcon, 
  ClockIcon, 
  CheckCircleIcon 
} from '@heroicons/react/24/outline';

const Dashboard: React.FC = () => {
  const stats = [
    {
      name: 'Total Users',
      value: '1,234',
      change: '+12%',
      changeType: 'positive',
      icon: UsersIcon,
    },
    {
      name: 'Active Sessions',
      value: '456',
      change: '+5%',
      changeType: 'positive',
      icon: ChartBarIcon,
    },
    {
      name: 'Response Time',
      value: '2.3s',
      change: '-8%',
      changeType: 'negative',
      icon: ClockIcon,
    },
    {
      name: 'Success Rate',
      value: '99.8%',
      change: '+0.2%',
      changeType: 'positive',
      icon: CheckCircleIcon,
    },
  ];

  const recentActivity = [
    {
      id: 1,
      user: 'John Doe',
      action: 'created a new account',
      time: '2 minutes ago',
    },
    {
      id: 2,
      user: 'Jane Smith',
      action: 'updated profile information',
      time: '5 minutes ago',
    },
    {
      id: 3,
      user: 'Bob Johnson',
      action: 'completed a transaction',
      time: '10 minutes ago',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-600 mt-2">Welcome to your Toast application overview</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat) => (
          <div key={stat.name} className="card">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">{stat.name}</p>
                <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
              </div>
              <div className={`p-2 rounded-lg ${
                stat.changeType === 'positive' ? 'bg-green-100' : 'bg-red-100'
              }`}>
                <stat.icon className={`w-6 h-6 ${
                  stat.changeType === 'positive' ? 'text-green-600' : 'text-red-600'
                }`} />
              </div>
            </div>
            <div className="mt-4">
              <span className={`text-sm font-medium ${
                stat.changeType === 'positive' ? 'text-green-600' : 'text-red-600'
              }`}>
                {stat.change}
              </span>
              <span className="text-sm text-gray-600 ml-1">from last month</span>
            </div>
          </div>
        ))}
      </div>

      {/* Recent Activity */}
      <div className="card">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Recent Activity</h2>
        <div className="space-y-4">
          {recentActivity.map((activity) => (
            <div key={activity.id} className="flex items-center space-x-3">
              <div className="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
                <UsersIcon className="w-4 h-4 text-primary-600" />
              </div>
              <div className="flex-1">
                <p className="text-sm font-medium text-gray-900">
                  {activity.user} {activity.action}
                </p>
                <p className="text-sm text-gray-500">{activity.time}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Quick Actions */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="card">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h3>
          <div className="space-y-3">
            <button className="w-full btn-primary">
              Create New User
            </button>
            <button className="w-full btn-secondary">
              View Analytics
            </button>
          </div>
        </div>

        <div className="card">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">System Status</h3>
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-600">API Service</span>
              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                Online
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-600">Database</span>
              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                Online
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-600">Message Queue</span>
              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                Online
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard; 