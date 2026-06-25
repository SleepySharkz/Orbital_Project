type TFCSharingRequestStatus =
  | "PENDING"
  | "ACCEPTED"
  | "DECLINED"
  | "CANCELLED";

type TFCSharingCompatibilityStatus =
  | "READY"
  | "MISSING_MODULE"
  | "MISSING_TOPIC";

type TFCSharingRequestSummary = {
  id: number;
  senderUserId: number;
  senderUsername: string;
  senderEmail: string;
  recipientUserId: number;
  recipientUsername: string;
  recipientEmail: string;
  status: TFCSharingRequestStatus;
  itemCount: number;
  topics: string[];
  createdAt: string;
};

type TFCSharingRequestEntrySnapshot = {
  id: number;
  sourceEntryId: number;
  flashcardQuestion: string;
  flashcardNoteContent: string;
  questionText: string | null;
  roughNote: string;
  sourceEntryCreatedAt: string | null;
};

type TFCSharingRequestItem = {
  id: number;
  sourceTfcId: number;
  sourceModuleId: number;
  sourceOwnerUsername: string;
  courseCode: string;
  schoolSem: string;
  topic: string;
  sourceWasStaleAtSendTime: boolean;
  sourceUpdatedAt: string | null;
  matchingRecipientModuleId: number | null;
  hasMatchingModule: boolean | null;
  hasMatchingTopic: boolean | null;
  compatibilityStatus: TFCSharingCompatibilityStatus | null;
  blockingReason: string | null;
  entryCount: number;
  entries: TFCSharingRequestEntrySnapshot[];
};

type TFCSharingRequestDetail = {
  id: number;
  senderUserId: number;
  senderUsername: string;
  senderEmail: string;
  recipientUserId: number;
  recipientUsername: string;
  recipientEmail: string;
  status: TFCSharingRequestStatus;
  createdAt: string;
  respondedAt: string | null;
  canAccept: boolean;
  blockingReasons: string[];
  items: TFCSharingRequestItem[];
};

type SharedTFCEntry = {
  id: number;
  sourceEntryId: number;
  flashcardQuestion: string;
  flashcardNoteContent: string;
  questionText: string | null;
  roughNote: string;
  sourceEntryCreatedAt: string | null;
};

type SharedTFCSummary = {
  id: number;
  moduleId: number;
  courseCode: string;
  schoolSem: string;
  topic: string;
  entryCount: number;
  sharedByUserId: number;
  sharedByUsername: string;
  acceptedAt: string;
};

type SharedTFCDetail = {
  id: number;
  moduleId: number;
  courseCode: string;
  schoolSem: string;
  topic: string;
  sharedByUserId: number;
  sharedByUsername: string;
  acceptedAt: string;
  entries: SharedTFCEntry[];
};

export type {
  SharedTFCDetail,
  SharedTFCEntry,
  SharedTFCSummary,
  TFCSharingCompatibilityStatus,
  TFCSharingRequestDetail,
  TFCSharingRequestEntrySnapshot,
  TFCSharingRequestItem,
  TFCSharingRequestStatus,
  TFCSharingRequestSummary,
};
