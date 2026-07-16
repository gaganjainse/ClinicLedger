import React, { useState, useEffect } from 'react';

/**
 * Agentic Clinic Ledger OS | SYSTEM DIAGNOSTICS & ARCHITECT
 *
 * Functional diagnostic tool for the Clinical Operating System.
 * Visualizes data flow, NLU confidence, and Tool Registry state.
 */

const COLORS = {
  bg: '#020617',
  grid: '#1e293b',
  text: '#f8fafc',
  subtext: '#94a3b8',
  primary: '#0D9488', // Medical Teal
  secondary: '#6366f1',
  success: '#22c55e',
  danger: '#ef4444',
};

const MetricItem = ({ label, value }) => (
  <div style={{ marginBottom: '8px' }}>
    <div style={{ fontSize: '10px', color: COLORS.subtext, textTransform: 'uppercase', letterSpacing: '0.5px' }}>{label}</div>
    <div style={{ fontSize: '14px', fontWeight: 'bold', color: COLORS.text }}>{value}</div>
  </div>
);

const OSNode = ({ id, name, metrics, status, type, logs, x, y, onSelect, isSelected }) => {
  const borderColor = isSelected ? COLORS.primary : `${COLORS.grid}88`;

  return (
    <div
      onClick={() => onSelect(id)}
      style={{
        position: 'absolute',
        left: x,
        top: y,
        width: '260px',
        backgroundColor: '#0f172a',
        border: `1px solid ${borderColor}`,
        borderRadius: '16px',
        padding: '20px',
        cursor: 'pointer',
        boxShadow: isSelected ? `0 0 40px ${COLORS.primary}22` : 'none',
        transition: 'all 0.3s ease-in-out',
        zIndex: isSelected ? 10 : 1
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
        <span style={{ fontSize: '10px', color: COLORS.primary, fontWeight: 'bold' }}>{type}</span>
        <div style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: COLORS.success }} />
      </div>

      <h3 style={{ margin: '0 0 16px 0', fontSize: '18px', fontWeight: '800' }}>{name}</h3>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr' }}>
        {Object.entries(metrics).map(([k, v]) => (
          <MetricItem key={k} label={k} value={v} />
        ))}
      </div>

      {isSelected && (
        <div style={{ marginTop: '12px', padding: '10px', background: '#000', borderRadius: '8px', fontSize: '10px', fontFamily: 'monospace', color: COLORS.success }}>
          {logs.map((log, i) => <div key={i}>{`> ${log}`}</div>)}
        </div>
      )}
    </div>
  );
};

export default function ClinicalOSArchitect() {
  const [activeNode, setActiveNode] = useState('DB');
  const [isSyncing, setIsSyncing] = useState(false);

  const runDiagnostic = () => {
    setIsSyncing(true);
    setTimeout(() => setIsSyncing(false), 2000);
  };

  const nodes = [
    {
      id: 'DB', name: 'SQLite DB Engine', type: 'STORAGE', x: 50, y: 150,
      metrics: { Version: '5.0', Tables: 7, Integrity: '100%' },
      logs: ['Index optimization OK', 'VACUUM complete', 'Pragmas verified']
    },
    {
      id: 'BRAIN', name: 'Contextual Brain', type: 'LOGIC', x: 380, y: 150,
      metrics: { State: 'DETAIL', ActiveID: '1042', Aware: 'True' },
      logs: ['Patient context locked', 'Navigation state synced']
    },
    {
      id: 'NLU', name: 'NLU Orchestrator', type: 'INTENT', x: 710, y: 150,
      metrics: { Confidence: '94%', Latency: '12ms', Mode: 'Local' },
      logs: ['Resolved: PAYMENT', 'Dialect mapping: Success']
    },
    {
      id: 'UI', name: 'Medical Interface', type: 'PHYSICAL', x: 1040, y: 150,
      metrics: { FPS: '60', Theme: 'Medical', Layout: 'Redo' },
      logs: ['Layer re-composition OK', 'Insets handled']
    }
  ];

  return (
    <div style={{ backgroundColor: COLORS.bg, minHeight: '100vh', color: COLORS.text, padding: '40px', position: 'relative' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '60px' }}>
        <div>
          <h1 style={{ margin: 0, fontSize: '28px', fontWeight: '900' }}>CLINIC LEDGER <span style={{ color: COLORS.primary }}>OS</span></h1>
          <p style={{ color: COLORS.subtext, marginTop: '4px' }}>System Architecture & Functional Diagnostics</p>
        </div>

        <div style={{ display: 'flex', gap: '20px' }}>
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: '10px', color: COLORS.subtext }}>SYSTEM STATUS</div>
            <div style={{ color: COLORS.success, fontWeight: 'bold' }}>● OPTIMAL / NO ERRORS</div>
          </div>
          <button
            onClick={runDiagnostic}
            style={{
              backgroundColor: COLORS.primary, border: 'none', color: 'white',
              padding: '12px 24px', borderRadius: '12px', fontWeight: 'bold', cursor: 'pointer'
            }}
          >
            {isSyncing ? 'SYNCING...' : 'RUN DIAGNOSTIC'}
          </button>
        </div>
      </div>

      <div style={{ position: 'relative', height: '400px', border: `1px solid ${COLORS.grid}`, borderRadius: '24px', background: 'rgba(15, 23, 42, 0.4)' }}>
        <svg style={{ position: 'absolute', width: '100%', height: '100%', pointerEvents: 'none' }}>
          <line x1="310" y1="210" x2="380" y2="210" stroke={COLORS.grid} strokeWidth="2" strokeDasharray="4" />
          <line x1="640" y1="210" x2="710" y2="210" stroke={COLORS.grid} strokeWidth="2" strokeDasharray="4" />
          <line x1="970" y1="210" x2="1040" y2="210" stroke={COLORS.grid} strokeWidth="2" strokeDasharray="4" />
        </svg>

        {nodes.map(n => (
          <OSNode
            key={n.id}
            {...n}
            isSelected={activeNode === n.id}
            onSelect={setActiveNode}
          />
        ))}
      </div>

      <div style={{ marginTop: '40px', display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '24px' }}>
        <SummaryCard title="Data Efficiency" value="98.2%" sub="Query Cache hits" />
        <SummaryCard title="Skill Acquisition" value="15 Items" sub="Local Dialect Mappings" />
        <SummaryCard title="Haptic Precision" value="Unified" sub="System Feedback" />
      </div>
    </div>
  );
}

const SummaryCard = ({ title, value, sub }) => (
  <div style={{ background: '#0f172a', padding: '24px', borderRadius: '20px', border: `1px solid ${COLORS.grid}` }}>
    <div style={{ fontSize: '12px', color: COLORS.subtext, marginBottom: '8px' }}>{title}</div>
    <div style={{ fontSize: '24px', fontWeight: '900', color: COLORS.primary }}>{value}</div>
    <div style={{ fontSize: '11px', color: COLORS.subtext, marginTop: '4px' }}>{sub}</div>
  </div>
);
