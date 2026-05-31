import type { CFCSummary } from "../api/cfcApi";

type CFCCollectionViewMode = "date" | "module" | "topic";

type CFCCollectionItem = {
  summary: CFCSummary;
  topics: string[];
  entryCount: number;
};

export type { CFCCollectionItem, CFCCollectionViewMode };
