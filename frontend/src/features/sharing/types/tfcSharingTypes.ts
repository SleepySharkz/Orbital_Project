type TFCSharingRequestStatus =
  | "PENDING"
  | "ACCEPTED"
  | "DECLINED"
  | "CANCELLED";

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
  items: TFCSharingRequestItem[];
};

export type {
  TFCSharingRequestDetail,
  TFCSharingRequestEntrySnapshot,
  TFCSharingRequestItem,
  TFCSharingRequestStatus,
  TFCSharingRequestSummary,
};
