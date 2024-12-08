import kubes_logo from './logos/kubes-logo.png';
import react_logo from './logos/react-logo.svg';
import "./fonts/JetBrainsMono-Medium.ttf";
import "./fonts/JetBrainsMono-ExtraBoldItalic.ttf";

export default function HeaderBanner() {
    return <nav className="header-banner">
        <div>This is a <span className="react-text">React</span> <img className="react-logo" src={react_logo} alt="React logo" /> web application, backed by a microservices architecture consisting of a Spring Boot 3 🍃 (Java 23 ☕️) server and Python 3.12 🐍 web scraper.</div>
        <div>It's containerised and hosted on AWS EKS in a <span className="kubes-text">Kubernetes</span> <img className="kubes-logo" src={kubes_logo} alt="Kubes logo" /> cluster, check out the source code <a className="github-link" href="https://github.com/conorsheppard/yt-chanalyzer" target="_blank" rel="noreferrer">here</a>.</div>
    </nav>
}