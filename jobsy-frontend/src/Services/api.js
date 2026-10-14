
import axios from "axios";

const api = axios.create({
    baseURL: "/api",
    headers: { "Content-Type": "application/json" },
});

const JOBS_CACHE_DURATION = 30_000;
let jobsCache = null;
let jobsCacheTime = 0;
let jobsRequest = null;

export const getCachedJobs = () => jobsCache;

export const invalidateJobsCache = () => {
  jobsCache = null;
  jobsCacheTime = 0;
};

export const createJob = async (jobData) => {
  const response = await api.post("/admin/jobs/Create", jobData);
  invalidateJobsCache();
  return response;
};

export const getAllJobs = () => {
  const cacheIsFresh = jobsCache && Date.now() - jobsCacheTime < JOBS_CACHE_DURATION;
  if (cacheIsFresh) return Promise.resolve({ data: jobsCache });
  if (jobsRequest) return jobsRequest;

  jobsRequest = api.get("/job/GetAllJob")
    .then((response) => {
      jobsCache = response.data;
      jobsCacheTime = Date.now();
      return response;
    })
    .finally(() => {
      jobsRequest = null;
    });

  return jobsRequest;
};

export const extractAndSaveGovernmentJobs = async () => {
  const response = await api.post("/admin/jobs/extract");
  invalidateJobsCache();
  return response;
};

export const getJobById = (id) =>
  api.get(`/job/${id}`);

export const updateJob = (id, jobData) =>
  api.put(`/admin/jobs/${id}`, jobData);

export const deleteJob = (id) =>
  api.delete(`/admin/jobs/${id}`);
export default api;