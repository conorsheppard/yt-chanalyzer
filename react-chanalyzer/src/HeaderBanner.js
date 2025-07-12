import kubes_logo from './logos/kubes-logo.png';
import react_logo from './logos/react-logo.svg';
import spring_boot_logo from './logos/spring-boot-logo.png';
import java_logo from './logos/java-logo.png';
import playwright_logo from './logos/playwright-logo.png';
import eks_logo from './logos/eks-logo.png';
import "./fonts/JetBrainsMono-Medium.ttf";
import "./fonts/JetBrainsMono-ExtraBoldItalic.ttf";

export default function HeaderBanner() {
    return (
        <div className="all-elements">
            <nav className="header-banner">
                <div className='banner-text'>
                    <div><i>YouTube Chanalyzer</i> is a <span className="kubes-text bold-text">Kubernetes</span> <img className="icon-logo" src={kubes_logo} alt="Kubes logo" /> deployed <span className="react-text bold-text">React</span> <img className="icon-logo" src={react_logo} alt="React logo" /> application, 
                    backed by a microservices architecture consisting of a <span className="spring-boot-text bold-text">Spring Boot 3</span> <img className="icon-logo" src={spring_boot_logo} alt="Spring Boot logo" /> (<span className="java-text bold-text">Java 21</span> <img className="icon-logo" src={java_logo} alt="Java logo" />) web service and a <span className="playwright-text bold-text">Playwright</span> <img className="icon-logo" src={playwright_logo} alt="Playwright logo" /> for Java (<span className="java-text bold-text">21</span> <img className="icon-logo" src={java_logo} alt="Java logo" />) web scraper.</div>
                    <div>It is containerised and hosted in an AWS <span className="eks-text bold-text">EKS</span> <img className="icon-logo" src={eks_logo} alt="EKS logo" /> cluster, check out the source code <a className="github-link" href="https://github.com/conorsheppard/yt-chanalyzer" target="_blank" rel="noreferrer">here</a>.</div>
                </div>
            </nav>
        </div>
    )
}