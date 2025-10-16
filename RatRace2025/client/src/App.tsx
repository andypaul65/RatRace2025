import React from 'react';
import RatRaceFinanceTab from './components/RatRaceFinanceTab';

function App() {
  return (
    <div style={{ fontFamily: 'Arial, sans-serif' }}>
      <h1>RatRace2025 Financial Modeling Platform</h1>
      <RatRaceFinanceTab namespace="ratrace" />
    </div>
  );
}

export default App;