import { TabbedInterface } from './stubs/mvp-client';
import type { TabConfig } from './stubs/mvp-client';
import RatRaceFinanceTab from './components/RatRaceFinanceTab';

const financeTab: TabConfig = {
  namespace: 'ratrace',
  title: 'Financial Modeling',
  component: RatRaceFinanceTab,
};

function App() {
  const tabs = [financeTab]; // Add more tabs as needed
  return <TabbedInterface tabs={tabs} />;
}

export default App;