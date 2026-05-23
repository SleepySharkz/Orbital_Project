export type EntryDraft = {
  topic: string;
  questionText: string;
  roughNote: string;
  files: File[];
};

export function createEmptyEntry(): EntryDraft {
  return {
    topic: "",
    questionText: "",
    roughNote: "",
    files: [],
  };
}
