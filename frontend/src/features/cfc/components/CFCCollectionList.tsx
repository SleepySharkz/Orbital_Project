import { CFCCollectionSection } from "./CFCCollectionSection";
import type { CFCCollectionItem, CFCCollectionViewMode } from "./cfcCollectionTypes";

type CFCCollectionListProps = {
  items: CFCCollectionItem[];
  viewMode: CFCCollectionViewMode;
};

function compareNewestFirst(left: CFCCollectionItem, right: CFCCollectionItem) {
  return new Date(right.summary.createdAt).getTime() - new Date(left.summary.createdAt).getTime();
}

function groupByModule(items: CFCCollectionItem[]) {
  const groups = new Map<string, CFCCollectionItem[]>();

  for (const item of items) {
    const key = `${item.summary.courseCode} - ${item.summary.schoolSem}`;
    groups.set(key, [...(groups.get(key) ?? []), item]);
  }

  return Array.from(groups.entries())
    .sort(([left], [right]) => left.localeCompare(right))
    .map(([title, groupItems]) => ({
      title,
      items: [...groupItems].sort(compareNewestFirst),
    }));
}

function groupByTopic(items: CFCCollectionItem[]) {
  const groups = new Map<string, CFCCollectionItem[]>();

  for (const item of items) {
    const topics = item.topics.length > 0 ? item.topics : ["No topic saved"];

    for (const topic of topics) {
      groups.set(topic, [...(groups.get(topic) ?? []), item]);
    }
  }

  return Array.from(groups.entries())
    .sort(([left], [right]) => left.localeCompare(right))
    .map(([title, groupItems]) => ({
      title,
      items: [...groupItems].sort(compareNewestFirst),
    }));
}

export function CFCCollectionList({ items, viewMode }: CFCCollectionListProps) {
  if (items.length === 0) {
    return (
      <section className="cfc-panel">
        <p className="cfc-helper-copy">No saved CFCs yet.</p>
      </section>
    );
  }

  if (viewMode === "module") {
    return (
      <div className="cfc-collection-sections">
        {groupByModule(items).map((group) => (
          <CFCCollectionSection items={group.items} key={group.title} title={group.title} />
        ))}
      </div>
    );
  }

  if (viewMode === "topic") {
    return (
      <div className="cfc-collection-sections">
        {groupByTopic(items).map((group) => (
          <CFCCollectionSection items={group.items} key={group.title} title={group.title} />
        ))}
      </div>
    );
  }

  return (
    <div className="cfc-collection-sections">
      <CFCCollectionSection
        items={[...items].sort(compareNewestFirst)}
        title="Recently Created"
      />
    </div>
  );
}
