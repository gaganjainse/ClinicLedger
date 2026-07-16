import React, { useState, useEffect, useRef } from 'react';

/**
 * Agentic Clinic Ledger OS | SYSTEM DIAGNOSTICS & ARCHITECT v3.0
 *
 * High-fidelity React twin of the native v3.0 ArchitecturalDiagnosticHub.
 * Industrial Maintenance Workspace with Water Flow, Draggable Nodes, and Live Console.
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

const OSNode = ({ id, name, metrics, type, logs, x, y, onSelect, isSelected, isSyncing, pulseAlpha, onDrag }) => {
  const borderColor = isSelected ? COLORS.primary : `${COLORS.grid}88`;
  const shadow = isSelected ? `0 0 50px ${COLORS.primary}33` : 'none';

  return (
    <div
      onMouseDown={(e) => onDrag(e, id)}
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
        transition: 'border 0.3s, box-shadow 0.3s',
        transform: isSelected ? 'scale(1.02)' : 'scale(1)',
        zIndex: isSelected ? 10 : 1,
        userSelect: 'none'
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
    </div>
  );
};

export default function ClinicalOSArchitect() {
  const [activeNode, setActiveNode] = useState('DB');
  const [isSyncing, setIsSyncing] = useState(false);
  const [pulse, setPulse] = useState(1);
  const [zoom, setZoom] = useState(1);
  const [pan, setPan] = useState({ x: 0, y: 0 });
  const [isPanning, setIsPanning] = useState(false);
  const [nodes, setNodes] = useState([
    { id: 'DB', name: 'SQLite Engine', type: 'STORAGE', x: 100, y: 300, metrics: { Version: '6.0', Health: 'Opt' }, logs: ['Backup verified', 'Index: READY'] },
    { id: 'BRAIN', name: 'Context Brain', type: 'LOGIC', x: 450, y: 250, metrics: { State: 'STABLE', Active: 'Yes' }, logs: ['Context: SYNCED'] },
    { id: 'NLU', name: 'Intent Engine', type: 'INTENT', x: 800, y: 350, metrics: { Conf: '98%', Lat: '12ms' }, logs: ['Resolved: SYS_DIAG'] },
    { id: 'LLAMA', name: 'Llama Inference', type: 'AGENTIC', x: 800, y: 150, metrics: { RAM: '2.4GB', Mode: 'Local' }, logs: ['Model Loaded: GGUF'] },
    { id: 'TESTS', name: 'JUnit Suite', type: 'QUALITY', x: 450, y: 500, metrics: { Pass: '100%', Total: '100+' }, logs: ['All suites passed'] },
    { id: 'UI', name: 'Medical Hub', type: 'PHYSICAL', x: 1150, y: 300, metrics: { FPS: '120', Insets: 'OK' }, logs: ['Layer cache: HIT'] }
  ]);

  const dragRef = useRef(null);

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

  const handleNodeDrag = (e, id) => {
    e.preventDefault();
    const startX = e.clientX;
    const startY = e.clientY;
    const node = nodes.find(n => n.id === id);
    const startNodeX = node.x;
    const startNodeY = node.y;

    const onMouseMove = (moveE) => {
      const dx = (moveE.clientX - startX) / zoom;
      const dy = (moveE.clientY - startY) / zoom;
      setNodes(prev => prev.map(n => n.id === id ? { ...n, x: startNodeX + dx, y: startNodeY + dy } : n));
    };

    const onMouseUp = () => {
      document.removeEventListener('mousemove', onMouseMove);
      document.removeEventListener('mouseup', onMouseUp);
    };

    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', onMouseUp);
  };

  const handleWorkspacePan = (e) => {
    if (e.button !== 1) return; // Middle mouse for pan
    setIsPanning(true);
    const startX = e.clientX - pan.x;
    const startY = e.clientY - pan.y;

    const onMouseMove = (moveE) => {
      setPan({ x: moveE.clientX - startX, y: moveE.clientY - startY });
    };

    const onMouseUp = () => {
      setIsPanning(false);
      document.removeEventListener('mousemove', onMouseMove);
      document.removeEventListener('mouseup', onMouseUp);
    };

    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', onMouseUp);
  };

  return (
    <div
      style={{ backgroundColor: COLORS.bg, minHeight: '100vh', color: COLORS.text, padding: '0', position: 'relative', fontFamily: 'Inter, system-ui', overflow: 'hidden' }}
      onMouseDown={handleWorkspacePan}
    >
      {/* Dynamic Grid */}
      <div style={{ position: 'absolute', inset: 0, backgroundImage: `radial-gradient(${COLORS.grid} 1px, transparent 1px)`, backgroundSize: '40px 40px', opacity: 0.3, transform: `translate(${pan.x}px, ${pan.y}px) scale(${zoom})`, transformOrigin: '0 0' }} />

      {/* Engineering Header */}
      <header style={{ position: 'absolute', top: 0, width: '100%', display: 'flex', justifyContent: 'space-between', padding: '40px 60px', zIndex: 100, background: 'linear-gradient(to bottom, #020617 0%, transparent 100%)', pointerEvents: 'none' }}>
        <div style={{ pointerEvents: 'auto' }}>
          <h1 style={{ margin: 0, fontSize: '28px', fontWeight: '900', letterSpacing: '-1.5px' }}>CLINIC LEDGER <span style={{ color: COLORS.primary }}>OS</span> <span style={{ color: COLORS.subtext, fontWeight: '300' }}>v3.0</span></h1>
          <p style={{ color: COLORS.subtext, marginTop: '8px', fontSize: '13px' }}>Industrial Maintenance Mode | Full-Screen Engineering Canvas</p>
        </div>

        <div style={{ display: 'flex', gap: '30px', alignItems: 'center', pointerEvents: 'auto' }}>
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: '10px', color: COLORS.subtext, fontWeight: 'bold' }}>ZOOM_LEVEL: {Math.round(zoom * 100)}%</div>
            <input type="range" min="0.5" max="2" step="0.1" value={zoom} onChange={(e) => setZoom(parseFloat(e.target.value))} style={{ accentColor: COLORS.primary }} />
          </div>
          <button
            onClick={() => { setIsSyncing(true); setTimeout(() => setIsSyncing(false), 3000); }}
            style={{ backgroundColor: COLORS.primary, border: 'none', color: 'white', padding: '14px 28px', borderRadius: '12px', fontWeight: '900', cursor: 'pointer', boxShadow: `0 8px 25px ${COLORS.primary}44` }}
          >
            {isSyncing ? 'SUITE_EXECUTING...' : 'RUN_SYSTEM_DIAG'}
          </button>
        </div>
      </header>

      {/* Interactive Workspace */}
      <main style={{ width: '3000px', height: '2000px', position: 'relative', transform: `translate(${pan.x}px, ${pan.y}px) scale(${zoom})`, transformOrigin: '0 0' }}>
        <svg style={{ position: 'absolute', width: '100%', height: '100%', pointerEvents: 'none' }}>
          {/* Water Flow Connections logic here (simplified lines for React view) */}
        </svg>

        {nodes.map(n => (
          <OSNode key={n.id} {...n} isSelected={activeNode === n.id} onSelect={setActiveNode} isSyncing={isSyncing} pulseAlpha={pulse} onDrag={handleNodeDrag} />
        ))}
      </main>

      {/* Engineering Console Overlay */}
      <footer style={{ position: 'absolute', bottom: 0, width: '100%', padding: '30px 60px', zIndex: 100, background: 'linear-gradient(to top, #020617 90%, transparent 100%)' }}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '40px' }}>
          <div>
            <h4 style={{ fontSize: '20px', fontWeight: '900', margin: '0 0 10px 0' }}>{nodes.find(n => n.id === activeNode)?.name || 'System'}</h4>
            <p style={{ color: COLORS.subtext, fontSize: '12px' }}>v3.0 Maintenance Console: SELECT_NODE for component trace.</p>
            <div style={{ display: 'flex', gap: '20px', marginTop: '20px' }}>
              <SummaryCard title="CPU_LOAD" value="12%" />
              <SummaryCard title="MEM_USE" value="1.8GB" />
            </div>
          </div>
          <div style={{ background: '#000', borderRadius: '16px', padding: '24px', border: `1px solid ${COLORS.success}33`, height: '180px', overflowY: 'auto' }}>
            <div style={{ fontFamily: 'monospace', fontSize: '11px', color: COLORS.success }}>
              {`> SYSTEM_INIT_COMPLETE [OK]`} <br />
              {`> 120FPS_PHYSICAL_ENGINE [READY]`} <br />
              {`> LLAMA_INFERENCE_SERVICE [STANDBY]`} <br />
              {`> INTEGRITY_GUARDIAN_ACTIVE [TRUE]`} <br />
              {nodes.find(n => n.id === activeNode)?.logs.map((l, i) => <div key={i}>{`> TRACE: ${l}`}</div>)}
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}

const SummaryCard = ({ title, value }) => (
  <div style={{ background: 'rgba(15, 23, 42, 0.6)', padding: '16px', borderRadius: '16px', border: `1px solid ${COLORS.grid}`, flex: 1 }}>
    <div style={{ fontSize: '9px', color: COLORS.subtext, fontWeight: 'bold' }}>{title}</div>
    <div style={{ fontSize: '20px', fontWeight: '900', color: COLORS.primary }}>{value}</div>
  </div>
);
