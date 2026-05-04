export const getApiErrorMessage = (error, fallback = 'Request failed. Please try again.') => {
  const data = error?.response?.data;

  if (typeof data === 'string') {
    return data;
  }

  if (data?.error) {
    return data.error;
  }

  if (data?.message) {
    return data.message;
  }

  if (data && typeof data === 'object') {
    return Object.values(data).filter(Boolean).join(', ') || fallback;
  }

  return error?.message || fallback;
};
