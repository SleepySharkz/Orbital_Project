import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import type { CreatedCFCEntry } from "../api/cfcApi";

type CFCFlashcardViewerProps = {
  entries: CreatedCFCEntry[];
  title: string;
};

type DeckAnimation = {
  id: number;
  direction: "forward" | "backward";
  outgoingWasAnswer: boolean;
  speed: "normal" | "skip";
};

type CardRenderOptions = {
  entry: CreatedCFCEntry;
  cardPosition: string;
  className: string;
  showAnswer: boolean;
  renderKey?: string;
  isInteractive?: boolean;
  isOverlayStage: boolean;
  onAnimationEnd?: () => void;
};

function isEditableTarget(target: EventTarget | null) {
  if (!(target instanceof HTMLElement)) {
    return false;
  }

  const tagName = target.tagName;
  return (
    target.isContentEditable ||
    tagName === "INPUT" ||
    tagName === "TEXTAREA" ||
    tagName === "SELECT"
  );
}

function getQuestionText(entry: CreatedCFCEntry) {
  const questionText = entry.sourceMaterial.questionText?.trim();

  if (questionText) {
    return {
      label: "Question",
      text: questionText,
    };
  }

  const roughNote = entry.sourceMaterial.roughNote.trim();

  if (roughNote) {
    return {
      label: "Source note",
      text: roughNote,
    };
  }

  return {
    label: "Question",
    text: "No saved question text for this card.",
  };
}

function getEntryAtOffset(
  entries: CreatedCFCEntry[],
  activeIndex: number,
  offset: number,
) {
  const entryCount = entries.length;

  if (entryCount === 0) {
    return null;
  }

  return entries[(activeIndex + offset + entryCount) % entryCount];
}

