import { useState, useEffect } from "react";

const API_URL = "http://localhost:8080/tasks";

function App() {
  const [tasks, setTasks] = useState([]);
  const [error, setError] = useState(null);

  // starea formularului
  const [type, setType] = useState("");
  const [payload, setPayload] = useState("");

  const loadTasks = async () => {
    try {
      const response = await fetch(API_URL);
      if (!response.ok) throw new Error("Eroare la incarcare");
      const data = await response.json();
      setTasks(data);
      setError(null);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    loadTasks();
  }, []);

  // polling: reincarca lista automat la fiecare 2 secunde
  useEffect(() => {
    const interval = setInterval(() => {
      loadTasks();
    }, 2000);

    return () => clearInterval(interval);   // curatare la demontare
  }, []);

  // trimite un task nou
  const submitTask = async () => {
    try {
      const response = await fetch(API_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ type, payload }),
      });
      if (!response.ok) throw new Error("Eroare la trimitere");
      setType("");
      setPayload("");
      loadTasks();       // reincarca lista dupa submit
    } catch (err) {
      setError(err.message);
    }
  };

  return (
      <div style={{ fontFamily: "sans-serif", maxWidth: 800, margin: "40px auto", padding: "0 20px" }}>
        <h1>Task Queue Dashboard</h1>

        <div style={{ marginBottom: 20, padding: 16, border: "1px solid #ddd", borderRadius: 8 }}>
          <h3 style={{ marginTop: 0 }}>Task nou</h3>
          <input
              placeholder="Tip (ex: email, fail)"
              value={type}
              onChange={(e) => setType(e.target.value)}
              style={{ padding: 8, marginRight: 8 }}
          />
          <input
              placeholder="Payload"
              value={payload}
              onChange={(e) => setPayload(e.target.value)}
              style={{ padding: 8, marginRight: 8 }}
          />
          <button onClick={submitTask} disabled={!type.trim()}>Trimite</button>
        </div>

        <button onClick={loadTasks}>Reîncarcă</button>

        {error && <p style={{ color: "red" }}>{error}</p>}

        <table style={{ width: "100%", borderCollapse: "collapse", marginTop: 20 }}>
          <thead>
          <tr style={{ textAlign: "left", borderBottom: "2px solid #ccc" }}>
            <th style={{ padding: 8 }}>Tip</th>
            <th style={{ padding: 8 }}>Payload</th>
            <th style={{ padding: 8 }}>Status</th>
            <th style={{ padding: 8 }}>Retry</th>
          </tr>
          </thead>
          <tbody>
          {tasks.map((task) => (
              <tr key={task.id} style={{ borderBottom: "1px solid #eee" }}>
                <td style={{ padding: 8 }}>{task.type}</td>
                <td style={{ padding: 8 }}>{task.payload}</td>
                <td style={{ padding: 8 }}>{task.status}</td>
                <td style={{ padding: 8 }}>{task.retryCount}</td>
              </tr>
          ))}
          </tbody>
        </table>

        {tasks.length === 0 && !error && <p>Niciun task încă.</p>}
      </div>
  );
}

export default App;