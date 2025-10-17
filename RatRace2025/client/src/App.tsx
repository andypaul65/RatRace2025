import React from 'react';
import { TabbedInterface, TabConfig } from '@nednederlander/mvp-client';
import RatRaceFinanceTab from './components/RatRaceFinanceTab';

function App() {
  const tabs: TabConfig[] = [
    {
      namespace: 'ratrace',
      title: 'Financial Modeling',
      component: RatRaceFinanceTab,
      hooks: {
        onTabMount: (namespace) => {
          console.log(`RatRace tab mounted: ${namespace}`);
        },
        onTabUnmount: (namespace) => {
          console.log(`RatRace tab unmounted: ${namespace}`);
        },
      },
      style: {
        padding: '20px',
      },
    },
  ];

  return (
    <div style={{ fontFamily: 'Arial, sans-serif' }}>
      <h1>RatRace2025 Financial Modeling Platform</h1>
      <TabbedInterface tabs={tabs} />
    </div>
  );
}

export default App;