export function CFCFlashcardViewer({ entries, title }: CFCFlashcardViewerProps) {
  const [activeIndex, setActiveIndex] = useState(0);
  const [isShowingAnswer, setIsShowingAnswer] = useState(false);
  const [isMaximized, setIsMaximized] = useState(false);
  const [deckAnimation, setDeckAnimation] = useState<DeckAnimation | null>(null);
  const deckAnimationRef = useRef<DeckAnimation | null>(null);
  const pendingDirectionsRef = useRef<DeckAnimation["direction"][]>([]);
  const animationIdRef = useRef(0);

  const safeActiveIndex = entries.length === 0 ? 0 : Math.min(activeIndex, entries.length - 1);
  const currentEntry = entries[safeActiveIndex] ?? null;
  const cardCount = entries.length;
  const cardPosition = `${safeActiveIndex + 1}/${cardCount}`;
  const isAnimating = deckAnimation !== null;

  const questionSide = useMemo(
    () => (currentEntry ? getQuestionText(currentEntry) : null),
    [currentEntry],
  );

  const getCardPositionAtOffset = useCallback(
    (offset: number) => {
      if (entries.length === 0) {
        return "0/0";
      }

      const index = (safeActiveIndex + offset + entries.length) % entries.length;
      return `${index + 1}/${entries.length}`;
    },
    [entries.length, safeActiveIndex],
  );

  const finishDeckAnimation = useCallback((animationId?: number) => {
    const currentAnimation = deckAnimationRef.current;

    if (!currentAnimation || entries.length === 0) {
      return;
    }

    if (animationId !== undefined && currentAnimation.id !== animationId) {
      return;
    }

    setActiveIndex((currentIndex) => {
      const normalizedIndex = Math.min(currentIndex, entries.length - 1);

      if (currentAnimation.direction === "forward") {
        return (normalizedIndex + 1) % entries.length;
      }

      return (normalizedIndex - 1 + entries.length) % entries.length;
    });
    setIsShowingAnswer(false);

    const nextDirection = pendingDirectionsRef.current.shift();

    if (nextDirection) {
      const nextAnimation: DeckAnimation = {
        id: animationIdRef.current + 1,
        direction: nextDirection,
        outgoingWasAnswer: false,
        speed: pendingDirectionsRef.current.length > 0 ? "skip" : "normal",
      };
      animationIdRef.current = nextAnimation.id;
      deckAnimationRef.current = nextAnimation;
      setDeckAnimation(nextAnimation);
      return;
    }

    deckAnimationRef.current = null;
    setDeckAnimation(null);
  }, [entries.length]);

  const startDeckAnimation = useCallback(
    (direction: DeckAnimation["direction"]) => {
      if (entries.length < 2) {
        return;
      }

      if (deckAnimationRef.current) {
        pendingDirectionsRef.current.push(direction);
        const skippedAnimation: DeckAnimation = {
          ...deckAnimationRef.current,
          speed: "skip",
        };
        deckAnimationRef.current = skippedAnimation;
        setDeckAnimation(skippedAnimation);
        return;
      }

      const nextAnimation: DeckAnimation = {
        id: animationIdRef.current + 1,
        direction,
        outgoingWasAnswer: isShowingAnswer,
        speed: "normal",
      };
      animationIdRef.current = nextAnimation.id;
      deckAnimationRef.current = nextAnimation;
      setDeckAnimation(nextAnimation);
    },
    [entries.length, isShowingAnswer],
  );

  const goToPreviousCard = useCallback(() => {
    startDeckAnimation("backward");
  }, [startDeckAnimation]);

  const goToNextCard = useCallback(() => {
    startDeckAnimation("forward");
  }, [startDeckAnimation]);

  const toggleCardSide = useCallback(() => {
    if (entries.length === 0 || isAnimating) {
      return;
    }

    setIsShowingAnswer((currentValue) => !currentValue);
  }, [entries.length, isAnimating]);

  useEffect(() => {
    deckAnimationRef.current = deckAnimation;
  }, [deckAnimation]);

  useEffect(() => {
    if (deckAnimation?.speed !== "skip") {
      return undefined;
    }

    const skipAnimationId = deckAnimation.id;
    const skipTimer = window.setTimeout(
      () => finishDeckAnimation(skipAnimationId),
      90,
    );
    return () => window.clearTimeout(skipTimer);
  }, [deckAnimation, finishDeckAnimation]);

  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if (isEditableTarget(event.target)) {
        return;
      }

      if (event.key === "ArrowLeft") {
        event.preventDefault();
        goToPreviousCard();
        return;
      }

      if (event.key === "ArrowRight") {
        event.preventDefault();
        goToNextCard();
        return;
      }

      if (event.key === " " || event.key === "Spacebar") {
        event.preventDefault();
        toggleCardSide();
        return;
      }

      if (event.key === "Escape" && isMaximized) {
        event.preventDefault();
        setIsMaximized(false);
      }
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [goToNextCard, goToPreviousCard, isMaximized, toggleCardSide]);

  if (entries.length === 0 || !currentEntry || !questionSide) {
    return (
      <section className="cfc-flashcard-viewer">
        <p className="cfc-helper-copy">No saved entries for this CFC.</p>
      </section>
    );
  }

  function renderFlashcard({
    entry,
    cardPosition: renderedCardPosition,
    className,
    showAnswer,
    renderKey,
    isInteractive = false,
    isOverlayStage,
    onAnimationEnd,
  }: CardRenderOptions) {
    const renderedQuestionSide = getQuestionText(entry);

    function renderCardFace(label: string, text: string, faceClassName: string) {
      return (
        <div className={faceClassName}>
          <header className="cfc-flashcard-card-header">
            <div>
              <p className="cfc-flashcard-side-label">{label}</p>
              <h2 className="cfc-flashcard-topic">{entry.topic}</h2>
            </div>
            <div className="cfc-flashcard-card-actions">
              <p className="cfc-flashcard-position">{renderedCardPosition}</p>
              {isInteractive && (
                <button
                  className="cfc-flashcard-card-button"
                  type="button"
                  onClick={(event) => {
                    event.stopPropagation();
                    setIsMaximized(!isOverlayStage);
                  }}
                  aria-label={isOverlayStage ? "Minimise flashcard" : "Maximise flashcard"}
                  title={isOverlayStage ? "Minimise" : "Maximise"}
                >
                  {isOverlayStage ? "×" : "⛶"}
                </button>
              )}
            </div>
          </header>

          <div className="cfc-flashcard-copy-shell">
            <p className="cfc-flashcard-copy">{text}</p>
          </div>
        </div>
      );
    }

    return (
      <article
        key={renderKey}
        className={
          showAnswer
            ? `${className} cfc-flashcard-card-answer-visible`
            : className
        }
        role={isInteractive ? "button" : undefined}
        tabIndex={isInteractive ? 0 : undefined}
        onClick={isInteractive ? toggleCardSide : undefined}
        onKeyDown={
          isInteractive
            ? (event) => {
                if (event.key === "Enter") {
                  event.preventDefault();
                  toggleCardSide();
                }
              }
            : undefined
        }
        onAnimationEnd={onAnimationEnd}
        aria-hidden={!isInteractive}
        aria-label={
          isInteractive
            ? showAnswer
              ? "Flashcard learning point side"
              : "Flashcard question side"
            : undefined
        }
      >
        <div className="cfc-flashcard-card-inner">
          {renderCardFace(
            renderedQuestionSide.label,
            renderedQuestionSide.text,
            "cfc-flashcard-face cfc-flashcard-face-question",
          )}
          {renderCardFace(
            "Learning point",
            entry.content.learningPoint,
            "cfc-flashcard-face cfc-flashcard-face-answer",
          )}
        </div>
      </article>
    );
  }

  function renderDeck(isOverlayStage: boolean) {
    const frontEntry =
      deckAnimation?.direction === "forward"
        ? getEntryAtOffset(entries, safeActiveIndex, 1)
        : currentEntry;
    const secondEntry =
      deckAnimation?.direction === "forward"
        ? getEntryAtOffset(entries, safeActiveIndex, 2)
        : getEntryAtOffset(entries, safeActiveIndex, 1);
    const rearEntry =
      deckAnimation?.direction === "forward"
        ? currentEntry
        : getEntryAtOffset(entries, safeActiveIndex, -1);
    const movingEntry =
      deckAnimation?.direction === "backward"
        ? getEntryAtOffset(entries, safeActiveIndex, -1)
        : currentEntry;
    const shouldHandleAnimationEnd = isMaximized ? isOverlayStage : !isOverlayStage;

    return (
      <div
        className={
          isOverlayStage
            ? "cfc-flashcard-deck cfc-flashcard-deck-overlay"
            : "cfc-flashcard-deck"
        }
      >
        {rearEntry &&
          renderFlashcard({
            entry: rearEntry,
            cardPosition:
              deckAnimation?.direction === "forward"
                ? getCardPositionAtOffset(0)
                : getCardPositionAtOffset(-1),
            renderKey:
              deckAnimation?.direction === "forward"
                ? `rear-${deckAnimation.id}-outgoing`
                : `rear-${getCardPositionAtOffset(-1)}`,
            className: "cfc-flashcard-card cfc-flashcard-card-layer cfc-flashcard-card-rear",
            showAnswer: false,
            isOverlayStage,
          })}

        {secondEntry &&
          renderFlashcard({
            entry: secondEntry,
            cardPosition:
              deckAnimation?.direction === "forward"
                ? getCardPositionAtOffset(2)
                : getCardPositionAtOffset(1),
            renderKey:
              deckAnimation?.direction === "forward"
                ? `second-${deckAnimation.id}-${getCardPositionAtOffset(2)}`
                : `second-${getCardPositionAtOffset(1)}`,
            className: "cfc-flashcard-card cfc-flashcard-card-layer cfc-flashcard-card-second",
            showAnswer: false,
            isOverlayStage,
          })}

        {frontEntry &&
          renderFlashcard({
            entry: frontEntry,
            cardPosition:
              deckAnimation?.direction === "forward"
                ? getCardPositionAtOffset(1)
                : getCardPositionAtOffset(0),
            renderKey:
              deckAnimation?.direction === "forward"
                ? `front-${deckAnimation.id}-${getCardPositionAtOffset(1)}`
                : `front-${getCardPositionAtOffset(0)}`,
            className: "cfc-flashcard-card cfc-flashcard-card-layer cfc-flashcard-card-front",
            showAnswer: deckAnimation ? false : isShowingAnswer,
            isInteractive: !deckAnimation,
            isOverlayStage,
          })}

        {deckAnimation &&
          movingEntry &&
          renderFlashcard({
            entry: movingEntry,
            cardPosition:
              deckAnimation.direction === "forward"
                ? getCardPositionAtOffset(0)
                : getCardPositionAtOffset(-1),
            renderKey: `moving-${deckAnimation.id}-${deckAnimation.direction}-${deckAnimation.speed}`,
            className:
              deckAnimation.direction === "forward"
                ? `cfc-flashcard-card cfc-flashcard-card-layer cfc-flashcard-card-moving cfc-flashcard-card-moving-forward ${
                    deckAnimation.speed === "skip" ? "cfc-flashcard-card-moving-skip" : ""
                  }`
                : `cfc-flashcard-card cfc-flashcard-card-layer cfc-flashcard-card-moving cfc-flashcard-card-moving-backward ${
                    deckAnimation.speed === "skip" ? "cfc-flashcard-card-moving-skip" : ""
                  }`,
            showAnswer:
              deckAnimation.direction === "forward"
                ? deckAnimation.outgoingWasAnswer
                : false,
            isOverlayStage,
            onAnimationEnd: shouldHandleAnimationEnd
              ? () => finishDeckAnimation(deckAnimation.id)
              : undefined,
          })}
      </div>
    );
  }

  function renderCardStage(isOverlayStage: boolean) {
    return (
      <div
        className={
          isOverlayStage
            ? "cfc-flashcard-stage cfc-flashcard-stage-overlay"
            : "cfc-flashcard-stage"
        }
      >
        <button
          className="cfc-flashcard-nav cfc-flashcard-nav-left"
          type="button"
          onClick={goToPreviousCard}
          aria-label="Previous flashcard"
          disabled={entries.length < 2}
        >
          {"←"}
        </button>

        {renderDeck(isOverlayStage)}

        <button
          className="cfc-flashcard-nav cfc-flashcard-nav-right"
          type="button"
          onClick={goToNextCard}
          aria-label="Next flashcard"
          disabled={entries.length < 2}
        >
          {"→"}
        </button>
      </div>
    );
  }

  return (
    <section className="cfc-flashcard-viewer" aria-label={`${title} flashcards`}>
      <div className="cfc-flashcard-toolbar">
        <div>
          <p className="cfc-eyebrow">Flashcards</p>
          <h2 className="cfc-flashcard-heading">{title}</h2>
        </div>

        <div className="cfc-flashcard-toolbar-actions">
          <p className="cfc-entry-counter">{cardPosition}</p>
        </div>
      </div>

      {!isMaximized && renderCardStage(false)}

      {isMaximized && (
        <div className="cfc-flashcard-overlay" role="dialog" aria-modal="true">
          <button
            className="cfc-flashcard-overlay-backdrop"
            type="button"
            onClick={() => setIsMaximized(false)}
            aria-label="Close maximised flashcard"
          />
          <div className="cfc-flashcard-overlay-content">
            {renderCardStage(true)}
          </div>
        </div>
      )}
    </section>
  );
}
