import React from 'react';
import { TabbedInterface, TabConfig } from '@ajp/mvp-client';
import RatRaceFinanceTab from './components/RatRaceFinanceTab';

const financeTab: TabConfig = {
  namespace: 'ratrace-finance',
  title: 'Financial Modeling',
  component: RatRaceFinanceTab,
};

function App() {
  const tabs = [financeTab]; // Add more tabs as needed
  return <TabbedInterface tabs={tabs} />;
}

export default App;