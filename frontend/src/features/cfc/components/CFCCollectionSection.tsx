import { CFCCollectionCard } from "./CFCCollectionCard";
import type { CFCCollectionItem } from "./cfcCollectionTypes";

type CFCCollectionSectionProps = {
  title: string;
  items: CFCCollectionItem[];
};

export function CFCCollectionSection({ title, items }: CFCCollectionSectionProps) {
  return (
    <section className="cfc-collection-section">
      <div className="cfc-collection-section-header">
        <h2 className="cfc-collection-section-title">{title}</h2>
        <p className="cfc-entry-counter">{items.length} CFC(s)</p>
      </div>

      <div className="cfc-collection-card-list">
        {items.map((item) => (
          <CFCCollectionCard item={item} key={item.summary.id} />
        ))}
      </div>
    </section>
  );
}
