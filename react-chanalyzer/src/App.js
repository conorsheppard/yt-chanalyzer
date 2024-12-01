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

    {/* MANTA channel ID: UC6C2fzB1RTMY9uga4sATZBg */}
    {/* David Bennett Piano: UCz2iUx-Imr6HgDC3zAFpjOw */}
    {/* NASA: UCTPSb6BE8Qxltodm96M5lew */}
    axios.get("http://localhost:8080/api/channel?channelId=" + value).then(response => {
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
          Enter YouTube Channel ID: <input ref={inputRef} type="text" />
          <button type="submit">Submit</button>
        </form>
      </>
    )
  }

  return (
    <>
      <h3>Graph Data:</h3>
      <LineGraph data={graphData} />
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
