import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Navbar } from '../components/Navbar';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';

describe('Navbar Component', () => {
  it('debe renderizar el logo corporativo de OL Software y subtítulo', () => {
    render(
      <BrowserRouter>
        <AuthProvider>
          <Navbar />
        </AuthProvider>
      </BrowserRouter>
    );

    expect(screen.getByText('OLSoftware')).toBeInTheDocument();
    expect(screen.getByText('Software & Development')).toBeInTheDocument();
    expect(screen.getByText(/Beneficios por renovar/i)).toBeInTheDocument();
  });
});
