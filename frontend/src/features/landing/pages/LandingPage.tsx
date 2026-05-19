import { Link } from "react-router-dom";
import "./../styles/landingStyles.css";

const featureCards = [
  {
    icon: "✦",
    title: "AI-Generated Flashcards",
    description:
      "Upload screenshots and rough notes. AI converts them into revision-ready learning cards.",
  },
  {
    icon: "◎",
    title: "Topic Aggregation",
    description:
      "Automatically organize flashcards by topic so scattered practice becomes structured knowledge.",
  },
  {
    icon: "⌘",
    title: "Mindmap Visualization",
    description:
      "Explore topic connections visually and surface how concepts reinforce one another.",
  },
  {
    icon: "↗",
    title: "Private Sharing",
    description:
      "Share your strongest notes with friends and merge useful insights into your own collection.",
  },
];

export function LandingPage() {
  return (
    <div className="landing-page">
      <header className="landing-nav">
        <Link className="landing-brand" to="/">
          <img
            className="landing-brand-icon"
            src="/favicon.svg"
            alt="MindMesh logo"
          />
          <span>MINDMESH</span>
        </Link>

        <nav className="landing-nav-links" aria-label="Authentication">
          <Link className="landing-nav-link" to="/login">
            Login
          </Link>
          <Link className="landing-nav-button" to="/signup">
            Sign Up
          </Link>
        </nav>
      </header>

      <main className="landing-main">
        <section className="landing-hero">
          <p className="landing-kicker">From rough practice to durable learning</p>
          <h1 className="landing-title">
            Transform Practice Into{" "}
            <span className="landing-title-accent">Permanent Knowledge</span>
          </h1>
          <p className="landing-subtitle">
            Stop losing insights from assignments and tutorials. MINDMESH
            converts your messy notes into structured flashcards, aggregates them
            by topic, and reveals connections across your learning.
          </p>

          <div className="landing-hero-actions">
            <Link className="landing-cta" to="/signup">
              Start Learning Smarter
            </Link>
          </div>
        </section>

        <section className="landing-features" aria-label="Core features">
          {featureCards.map((feature) => (
            <article className="landing-feature-card" key={feature.title}>
              <span className="landing-feature-icon" aria-hidden="true">
                {feature.icon}
              </span>
              <h2 className="landing-feature-title">{feature.title}</h2>
              <p className="landing-feature-description">{feature.description}</p>
            </article>
          ))}
        </section>
      </main>
    </div>
  );
}
