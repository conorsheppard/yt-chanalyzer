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

    console.log("sending request to backend service ...")
    axios.get("http://k8s-ytchanal-ingressy-8a0d456b8d-995833085.eu-west-2.elb.amazonaws.com/api/channel?channelId=" + value).then(response => {
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
          <div className="search-bar-text">Enter the YouTube channel ID</div>
          <input className="search-bar-input" ref={inputRef} type="text" placeholder="UCz2iUx-Imr6HgDC3zAFpjOw" />
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
