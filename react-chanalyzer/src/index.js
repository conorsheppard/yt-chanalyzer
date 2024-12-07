import React from "react"
import ReactDOM from "react-dom/client"
import App from "./App"
import HeaderBanner from "./HeaderBanner";
import './App.css';
import './HeaderBanner.css'

const root = ReactDOM.createRoot(document.getElementById("root"))
root.render(
  <React.StrictMode>
    <HeaderBanner /><br /><br /><br />
    <App />
  </React.StrictMode>
)
