import { useState, useRef } from 'react';
import { BarChart } from "./BarChart";
import FormInput from './FormInput';
import Toggle from 'react-toggle';
import "./Toggle.css";

const scraperApi = process.env.REACT_APP_ANALYTICS_API;
const ytBaseUrl = "https://www.youtube.com/";
const maxVideos = 88;

function App() {
  const [values, setValues] = useState({channelname: ""});
  const showAvgViewsGraph = useRef();
  const channelLinkName = useRef();
  const [avgViewsGraphData, setAvgViewsGraphData] = useState();
  const [mainGraphData, setMainGraphData] = useState();
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
    channelLinkName.current = values["channelname"]
    setPlaceholder(values["channelname"]);
    setLoading(true);
    setProcessingComplete(false);
    let mainGraphData = initMainGraph();
    let avgViewsGraphData = initAvgViewsGraph();
    setGraphData(showAvgViewsGraph.current ? avgViewsGraphData : mainGraphData);

    const eventSource = new EventSource(`${scraperApi}/v1/channel/${ytBaseUrl}${values["channelname"]}/videos`);

    eventSource.onmessage = event => {
      setLoading(false);
      const eventData = JSON.parse(event.data);
      let mainGraphData = initMainGraph();
      let avgViewsGraphData = initAvgViewsGraph();
      mainGraphData["labels"] = eventData["labels"];
      avgViewsGraphData["labels"] = eventData["labels"];
      mainGraphData["datasets"][0]["data"] = eventData["datasets"][0]["data"];
      mainGraphData["datasets"][1]["data"] = eventData["datasets"][1]["data"];
      avgViewsGraphData["datasets"][0]["data"] = eventData["datasets"][2]["data"];
      setMainGraphData(mainGraphData);
      setAvgViewsGraphData(avgViewsGraphData);

      if (showAvgViewsGraph.current) {
        setGraphData(avgViewsGraphData);
      } else {
        setGraphData(mainGraphData);
      }

      setInterval(eventData["currentInterval"]);

      if (eventData["currentInterval"] === maxVideos) {
          eventSource.close();
          setProcessingComplete(true);
      }
    }

    return () => eventSource.close();
  };

  const onChange = (e) => {
    setValues({ ...values, [e.target.name]: e.target.value });
  };

  const onToggle = (e) => {
    showAvgViewsGraph.current = !showAvgViewsGraph.current;
    setGraphData(showAvgViewsGraph.current ? avgViewsGraphData : mainGraphData);
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
              <div className="chart-title-and-toggle">
                <h3>Channel analytics: <a href={ytBaseUrl + values["channelname"]} target="_blank" rel="noreferrer">{channelLinkName.current}</a></h3>
                <div className="toggle">
                    <div className="toggle-text">{showAvgViewsGraph.current ? <span className="medium-text">Show uploads/month & total views</span> : <span className="medium-text">Show average video views</span>}</div>
                    <Toggle className="toggle-button toggle-bg-colour" icons={false} defaultChecked={false} onChange={onToggle} />
                </div>
              </div>
              <div className="processing-indicator medium-text">
                {processingComplete === true && <span>Processing complete. </span>}{typeof(interval) === 'undefined' ? 0 : ' ' + interval} videos processed{processingComplete === true ? <span>.</span> : <span>...</span>}
              </div>
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

function initMainGraph() {
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
        }
    ]
  };
}

function initAvgViewsGraph() {
  return {
    labels: [],
    datasets: [
        {
            backgroundColor: ["rgba(168, 0, 73, 0.4)"],
            hoverBackgroundColor: ["rgba(126, 10, 60, 0.5)"],
            borderColor: ["rgba(87, 255, 182, 0.4)"],
            hoverBorderColor: ["rgba(68, 192, 138, 0.5)"],
            borderWidth: 2,
            label: "Average video views per month",
            data: [],
            hoverBorderWidth: 3,
            borderRadius: 3,
            hoverBorderRadius: 4,
        }
    ]
  };
}

export default App