import type { FormEvent } from "react";
import type { MarketplaceSort } from "../types/marketplaceTypes";

type MarketplaceSearchBarProps = {
  searchText: string;
  courseCode: string;
  topic: string;
  tag: string;
  sort: MarketplaceSort;
  disabled: boolean;
  onSearchTextChange: (value: string) => void;
  onCourseCodeChange: (value: string) => void;
  onTopicChange: (value: string) => void;
  onTagChange: (value: string) => void;
  onSortChange: (value: MarketplaceSort) => void;
  onSubmit: () => void;
  onClear: () => void;
};

export function MarketplaceSearchBar({
  searchText,
  courseCode,
  topic,
  tag,
  sort,
  disabled,
  onSearchTextChange,
  onCourseCodeChange,
  onTopicChange,
  onTagChange,
  onSortChange,
  onSubmit,
  onClear,
}: MarketplaceSearchBarProps) {
  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSubmit();
  }

  return (
    <form className="marketplace-search-panel" onSubmit={handleSubmit}>
      <div className="marketplace-search-row">
        <label className="marketplace-search-main">
          <span className="marketplace-field-label">Search</span>
          <input
            className="marketplace-input marketplace-search-input"
            type="search"
            value={searchText}
            disabled={disabled}
            placeholder="Search public notes by title, course, topic, or keyword..."
            onChange={(event) => onSearchTextChange(event.target.value)}
          />
        </label>

        <label className="marketplace-filter-field">
          <span className="marketplace-field-label">Course</span>
          <input
            className="marketplace-input"
            value={courseCode}
            disabled={disabled}
            placeholder="CS2040"
            onChange={(event) => onCourseCodeChange(event.target.value)}
          />
        </label>

        <label className="marketplace-filter-field">
          <span className="marketplace-field-label">Sort</span>
          <select
            className="marketplace-input"
            value={sort}
            disabled={disabled}
            onChange={(event) => onSortChange(event.target.value as MarketplaceSort)}
          >
            <option value="newest">Newest</option>
            <option value="mostUpvoted">Most upvoted</option>
            <option value="mostImported">Most imported</option>
          </select>
        </label>
      </div>

      <div className="marketplace-filter-row">
        <label className="marketplace-filter-field">
          <span className="marketplace-field-label">Topic</span>
          <input
            className="marketplace-input"
            value={topic}
            disabled={disabled}
            placeholder="Trees"
            onChange={(event) => onTopicChange(event.target.value)}
          />
        </label>

        <label className="marketplace-filter-field">
          <span className="marketplace-field-label">Tag</span>
          <input
            className="marketplace-input"
            value={tag}
            disabled={disabled}
            placeholder="revision"
            onChange={(event) => onTagChange(event.target.value)}
          />
        </label>

        <div className="marketplace-filter-actions">
          <button
            className="marketplace-secondary-button"
            type="button"
            disabled={disabled}
            onClick={onClear}
          >
            Clear
          </button>
          <button
            className="marketplace-primary-button"
            type="submit"
            disabled={disabled}
          >
            Search
          </button>
        </div>
      </div>
    </form>
  );
}
