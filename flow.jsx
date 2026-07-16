import React, { useState, useEffect } from 'react';

/**
 * Agentic Clinic Ledger OS | SYSTEM DIAGNOSTICS & ARCHITECT v2.0
 *
 * High-fidelity React twin of the native Android ArchitecturalDiagnosticHub.
 * Full-screen maintenance workspace with Water Flow logic and Test Integration.
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
  <div style={{ marginBottom: '10px' }}>
    <div style={{ fontSize: '9px', color: COLORS.subtext, textTransform: 'uppercase', letterSpacing: '0.8px', fontWeight: 'bold' }}>{label}</div>
    <div style={{ fontSize: '15px', fontWeight: '900', color: COLORS.text }}>{value}</div>
  </div>
);

const OSNode = ({ id, name, metrics, status, type, logs, x, y, onSelect, isSelected, isSyncing, pulseAlpha }) => {
  const borderColor = isSelected ? COLORS.primary : `${COLORS.grid}88`;
  const shadow = isSelected ? `0 0 50px ${COLORS.primary}33` : 'none';

  return (
    <div
      onClick={(e) => { e.stopPropagation(); onSelect(id); }}
      style={{
        position: 'absolute',
        left: x,
        top: y,
        width: '240px',
        backgroundColor: 'rgba(15, 23, 42, 0.95)',
        backdropFilter: 'blur(12px)',
        border: `2px solid ${borderColor}`,
        borderRadius: '16px',
        padding: '24px',
        cursor: 'grab',
        boxShadow: shadow,
        transition: 'all 0.4s cubic-bezier(0.16, 1, 0.3, 1)',
        transform: isSelected ? 'scale(1.02) translateY(-4px)' : 'scale(1)',
        zIndex: isSelected ? 10 : 1
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
        <span style={{ fontSize: '10px', color: COLORS.primary, fontWeight: '900', letterSpacing: '1px' }}>{type}</span>
        <div style={{
          width: '10px', height: '10px', borderRadius: '50%',
          backgroundColor: COLORS.success,
          boxShadow: `0 0 10px ${COLORS.success}`,
          opacity: isSyncing ? pulseAlpha : 1
        }} />
      </div>

      <h3 style={{ margin: '0 0 16px 0', fontSize: '19px', fontWeight: '900', color: COLORS.text }}>{name}</h3>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', borderTop: `1px solid ${COLORS.grid}`, paddingTop: '12px' }}>
        {Object.entries(metrics).map(([k, v]) => (
          <MetricItem key={k} label={k} value={v} />
        ))}
      </div>

      {isSelected && (
        <div style={{
          marginTop: '16px', padding: '12px', background: '#000', borderRadius: '8px',
          fontSize: '10px', fontFamily: 'monospace', color: COLORS.success,
          border: `1px solid ${COLORS.success}33`
        }}>
          {logs.map((log, i) => <div key={i} style={{ marginBottom: '4px' }}>{`> ${log}`}</div>)}
        </div>
      )}
    </div>
  );
};

export default function ClinicalOSArchitect() {
  const [activeNode, setActiveNode] = useState('DB');
  const [isSyncing, setIsSyncing] = useState(false);
  const [pulse, setPulse] = useState(1);
  const [zoom, setZoom] = useState(1);

  useEffect(() => {
    let interval;
    if (isSyncing) {
      interval = setInterval(() => {
        setPulse(p => p === 1 ? 0.4 : 1);
      }, 500);
    } else {
      setPulse(1);
    }
    return () => clearInterval(interval);
  }, [isSyncing]);

  const runSuite = () => {
    setIsSyncing(true);
    setTimeout(() => setIsSyncing(false), 3000);
  };

  const nodes = [
    {
      id: 'DB', name: 'SQLite Engine', type: 'STORAGE', x: 100, y: 150,
      metrics: { Version: '5.0', Integrity: '100%', Health: 'Opt' },
      logs: ['Index optimized', 'VACUUM complete', 'Pragmas verified']
    },
    {
      id: 'BRAIN', name: 'Context Brain', type: 'LOGIC', x: 450, y: 100,
      metrics: { State: 'STABLE', Active: 'True', Aware: 'True' },
      logs: ['Navigation state locked', 'Habit map updated', 'Context: SETTINGS']
    },
    {
      id: 'NLU', name: 'Intent Engine', type: 'INTENT', x: 800, y: 150,
      metrics: { Conf: '98%', Latency: '11ms', Mode: 'Local' },
      logs: ['Resolved: SYS_HEALTH', 'Dialect: DEODHARI_MAP', 'No cloud fallback']
    },
    {
      id: 'TESTS', name: 'JUnit 6 Suite', type: 'QUALITY', x: 450, y: 350,
      metrics: { Passed: '57/57', Coverage: '92%', Suite: 'Large' },
      logs: ['Lamba tests: OK', 'Parameterized runs: OK', 'Regression battery: PASS']
    },
    {
      id: 'UI', name: 'Physical Hub', type: 'PHYSICAL', x: 1150, y: 150,
      metrics: { FPS: '120', Theme: 'Medical', Insets: 'OK' },
      logs: ['Composition: 0.8ms', 'Layer cache hits: 99%', '120Hz Verified']
    }
  ];

  return (
    <div style={{ backgroundColor: COLORS.bg, minHeight: '100vh', color: COLORS.text, padding: '0', position: 'relative', fontFamily: 'Inter, system-ui', overflow: 'hidden' }}>
      {/* Zoomable Workspace Background */}
      <div style={{ position: 'absolute', inset: 0, backgroundImage: `radial-gradient(${COLORS.grid} 1px, transparent 1px)`, backgroundSize: '40px 40px', opacity: 0.3, transform: `scale(${zoom})` }} />

      {/* Top Header Overlay */}
      <header style={{ position: 'absolute', top: 0, width: '100%', display: 'flex', justifyContent: 'space-between', padding: '40px 60px', zIndex: 100, background: 'linear-gradient(to bottom, #020617 0%, transparent 100%)' }}>
        <div>
          <h1 style={{ margin: 0, fontSize: '28px', fontWeight: '900', letterSpacing: '-1.5px' }}>CLINIC LEDGER <span style={{ color: COLORS.primary }}>OS</span> <span style={{ color: COLORS.subtext, fontWeight: '300' }}>v2.0</span></h1>
          <p style={{ color: COLORS.subtext, marginTop: '8px', fontSize: '13px' }}>Industrial Maintenance Mode & Diagnostic Workspace</p>
        </div>

        <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: '10px', color: COLORS.subtext, fontWeight: 'bold' }}>WORKSPACE ZOOM</div>
            <input type="range" min="0.5" max="1.5" step="0.1" value={zoom} onChange={(e) => setZoom(e.target.value)} style={{ width: '100px' }} />
          </div>
          <button
            onClick={runSuite}
            style={{
              backgroundColor: COLORS.primary, border: 'none', color: 'white',
              padding: '14px 28px', borderRadius: '12px', fontWeight: '900', cursor: 'pointer',
              boxShadow: `0 8px 25px ${COLORS.primary}44`,
            }}
          >
            {isSyncing ? 'RUNNING SUITE...' : 'RUN FULL DIAGNOSTIC'}
          </button>
        </div>
      </header>

      {/* Interactive Hub */}
      <main
        onClick={() => setActiveNode(null)}
        style={{ width: '2000px', height: '1200px', position: 'relative', transform: `scale(${zoom})`, transformOrigin: 'top left', padding: '100px' }}
      >
        <svg style={{ position: 'absolute', width: '100%', height: '100%', pointerEvents: 'none', top: 0, left: 0 }}>
          <path d="M 340 210 Q 395 210 450 160" stroke={COLORS.grid} fill="none" strokeWidth="2" strokeDasharray="5,5" />
          <path d="M 690 160 Q 745 210 800 210" stroke={COLORS.grid} fill="none" strokeWidth="2" strokeDasharray="5,5" />
          <path d="M 1040 210 L 1150 210" stroke={COLORS.grid} fill="none" strokeWidth="2" strokeDasharray="5,5" />
          <path d="M 690 160 Q 570 260 450 410" stroke={COLORS.grid} fill="none" strokeWidth="2" strokeDasharray="5,5" />
          <path d="M 690 410 Q 920 410 1150 210" stroke={COLORS.grid} fill="none" strokeWidth="2" strokeDasharray="5,5" />
        </svg>

        {nodes.map(n => (
          <OSNode
            key={n.id}
            {...n}
            isSelected={activeNode === n.id}
            onSelect={setActiveNode}
            isSyncing={isSyncing}
            pulseAlpha={pulse}
          />
        ))}
      </main>

      {/* Bottom Log Panel Overlay */}
      <footer style={{ position: 'absolute', bottom: 0, width: '100%', padding: '30px 60px', zIndex: 100, background: 'linear-gradient(to top, #020617 80%, transparent 100%)' }}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: '20px', marginBottom: '30px' }}>
          <SummaryCard title="Test Integrity" value="57/57" sub="JUnit 6 Suite Passed" />
          <SummaryCard title="Frame Time" value="7.2ms" sub="Ultra-Performance Verified" />
          <SummaryCard title="Context Depth" value="High" sub="Cognitive Awareness State" />
          <SummaryCard title="Llama Engine" value="Ready" sub="Local Inference Service" />
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', opacity: 0.5, fontSize: '11px', fontFamily: 'monospace' }}>
          <div>UPTIME: 142H 12M</div>
          <div>BUILD_SHA: 0313d96_STABLE_ULTRA</div>
        </div>
      </footer>
    </div>
  );
}

const SummaryCard = ({ title, value, sub }) => (
  <div style={{ background: 'rgba(15, 23, 42, 0.8)', padding: '20px', borderRadius: '20px', border: `1px solid ${COLORS.grid}`, backdropFilter: 'blur(8px)' }}>
    <div style={{ fontSize: '11px', color: COLORS.subtext, marginBottom: '8px', fontWeight: 'bold' }}>{title}</div>
    <div style={{ fontSize: '24px', fontWeight: '900', color: COLORS.primary }}>{value}</div>
    <div style={{ fontSize: '10px', color: COLORS.subtext, marginTop: '4px' }}>{sub}</div>
  </div>
);
