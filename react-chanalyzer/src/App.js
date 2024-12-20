import {useState, useRef} from 'react';
import { LineGraph } from "./Line";

const scraperApi = process.env.REACT_APP_ANALYTICS_API;
const ytBaseUrl = "https://www.youtube.com/";

function App() {
  const [graphData, setGraphData] = useState([]);
  const [interval, setInterval] = useState();
  const inputRef = useRef();

  function onSubmit(e) {
    e.preventDefault();
    const value = inputRef.current.value;

    if (value === "") {
      console.log("returning ...")
      return
    }

    const eventSource = new EventSource(`${scraperApi}/channel?channelUrl=${ytBaseUrl}${value}`);
    inputRef.current.value = "";

    eventSource.onmessage = event => {
      console.log("event:")
        console.log(event)
        const eventData = JSON.parse(event.data);
        console.log("eventData:")
        console.log(eventData)
        let graphDataInitialised = initialiseGraph();
        graphDataInitialised["labels"] = eventData["labels"];
        graphDataInitialised["channelName"] = value;
        graphDataInitialised["datasets"][0]["data"] = eventData["datasets"];
        setGraphData(graphDataInitialised);
        setInterval(eventData["currentInterval"]);

        if (eventData["currentInterval"] == 64) {
            console.log("Closing SSE connection");
            eventSource.close();
        }
    }

    return () => eventSource.close();
  };

  return (
      <>
        <div className="search-bar-and-graph">
          <div className="search-bar-form-and-text">
            <form onSubmit={onSubmit}>
              <div className="search-bar-text">Enter the YouTube channel name</div>
              <div className="search-bar-elements">
                <div className="search-bar-prefix-link">https://www.youtube.com/</div>
                <input className="search-bar-input" ref={inputRef} type="text" placeholder="@NASA" />
                <button className="submit-button" type="submit">Submit</button>
              </div>
            </form>
          </div>
          { !isEmpty(graphData) &&
            <div className="line-graph">
              <h3>Videos Uploaded Per Month: <a href={ytBaseUrl + graphData["channelName"]} target="_blank" rel="noreferrer">{graphData["channelName"]}</a></h3>
              <div>{typeof(interval) === 'undefined' ? 0 : interval}/64 videos processed</div>
              <LineGraph data={graphData} />
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
            label: "Number of uploads",
            data: [],
            borderColor: "green"
        }
    ],
    channelName: ""
  };
}

export default App