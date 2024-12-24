import { useState } from 'react';
import { BarChart } from "./BarChart";
import FormInput from './FormInput';

const scraperApi = process.env.REACT_APP_ANALYTICS_API;
const ytBaseUrl = "https://www.youtube.com/";
const maxVideos = 88;

function App() {
  const [values, setValues] = useState({channelname: ""});
  const [graphData, setGraphData] = useState([]);
  const [interval, setInterval] = useState();
  const [processingComplete, setProcessingComplete] = useState(false);
  const [placeholder, setPlaceholder] = useState("@NASA");
  const [loading, setLoading] = useState(false);
  const inputs = [
    {
      id: 1,
      name: "channelname",
      type: "text",
      errormessage: "Channel name should start with an '@' symbol and be a real YouTube channel with at least 1 video.",
      pattern: "(?:^|[^w])(?:@)([A-Za-z0-9_](?:(?:[A-Za-z0-9_]|(?:.(?!.))){0,28}(?:[A-Za-z0-9_]))?)",
      required: true,
    }
  ];

  function onSubmit(e) {
    e.preventDefault();
    setPlaceholder(values["channelname"]);
    setLoading(true);
    setProcessingComplete(false);
    let gData = initialiseGraph();
    setGraphData(gData);
    const eventSource = new EventSource(`${scraperApi}/channel?channelUrl=${ytBaseUrl}${values["channelname"]}`);

    eventSource.onmessage = event => {
      setLoading(false);
      const eventData = JSON.parse(event.data);
      let gData = initialiseGraph();
      gData["labels"] = eventData["labels"];
      gData["channelName"] = values["channelname"];
      gData["datasets"][0]["data"] = eventData["datasets"][0]["data"];
      gData["datasets"][1]["data"] = eventData["datasets"][1]["data"];
      setGraphData(gData);
      setInterval(eventData["currentInterval"]);

      if (eventData["currentInterval"] === maxVideos) {
          console.log("Closing SSE connection");
          eventSource.close();
          setProcessingComplete(true);
      }
    }

    return () => eventSource.close();
  };

  const onChange = (e) => {
    setValues({ ...values, [e.target.name]: e.target.value });
  };

  return (
      <>
        <div className="search-bar-and-graph">
        {inputs.map((input) => (
          <FormInput
            key={input.id}
            {...input}
            value={values[input.name]}
            onChange={onChange}
            onSubmit={onSubmit}
            loading={loading}
            placeholder={placeholder}
            onInvalid={e => e.target.setCustomValidity(input.errormessage)}
            onInput={e => e.target.setCustomValidity('')}
          />
        ))}
          { !isEmpty(graphData) &&
            <div className="bar-chart">
              <h3>Videos Uploaded Per Month: <a href={ytBaseUrl + graphData["channelName"]} target="_blank" rel="noreferrer">{graphData["channelName"]}</a></h3>
              <div>{processingComplete === true && <span>Processing complete. </span>}{typeof(interval) === 'undefined' ? 0 : ' ' + interval} videos processed{processingComplete === true ? <span>.</span> : <span> ...</span>}</div>
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
            backgroundColor: ["rgba(28, 183, 226, 0.4)"],
            hoverBackgroundColor: ["rgba(27, 149, 183, 0.5)"],
            borderColor: ["rgba(233, 160, 52, 0.4)"],
            hoverBorderColor: ["rgba(200, 138, 45, 0.5)"],
            borderWidth: 2,
            label: "Number of uploads",
            data: [],
            hoverBorderWidth: 3,
            borderRadius: 3,
            hoverBorderRadius: 4,
        },
        {
            backgroundColor: ["rgba(28, 226, 48, 0.4)"],
            hoverBackgroundColor: ["rgba(54, 199, 68, 0.5)"],
            borderColor: ["rgba(226, 38, 28, 0.4)"],
            hoverBorderColor: ["rgba(181, 54, 47, 0.5)"],
            borderWidth: 2,
            label: "Total views/month (millions)",
            data: [],
            hoverBorderWidth: 3,
            borderRadius: 3,
            hoverBorderRadius: 4,
        },
    ],
    channelName: ""
  };
}

export default App