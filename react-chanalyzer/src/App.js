import {useState, useRef} from 'react';
import { BarChart } from "./BarChart";

const scraperApi = process.env.REACT_APP_ANALYTICS_API;
const ytBaseUrl = "https://www.youtube.com/";

function App() {
  const [graphData, setGraphData] = useState([]);
  const [interval, setInterval] = useState();
  const [processingComplete, setProcessingComplete] = useState(false);
  const inputRef = useRef();

  function onSubmit(e) {
    e.preventDefault();
    setProcessingComplete(false);
    const value = inputRef.current.value;

    if (value === "") {
      console.log("returning ...")
      return
    }

    const eventSource = new EventSource(`${scraperApi}/channel?channelUrl=${ytBaseUrl}${value}`);
    inputRef.current.value = "";

    eventSource.onmessage = event => {
      const eventData = JSON.parse(event.data);
      let graphDataInitialised = initialiseGraph();
      graphDataInitialised["labels"] = eventData["labels"];
      graphDataInitialised["channelName"] = value;
      graphDataInitialised["datasets"][0]["data"] = eventData["datasets"];
      setGraphData(graphDataInitialised);
      setInterval(eventData["currentInterval"]);
      console.log("graphData:");
      console.log(graphData);
      if (eventData["currentInterval"] === 64) {
          console.log("Closing SSE connection");
          eventSource.close();
          setProcessingComplete(true);
      }
    }

    return () => eventSource.close();
  };

  return (
      <>
        <div className="search-bar-and-graph">
          <div className="search-bar-form-and-text">
            <form onSubmit={onSubmit}>
              <div className="search-bar-text">Enter a YouTube channel name</div>
              <div className="search-bar-elements">
                <div className="search-bar-prefix-link">https://www.youtube.com/</div>
                <input className="search-bar-input" ref={inputRef} type="text" placeholder="@NASA" />
                <button className="submit-button" type="submit">Submit</button>
              </div>
            </form>
          </div>
          { !isEmpty(graphData) &&
            <div className="bar-chart">
              <h3>Videos Uploaded Per Month: <a href={ytBaseUrl + graphData["channelName"]} target="_blank" rel="noreferrer">{graphData["channelName"]}</a></h3>
              <div>{processingComplete === true && <span>Processing complete. </span>}{typeof(interval) === 'undefined' ? 0 : interval} videos processed{processingComplete === true ? <span>.</span> : <span> ...</span>}</div>
              <BarChart data={graphData} />
            </div>
          }
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

function initialiseGraph() {
  return {
    labels: [],
    datasets: [
        {
            backgroundColor: ["rgba(99, 255, 180, 0.5)"],
            borderColor: "rgba(200, 38, 13, 0.78)",
            borderWidth: 2,
            label: "Number of uploads",
            data: [],
            borderColor: "green"
        }
    ],
    channelName: ""
  };
}

export default App