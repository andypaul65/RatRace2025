import React, { useState } from 'react';
import { useSystemState } from '@nednederlander/mvp-client';

interface RatRaceFinanceTabProps {
  namespace: string;
}

const RatRaceFinanceTab: React.FC<RatRaceFinanceTabProps> = ({ namespace }: RatRaceFinanceTabProps) => {
  const { sendMessage, loading: isLoading, error: connectionError } = useSystemState(namespace);
  const isConnected = !connectionError;
  const [scenarioJson, setScenarioJson] = useState('');
  const [status, setStatus] = useState<string>('');

  const loadScenario = async () => {
    if (!scenarioJson.trim()) {
      setStatus('Please enter scenario JSON');
      return;
    }

    setStatus('Loading scenario...');

    try {
      const response = await sendMessage({
        type: 'load_scenario',
        content: scenarioJson
      });

      // Parse the JSON response content
      const responseData = JSON.parse(response.content);

      if (responseData.type === 'load_response') {
        setStatus('Scenario loaded successfully');
      } else if (responseData.type === 'error') {
        setStatus(`Error: ${responseData.content}`);
      } else {
        setStatus(`Unexpected response: ${responseData.content}`);
      }
    } catch (error) {
      setStatus(`Failed to load scenario: ${error instanceof Error ? error.message : String(error)}`);
    }
  };

  const runSimulation = async () => {
    setStatus('Running simulation...');

    try {
      const response = await sendMessage({
        type: 'run_simulation',
        content: ''
      });

      // Parse the JSON response content
      const responseData = JSON.parse(response.content);

      if (responseData.type === 'simulation_response') {
        setStatus('Simulation completed successfully');
      } else if (responseData.type === 'error') {
        setStatus(`Error: ${responseData.content}`);
      } else {
        setStatus(`Unexpected response: ${responseData.content}`);
      }
    } catch (error) {
      setStatus(`Failed to run simulation: ${error instanceof Error ? error.message : String(error)}`);
    }
  };

  const getDump = async () => {
    setStatus('Generating dump...');

    try {
      const response = await sendMessage({
        type: 'get_dump',
        content: ''
      });

      // Parse the JSON response content
      const responseData = JSON.parse(response.content);

      if (responseData.type === 'dump_response') {
        setStatus(responseData.content);
      } else if (responseData.type === 'error') {
        setStatus(`Error: ${responseData.content}`);
      } else {
        setStatus(`Unexpected response: ${responseData.content}`);
      }
    } catch (error) {
      setStatus(`Failed to get dump: ${error instanceof Error ? error.message : String(error)}`);
    }
  };

  const getSankey = async () => {
    setStatus('Generating Sankey data...');

    try {
      const response = await sendMessage({
        type: 'get_sankey',
        content: ''
      });

      // Parse the JSON response content
      const responseData = JSON.parse(response.content);

      if (responseData.type === 'sankey_response') {
        setStatus('Sankey data generated - check console for JSON');
        console.log('Sankey Data:', JSON.parse(responseData.content));
      } else if (responseData.type === 'error') {
        setStatus(`Error: ${responseData.content}`);
      } else {
        setStatus(`Unexpected response: ${responseData.content}`);
      }
    } catch (error) {
      setStatus(`Failed to get Sankey data: ${error instanceof Error ? error.message : String(error)}`);
    }
  };

  return (
    <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
      <h2>RatRace2025 Financial Modeling</h2>

      <div style={{ marginBottom: '20px' }}>
        <strong>Connection Status:</strong> {isConnected ? 'Connected' : `Disconnected (${connectionError})`}
      </div>

      <div style={{ marginBottom: '20px' }}>
        <h3>Load Scenario</h3>
        <textarea
          value={scenarioJson}
          onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setScenarioJson(e.target.value)}
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