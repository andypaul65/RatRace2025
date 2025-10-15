import React, { useState } from 'react';
import { useMvpClient } from '@ajp/mvp-client';

interface RatRaceFinanceTabProps {
  namespace: string;
}

const RatRaceFinanceTab: React.FC<RatRaceFinanceTabProps> = ({ namespace }) => {
  const { sendMessage, isConnected } = useMvpClient();
  const [scenarioJson, setScenarioJson] = useState('');
  const [status, setStatus] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);

  const loadScenario = async () => {
    if (!scenarioJson.trim()) {
      setStatus('Please enter scenario JSON');
      return;
    }

    setIsLoading(true);
    setStatus('Loading scenario...');

    try {
      const response = await sendMessage(namespace, {
        type: 'load_scenario',
        content: scenarioJson
      });

      if (response.type === 'load_response') {
        setStatus('Scenario loaded successfully');
      } else {
        setStatus(`Error: ${response.content}`);
      }
    } catch (error) {
      setStatus(`Failed to load scenario: ${error}`);
    } finally {
      setIsLoading(false);
    }
  };

  const runSimulation = async () => {
    setIsLoading(true);
    setStatus('Running simulation...');

    try {
      const response = await sendMessage(namespace, {
        type: 'run_simulation',
        content: ''
      });

      if (response.type === 'simulation_response') {
        setStatus('Simulation completed successfully');
      } else {
        setStatus(`Error: ${response.content}`);
      }
    } catch (error) {
      setStatus(`Failed to run simulation: ${error}`);
    } finally {
      setIsLoading(false);
    }
  };

  const getDump = async () => {
    setIsLoading(true);
    setStatus('Generating dump...');

    try {
      const response = await sendMessage(namespace, {
        type: 'get_dump',
        content: ''
      });

      if (response.type === 'dump_response') {
        setStatus(response.content);
      } else {
        setStatus(`Error: ${response.content}`);
      }
    } catch (error) {
      setStatus(`Failed to get dump: ${error}`);
    } finally {
      setIsLoading(false);
    }
  };

  const getSankey = async () => {
    setIsLoading(true);
    setStatus('Generating Sankey data...');

    try {
      const response = await sendMessage(namespace, {
        type: 'get_sankey',
        content: ''
      });

      if (response.type === 'sankey_response') {
        setStatus('Sankey data generated - check console for JSON');
        console.log('Sankey Data:', JSON.parse(response.content));
      } else {
        setStatus(`Error: ${response.content}`);
      }
    } catch (error) {
      setStatus(`Failed to get Sankey data: ${error}`);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
      <h2>RatRace2025 Financial Modeling</h2>

      <div style={{ marginBottom: '20px' }}>
        <strong>Connection Status:</strong> {isConnected ? 'Connected' : 'Disconnected'}
      </div>

      <div style={{ marginBottom: '20px' }}>
        <h3>Load Scenario</h3>
        <textarea
          value={scenarioJson}
          onChange={(e) => setScenarioJson(e.target.value)}
          placeholder="Paste scenario JSON here..."
          rows={10}
          cols={80}
          style={{ width: '100%', fontFamily: 'monospace' }}
        />
        <br />
        <button onClick={loadScenario} disabled={isLoading || !isConnected}>
          Load Scenario
        </button>
      </div>

      <div style={{ marginBottom: '20px' }}>
        <h3>Simulation Controls</h3>
        <button onClick={runSimulation} disabled={isLoading || !isConnected} style={{ marginRight: '10px' }}>
          Run Simulation
        </button>
        <button onClick={getDump} disabled={isLoading || !isConnected} style={{ marginRight: '10px' }}>
          Get Dump
        </button>
        <button onClick={getSankey} disabled={isLoading || !isConnected}>
          Get Sankey Data
        </button>
      </div>

      <div style={{ marginTop: '20px', padding: '10px', backgroundColor: '#f0f0f0', borderRadius: '4px' }}>
        <strong>Status:</strong> {status}
      </div>
    </div>
  );
};

export default RatRaceFinanceTab;