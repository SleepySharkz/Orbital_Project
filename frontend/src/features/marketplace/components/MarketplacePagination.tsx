type MarketplacePaginationProps = {
  page: number;
  totalPages: number;
  totalItems: number;
  hasNext: boolean;
  disabled: boolean;
  onPageChange: (page: number) => void;
};

export function MarketplacePagination({
  page,
  totalPages,
  totalItems,
  hasNext,
  disabled,
  onPageChange,
}: MarketplacePaginationProps) {
  if (totalItems === 0) {
    return null;
  }

  return (
    <div className="marketplace-pagination">
      <p>
        Page {page + 1} of {Math.max(totalPages, 1)} · {totalItems} listings
      </p>

      <div className="marketplace-pagination-actions">
        <button
          className="marketplace-secondary-button"
          type="button"
          disabled={disabled || page === 0}
          onClick={() => onPageChange(page - 1)}
        >
          Previous
        </button>
        <button
          className="marketplace-secondary-button"
          type="button"
          disabled={disabled || !hasNext}
          onClick={() => onPageChange(page + 1)}
        >
          Next
        </button>
      </div>
    </div>
  );
}
