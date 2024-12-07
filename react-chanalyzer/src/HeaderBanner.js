import kubes_logo from './kubes-logo.png';
import "./fonts/JetBrainsMono-Medium.ttf";

export default function HeaderBanner() {
    return <nav className="header-banner">
        <div>This is a React ⚛ web application, backed by a microservices architecture consisting of a Spring Boot 3 🍃 (Java 23 ☕️) server and Python 3.12 🐍 web scraper.</div>
        <div>It's containerised and hosted on AWS EKS in a <span class="kubes-text">Kubernetes</span> <img class="kubes-logo" src={kubes_logo} alt="Kubes logo" /> cluster, check out the source code <a href="https://github.com/conorsheppard/yt-chanalyzer" target="_blank" rel="noreferrer">here</a>.</div>
    </nav>
}