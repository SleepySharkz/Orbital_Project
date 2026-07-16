import type {
  MarketplaceBrowseParams,
  MarketplaceListingDetail,
  MarketplaceListingPage,
} from "../types/marketplaceTypes";

type ErrorResponse = {
  message?: string;
  detail?: string;
  error?: string;
};

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

function authHeaders(token: string) {
  return {
    Authorization: `Bearer ${token}`,
  };
}

async function parseJson<T>(response: Response): Promise<T> {
  return (await response.json()) as T;
}

async function getErrorMessage(response: Response, fallback: string) {
  try {
    const error = await parseJson<ErrorResponse>(response);
    return error.message || error.detail || error.error || fallback;
  } catch {
    return fallback;
  }
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
