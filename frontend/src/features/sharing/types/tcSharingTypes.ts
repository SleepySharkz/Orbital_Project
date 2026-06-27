type TCSharingRequestStatus =
  | "PENDING"
  | "ACCEPTED"
  | "DECLINED"
  | "CANCELLED";

type TCSharingCompatibilityStatus =
  | "READY"
  | "MISSING_MODULE"
  | "MISSING_TOPIC";

type TCSharingRequestSummary = {
  id: number;
  senderUserId: number;
  senderUsername: string;
  senderEmail: string;
  recipientUserId: number;
  recipientUsername: string;
  recipientEmail: string;
  status: TCSharingRequestStatus;
  itemCount: number;
  topics: string[];
  createdAt: string;
};

type TCSharingRequestEntrySnapshot = {
  id: number;
  sourceEntryId: number;
  flashcardQuestion: string;
  flashcardNoteContent: string;
  questionText: string | null;
  roughNote: string;
  sourceEntryCreatedAt: string | null;
};

type TCSharingRequestItem = {
  id: number;
  sourceTcId: number;
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
  compatibilityStatus: TCSharingCompatibilityStatus | null;
  blockingReason: string | null;
  entryCount: number;
  entries: TCSharingRequestEntrySnapshot[];
};

type TCSharingRequestDetail = {
  id: number;
  senderUserId: number;
  senderUsername: string;
  senderEmail: string;
  recipientUserId: number;
  recipientUsername: string;
  recipientEmail: string;
  status: TCSharingRequestStatus;
  createdAt: string;
  respondedAt: string | null;
  canAccept: boolean;
  blockingReasons: string[];
  items: TCSharingRequestItem[];
};

type SharedTCEntry = {
  id: number;
  sourceEntryId: number;
  flashcardQuestion: string;
  flashcardNoteContent: string;
  questionText: string | null;
  roughNote: string;
  sourceEntryCreatedAt: string | null;
};

type SharedTCSummary = {
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

type SharedTCDetail = {
  id: number;
  moduleId: number;
  courseCode: string;
  schoolSem: string;
  topic: string;
  sharedByUserId: number;
  sharedByUsername: string;
  acceptedAt: string;
  entries: SharedTCEntry[];
};

export type {
  SharedTCDetail,
  SharedTCEntry,
  SharedTCSummary,
  TCSharingCompatibilityStatus,
  TCSharingRequestDetail,
  TCSharingRequestEntrySnapshot,
  TCSharingRequestItem,
  TCSharingRequestStatus,
  TCSharingRequestSummary,
};
