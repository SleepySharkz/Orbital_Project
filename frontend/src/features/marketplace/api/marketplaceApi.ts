import type {
  MarketplaceBrowseParams,
  MarketplaceListingDetail,
  MarketplaceListingManagementDetail,
  MarketplaceListingManagementPage,
  MarketplaceListingPage,
  MarketplaceListingPublishResponse,
  PublishMarketplaceListingRequest,
  UpdateMarketplaceListingMetadataRequest,
  MarketplaceUpvoteResponse,
  CreateMarketplaceReportRequest,
  MarketplaceReportResponse,
} from "../types/marketplaceTypes";

type ErrorResponse = {
  message?: string;
  detail?: string;
  error?: string;
  errors?: unknown;
  fieldErrors?: unknown;
};

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

function authHeaders(token: string) {
  return {
    Authorization: `Bearer ${token}`,
  };
}

function jsonHeaders(token: string) {
  return {
    ...authHeaders(token),
    "Content-Type": "application/json",
  };
}

async function parseJson<T>(response: Response): Promise<T> {
  return (await response.json()) as T;
}

async function getErrorMessage(response: Response, fallback: string) {
  try {
    const error = await parseJson<ErrorResponse>(response);
    return extractUsefulError(error) || fallback;
  } catch {
    return fallback;
  }
}

function extractUsefulError(error: ErrorResponse) {
  const validationMessage = extractValidationMessage(error.errors) ||
    extractValidationMessage(error.fieldErrors);

  if (validationMessage) {
    return validationMessage;
  }

  if (isUsefulErrorText(error.detail)) {
    return error.detail;
  }

  if (isUsefulErrorText(error.message)) {
    return error.message;
  }

  if (isUsefulErrorText(error.error)) {
    return error.error;
  }

  return "";
}

function extractValidationMessage(errors: unknown): string {
  if (!errors) {
    return "";
  }

  if (Array.isArray(errors)) {
    return errors
      .map((item) => {
        if (typeof item === "string") {
          return item;
        }

        if (item && typeof item === "object") {
          const record = item as Record<string, unknown>;
          return stringValue(record.defaultMessage) ||
            stringValue(record.message) ||
            stringValue(record.reason);
        }

        return "";
      })
      .filter(Boolean)
      .join(" ");
  }

  if (typeof errors === "object") {
    return Object.values(errors as Record<string, unknown>)
      .flatMap((value) => (Array.isArray(value) ? value : [value]))
      .map((value) => stringValue(value))
      .filter(Boolean)
      .join(" ");
  }

  return stringValue(errors);
}

function stringValue(value: unknown) {
  return typeof value === "string" ? value.trim() : "";
}

function isUsefulErrorText(value?: string) {
  if (!value) {
    return false;
  }

  const normalizedValue = value.trim().toLowerCase();
  return normalizedValue !== "bad request" && normalizedValue !== "error";
}

function buildBrowseQuery(params: MarketplaceBrowseParams) {
  const searchParams = new URLSearchParams();

  addParam(searchParams, "q", params.q);
  addParam(searchParams, "module", params.module);
  addParam(searchParams, "topic", params.topic);
  addParam(searchParams, "tag", params.tag);
  addParam(searchParams, "sort", params.sort);
  addParam(searchParams, "page", params.page?.toString());
  addParam(searchParams, "size", params.size?.toString());

  const query = searchParams.toString();
  return query ? `?${query}` : "";
}

function addParam(searchParams: URLSearchParams, key: string, value?: string) {
  if (value && value.trim()) {
    searchParams.set(key, value.trim());
  }
}

export async function fetchMarketplaceListings(
  params: MarketplaceBrowseParams,
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/marketplace/listings${buildBrowseQuery(params)}`,
    {
      method: "GET",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not load marketplace listings."),
    );
  }

  return parseJson<MarketplaceListingPage>(response);
}

export async function fetchMarketplaceListingDetail(
  listingId: number,
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/marketplace/listings/${listingId}`,
    {
      method: "GET",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not load marketplace listing."),
    );
  }

  return parseJson<MarketplaceListingDetail>(response);
}

export async function addMarketplaceListingUpvote(
  listingId: number,
  token: string,
) {
  const response = await fetch(`${API_BASE_URL}/api/v1/marketplace/listings/${listingId}/upvote`, {
    method: "POST",
    headers: authHeaders(token),
  });

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Couldn't upvote this listing :("),
    );
  }

  return parseJson<MarketplaceUpvoteResponse>(response);
}

export async function removeMarketplaceListingUpvote(
  listingId: number,
  token: string,
) {
  const response = await fetch(`${API_BASE_URL}/api/v1/marketplace/listings/${listingId}/upvote`, {
    method: "DELETE",
    headers: authHeaders(token),
  });

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Couldn't remove your upvote on this listing"),
    );
  }

  return parseJson<MarketplaceUpvoteResponse>(response);
}

export async function reportMarketplaceListing(
  listingId: number,
  request: CreateMarketplaceReportRequest,
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/marketplace/listings/${listingId}/reports`,
    {
      method: "POST",
      headers: jsonHeaders(token),
      body: JSON.stringify(request),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not report this listing."),
    );
  }

  return parseJson<MarketplaceReportResponse>(response);
}

export async function fetchMyMarketplaceListings(token: string) {
  const response = await fetch(`${API_BASE_URL}/api/v1/marketplace/my-listings`, {
    method: "GET",
    headers: authHeaders(token),
  });

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not load your marketplace listings."),
    );
  }

  return parseJson<MarketplaceListingManagementPage>(response);
}

export async function fetchMyMarketplaceListingDetail(
  listingId: number,
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/marketplace/my-listings/${listingId}`,
    {
      method: "GET",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not load this listing."),
    );
  }

  return parseJson<MarketplaceListingManagementDetail>(response);
}

export async function updateMyMarketplaceListingMetadata(
  listingId: number,
  request: UpdateMarketplaceListingMetadataRequest,
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/marketplace/my-listings/${listingId}`,
    {
      method: "PATCH",
      headers: jsonHeaders(token),
      body: JSON.stringify(request),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not update this listing."),
    );
  }

  return parseJson<MarketplaceListingManagementDetail>(response);
}

export async function unlistMyMarketplaceListing(
  listingId: number,
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/marketplace/my-listings/${listingId}/unlist`,
    {
      method: "POST",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not unlist this listing."),
    );
  }

  return parseJson<MarketplaceListingManagementDetail>(response);
}

export async function republishMyMarketplaceListing(
  listingId: number,
  token: string,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/marketplace/my-listings/${listingId}/republish`,
    {
      method: "POST",
      headers: authHeaders(token),
    },
  );

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not update this listing snapshot."),
    );
  }

  return parseJson<MarketplaceListingManagementDetail>(response);
}

export async function publishMarketplaceListing(
  request: PublishMarketplaceListingRequest,
  token: string,
) {
  const response = await fetch(`${API_BASE_URL}/api/v1/marketplace/listings`, {
    method: "POST",
    headers: jsonHeaders(token),
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Could not publish this topic sheet."),
    );
  }

  return parseJson<MarketplaceListingPublishResponse>(response);
}
