import { useRef, useState } from "react"
import axios from 'axios';
import { LineGraph } from "./Line";

function App() {
  const [graphData, setGraphData] = useState([])
  const inputRef = useRef()

  function onSubmit(e) {
    e.preventDefault()

    const value = inputRef.current.value

    if (value === "") {
      console.log("returning ...")
      return
    }

    let graphData = {
      labels: [],
      datasets: [
          {
              label: "Video views",
              data: [],
              borderColor: "green"
          }
      ],
    };

    axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
    axios.defaults.headers.get['Access-Control-Allow-Origin'] = '*';

    const scraper_api = process.env.REACT_APP_ANALYTICS_API;
    console.log("current env: " + process.env.NODE_ENV);
    console.log("sending request to backend service: " + scraper_api);
    axios.get(scraper_api + "/api/channel?channelUrl=" + value).then(response => {
      graphData["labels"] = response.data["labels"];
      graphData["datasets"][0]["data"] = response.data["datasets"];
      
      setGraphData(graphData)
    });

    inputRef.current.value = ""
  }

  if (isEmpty(graphData)) {
    return (
      <>
        <form onSubmit={onSubmit}>
          <div className="search-bar-text">Enter a YouTube channel URL</div>
          <input className="search-bar-input" ref={inputRef} type="text" placeholder="https://www.youtube.com/@NASA" />
          <button className="submit-button" type="submit">Submit</button>
        </form>
      </>
    )
  }

  return (
    <>
      <div className="line-graph">
        <h3>Graph Data:</h3>
        <LineGraph data={graphData} />
      </div>
    </>
  )
}

function isEmpty(obj) {
  for (const prop in obj) {
    if (Object.hasOwn(obj, prop)) {
      return false;
    }
  }

  return true;
}

export default App
