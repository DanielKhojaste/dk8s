import { createBrowserRouter } from 'react-router'

import { AppLayout } from '../components/AppLayout'
import { DashboardPage } from '../pages/DashboardPage'
import { NotFoundPage } from '../pages/NotFoundPage'
import { RegisterServicePage } from '../pages/RegisterServicePage'
import { ServiceDetailPage } from '../pages/ServiceDetailPage'
import { ServicesPage } from '../pages/ServicesPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'services', element: <ServicesPage /> },
      // Listed before :id so "new" isn't treated as a service ID.
      { path: 'services/new', element: <RegisterServicePage /> },
      { path: 'services/:id', element: <ServiceDetailPage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
