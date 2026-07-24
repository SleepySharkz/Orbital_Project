import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/context/useAuth";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import "../../modules/styles/modulesStyles.css";
import "../styles/helpStyles.css";

const WORKFLOW_STEPS = [
  {
    title: "Create a module",
    description:
      "Add your course code, semester, and main topics. These topics organise everything that follows.",
    links: [{ label: "Go to Modules", route: "/modules" }],
  },
  {
    title: "Create Coursework Flashcards",
    description:
      "Choose a module and coursework source, then add your rough questions, notes, and optional screenshots.",
    links: [{ label: "Create a CFC", route: "/cfcs" }],
  },
  {
    title: "Review your CFCs",
    description:
      "Open the generated flashcard set and check the structured learning points created from your coursework.",
    links: [{ label: "View My CFCs", route: "/my-cfcs" }],
  },
  {
    title: "Revise by topic",
    description:
      "MINDMESH automatically groups CFC entries with the same topic into Topical Cheatsheets for focused revision.",
    links: [{ label: "View Topical Cheatsheets", route: "/topic-sheets" }],
  },
  {
    title: "Discover connections",
    description:
      "Open a module mindmap, select two topic nodes, and discover an insight explaining how they relate.",
    links: [{ label: "Open Mindmap", route: "/mindmap" }],
  },
  {
    title: "Learn together (Optional)",
    description:
      "Once your cheatsheets are ready, share them with friends or explore revision material in the marketplace.",
    links: [
      { label: "Share your TCs", route: "/sharing" },
      { label: "Open Marketplace", route: "/marketplace" },
    ],
  },
];

export function HelpPage() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  if (!user) {
    return null;
  }

  return (
    <div className="modules-page">
      <ModulesSidebar user={user} onLogout={handleLogout} />

      <main className="modules-main help-main">
        <header className="modules-header help-header">
          <h1 className="modules-title">Your MINDMESH workflow</h1>
          <p className="modules-subtitle help-subtitle">
            Follow these steps in order to turn coursework into connected revision
            material.
          </p>
        </header>

        <ol className="help-workflow">
          {WORKFLOW_STEPS.map((step, index) => (
            <li className="help-step" key={step.title}>
              <span className="help-step-number" aria-hidden="true">
                {index + 1}
              </span>

              <article className="help-step-card">
                <h2>{step.title}</h2>
                <p>{step.description}</p>
                <div className="help-step-links">
                  {step.links.map((link) => (
                    <Link
                      className="help-step-link"
                      key={link.route}
                      to={link.route}
                    >
                      {link.label}
                      <span aria-hidden="true">→</span>
                    </Link>
                  ))}
                </div>
              </article>
            </li>
          ))}
        </ol>
      </main>
    </div>
  );
}
