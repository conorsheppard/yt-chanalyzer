import { useRef, useState } from "react"
import axios from 'axios';
import { LineGraph } from "./Line";

function App() {
  const [graphData, setGraphData] = useState([])
  const inputRef = useRef()

  function onSubmit(e) {
    e.preventDefault()
    let graphData = null;
    const value = inputRef.current.value

    if (value === "") {
      console.log("returning ...")
      return
    }

    graphData = {
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

    axios.defaults.headers.get['Content-Type'] = 'application/x-www-form-urlencoded';
    axios.defaults.headers.get['Access-Control-Allow-Origin'] = '*';

    const scraper_api = process.env.REACT_APP_ANALYTICS_API;
    console.log("current env: " + process.env.NODE_ENV);
    console.log("sending request to backend service: " + scraper_api);
    axios.get(scraper_api + "/api/channel?channelUrl=" + value).then(response => {
      graphData["labels"] = response.data["labels"];
      graphData["datasets"][0]["data"] = response.data["datasets"];
      graphData["channelName"] = value.replace("https://www.youtube.com/", "");
      
      setGraphData(graphData)
    });

    inputRef.current.value = ""
  }

    return (
      <>
        <form onSubmit={onSubmit}>
          <div className="search-bar-text">Enter the YouTube channel name</div>
          <input className="search-bar-prefix-link" placeholder="https://www.youtube.com/" /><input className="search-bar-input" ref={inputRef} type="text" placeholder="@NASA" />
          <button className="submit-button" type="submit">Submit</button>
        </form>
        { !isEmpty(graphData) &&
          <div className="line-graph">
            <h3>Videos Uploaded Per Month: <a href={"https://www.youtube.com/" + graphData["channelName"]} target="_blank" rel="noreferrer">{graphData["channelName"]}</a></h3>
            <LineGraph data={graphData} />
          </div>
        }
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
