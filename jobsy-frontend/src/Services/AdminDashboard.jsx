import { useState } from "react";
import { createJob, extractAndSaveGovernmentJobs } from "./api.js";

function AdminDashboard() {
  const [job, setJob] = useState({
    title: "",
    department: "",
    description: "",
    applicationStart: "",
    applicationEnd: "",
    officialUrl: "",
    qualification: "",
    requiredStream: "",
    minimumPercentage: "",
    minAge: "",
    maxAge: "",
    ageAsOfDate: "",
    categoryAgeLimits: "",
    caste: "",
    location: "",
    requiredNationality: "",
    minimumExperienceYears: "",
    requiredCertificate: "",
    minimumHeightCm: "",
    maximumHeightCm: "",
    minimumChestCm: "",
    maximumChestCm: "",
    requiredFitnessStatus: "",
    otherConditions: "",
    validCategoryCertificateRequired: "",
    minimumTenthPercentage: "",
    minimumHscPercentage: "",
    disabilityRequired: "",
  });

  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [extracting, setExtracting] = useState(false);
  const [extractError, setExtractError] = useState("");
  const [extractedCount, setExtractedCount] = useState(null);

  const handleChange = (e) => {
    setJob({ ...job, [e.target.name]: e.target.value });
  };

  const handleExtractJobs = async () => {
    setExtracting(true);
    setExtractError("");
    setExtractedCount(null);
    try {
      const { data } = await extractAndSaveGovernmentJobs();
      if (!Array.isArray(data)) {
        throw new Error("The extraction service returned an unexpected response.");
      }
      setExtractedCount(data.length);
    } catch (err) {
      setExtractError(
        err.response?.data?.message
        || err.response?.data?.detail
        || (err.code === "ERR_NETWORK"
          ? "Could not connect to the backend. Check that it is running on port 8081."
          : err.message || "Could not extract and save government jobs.")
      );
    } finally {
      setExtracting(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setResult(null);

    try {
      let categoryAgeLimits = {};
      if (job.categoryAgeLimits.trim()) {
        categoryAgeLimits = JSON.parse(job.categoryAgeLimits);
        if (
          !categoryAgeLimits
          || Array.isArray(categoryAgeLimits)
          || typeof categoryAgeLimits !== "object"
          || Object.entries(categoryAgeLimits).some(
            ([category, age]) => !category.trim()
              || typeof age !== "number"
              || !Number.isInteger(age)
              || age < 0
          )
        ) {
          throw new Error("Enter category age limits as a JSON object with non-negative whole-number ages.");
        }
        categoryAgeLimits = Object.fromEntries(
          Object.entries(categoryAgeLimits).map(([category, age]) => [category, Number(age)])
        );
      }
      const payload = {
        ...job,
        categoryAgeLimits,
        ageAsOfDate: job.ageAsOfDate || null,
        minAge: job.minAge === "" ? null : Number(job.minAge),
        maxAge: job.maxAge === "" ? null : Number(job.maxAge),
        minimumPercentage: job.minimumPercentage === ""
          ? null
          : Number(job.minimumPercentage),
        minimumExperienceYears: job.minimumExperienceYears === ""
          ? null
          : Number(job.minimumExperienceYears),
        minimumHeightCm: job.minimumHeightCm === ""
          ? null
          : Number(job.minimumHeightCm),
        maximumHeightCm: job.maximumHeightCm === ""
          ? null
          : Number(job.maximumHeightCm),
        minimumChestCm: job.minimumChestCm === ""
          ? null
          : Number(job.minimumChestCm),
        maximumChestCm: job.maximumChestCm === ""
          ? null
          : Number(job.maximumChestCm),
        minimumTenthPercentage: job.minimumTenthPercentage === ""
          ? null
          : Number(job.minimumTenthPercentage),
        minimumHscPercentage: job.minimumHscPercentage === ""
          ? null
          : Number(job.minimumHscPercentage),
        disabilityRequired: job.disabilityRequired === ""
          ? null
          : job.disabilityRequired === "true",
        validCategoryCertificateRequired: job.validCategoryCertificateRequired === ""
          ? null
          : job.validCategoryCertificateRequired === "true",
      };

      const response = await createJob(payload);
      setResult(response.data);
    } catch (err) {
      const responseData = err.response?.data;
      const validationErrors = responseData?.errors;
      const validationMessage = validationErrors
        && typeof validationErrors === "object"
        ? Object.values(validationErrors).flat().join(" ")
        : "";

      setError(
        err.message === "Enter category age limits as a JSON object with non-negative whole-number ages."
          ? err.message
          :
        responseData?.message
        || responseData?.detail
        || validationMessage
        || (err.code === "ERR_NETWORK"
          ? "Could not connect to the backend. Check that it is running on port 8081."
          : responseData?.error
            || err.message
            || "Unable to create job.")
      );
    } finally {
      setLoading(false);
    }
  };

  const fields = [
    ["title", "Job Title"],
    ["department", "Department"],
    ["description", "Description"],
    ["applicationStart", "Application Start Date"],
    ["applicationEnd", "Application End Date"],
    ["officialUrl", "Official URL"],
    ["qualification", "Qualification"],
    ["requiredStream", "Required Stream / Specialization"],
    ["minimumPercentage", "Minimum Percentage (for required qualification)"],
    ["minAge", "Minimum Age"],
    ["maxAge", "Maximum Age"],
    ["ageAsOfDate", "Calculate Age As Of Date"],
    ["caste", "Caste (leave blank for all)"],
    ["location", "Location (leave blank for all)"],
    ["requiredNationality", "Required Nationality"],
    ["minimumExperienceYears", "Minimum Experience (years)"],
    ["requiredCertificate", "Required Certificate / Licence"],
    ["minimumHeightCm", "Minimum Height (cm)"],
    ["maximumHeightCm", "Maximum Height (cm)"],
    ["minimumChestCm", "Minimum Chest Measurement (cm)"],
    ["maximumChestCm", "Maximum Chest Measurement (cm)"],
    ["requiredFitnessStatus", "Required Physical Fitness Status"],
    ["otherConditions", "Other Mandatory Conditions"],
    ["minimumTenthPercentage", "Minimum 10th / SSC percentage"],
    ["minimumHscPercentage", "Minimum 12th / HSC percentage"],
  ];

  return (
    <main className="admin-layout">
      <span className="eyebrow">JOB MANAGEMENT</span>
      <h1>Admin dashboard</h1>
      <p className="admin-intro">
        Enter a government job and its eligibility requirements manually.
        Users who match the requirements can receive an email notification.
      </p>

      <section className="admin-eligibility" aria-labelledby="admin-extract-heading">
        <h2 id="admin-extract-heading">Import official vacancies</h2>
        <p className="admin-help">
          Extract jobs from configured official sources and save new listings.
          Saved jobs are available to users without another extraction. Existing jobs are skipped.
        </p>
        <button type="button" onClick={handleExtractJobs} disabled={extracting}>
          {extracting ? "Extracting and saving..." : "Extract and save jobs"}
        </button>
        {extracting && (
          <p role="status">Checking configured government sources. This may take a little while...</p>
        )}
        {extractError && <p className="alert error" role="alert">{extractError}</p>}
        {extractedCount !== null && (
          <p role="status">
            Extraction complete. Saved {extractedCount} new job{extractedCount === 1 ? "" : "s"}.
          </p>
        )}
      </section>

      <form className="admin-form" onSubmit={handleSubmit}>
        <h2 className="admin-section-title">Job details</h2>
        {fields.map(([name, label]) => (
          <div className={`admin-field admin-field-${name}`} key={name}>
            <label htmlFor={`job-${name}`}>
              {label}
              {[
                "caste",
                "location",
                "requiredStream",
                "minimumPercentage",
                "minAge",
                "maxAge",
                "ageAsOfDate",
                "requiredNationality",
                "minimumExperienceYears",
                "requiredCertificate",
                "minimumHeightCm",
                "maximumHeightCm",
                "minimumChestCm",
                "maximumChestCm",
                "requiredFitnessStatus",
                "otherConditions",
                "minimumTenthPercentage",
                "minimumHscPercentage",
              ].includes(name) && (
                <span className="admin-optional"> (optional)</span>
              )}
            </label>
            {name === "description" || name === "otherConditions" ? (
              <textarea
                id={`job-${name}`}
                name={name}
                value={job[name]}
                onChange={handleChange}
                placeholder={name === "description"
                  ? "Add a summary of the job and its responsibilities"
                  : "Enter the exact required value or phrase applicants must match"}
                required={name === "description"}
              />
            ) : (
              <input
                id={`job-${name}`}
                type={
                  name.includes("application") || name === "ageAsOfDate" ? "date" :
                  name === "officialUrl" ? "url" :
                  [
                    "minAge",
                    "maxAge",
                    "minimumPercentage",
                    "minimumExperienceYears",
                    "minimumHeightCm",
                    "maximumHeightCm",
                    "minimumChestCm",
                    "maximumChestCm",
                    "minimumTenthPercentage",
                    "minimumHscPercentage",
                  ].includes(name) ? "number" :
                  "text"
                }
                name={name}
                value={job[name]}
                onChange={handleChange}
                placeholder={
                  name === "caste" || name === "location"
                    ? "Leave blank if there is no restriction"
                    : name === "officialUrl"
                      ? "https://"
                      : undefined
                }
                min={
                  name === "applicationEnd"
                    ? job.applicationStart || undefined
                    : name === "maxAge" && job.minAge !== ""
                      ? Number(job.minAge)
                    : name === "maximumHeightCm" && job.minimumHeightCm !== ""
                      ? Number(job.minimumHeightCm)
                    : name === "maximumChestCm" && job.minimumChestCm !== ""
                      ? Number(job.minimumChestCm)
                    : ["minAge", "maxAge"].includes(name)
                      ? 0
                    : [
                      "minimumPercentage",
                      "minimumExperienceYears",
                      "minimumHeightCm",
                      "maximumHeightCm",
                      "minimumChestCm",
                      "maximumChestCm",
                      "minimumTenthPercentage",
                      "minimumHscPercentage",
                    ].includes(name)
                      ? 0
                      : undefined
                }
                max={
                  name === "minAge" && job.maxAge !== ""
                    ? Number(job.maxAge)
                    : [
                      "minimumPercentage",
                      "minimumTenthPercentage",
                      "minimumHscPercentage",
                    ].includes(name)
                      ? 100
                      : undefined
                }
                step={
                  ["minAge", "maxAge"].includes(name)
                    ? 1
                    : [
                      "minimumPercentage",
                      "minimumExperienceYears",
                      "minimumHeightCm",
                      "maximumHeightCm",
                      "minimumChestCm",
                      "maximumChestCm",
                      "minimumTenthPercentage",
                      "minimumHscPercentage",
                    ].includes(name)
                      ? 0.01
                      : undefined
                }
                required={![ 
                  "caste",
                  "location",
                  "requiredStream",
                  "minimumPercentage",
                  "minAge",
                  "maxAge",
                  "ageAsOfDate",
                  "requiredNationality",
                  "minimumExperienceYears",
                  "requiredCertificate",
                  "minimumHeightCm",
                  "maximumHeightCm",
                  "minimumChestCm",
                  "maximumChestCm",
                  "requiredFitnessStatus",
                  "otherConditions",
                  "minimumTenthPercentage",
                  "minimumHscPercentage",
                ].includes(name)}
              />
            )}
          </div>
        ))}

        <section className="admin-eligibility" aria-labelledby="admin-eligibility-heading">
          <h2 id="admin-eligibility-heading">Additional eligibility</h2>
          <label htmlFor="job-disabilityRequired">
            Disability eligibility
            <span className="admin-optional"> (optional)</span>
          </label>
          <select
            id="job-disabilityRequired"
            name="disabilityRequired"
            value={job.disabilityRequired}
            onChange={handleChange}
          >
            <option value="">No disability restriction</option>
            <option value="true">Applicant must have a disability</option>
            <option value="false">Applicant must not have a disability</option>
          </select>
          <label htmlFor="job-categoryAgeLimits">
            Category-specific maximum ages
            <span className="admin-optional"> (optional; use only limits stated in the official notice)</span>
          </label>
          <textarea
            id="job-categoryAgeLimits"
            name="categoryAgeLimits"
            value={job.categoryAgeLimits}
            onChange={handleChange}
            placeholder={'{"OBC": 35, "SC": 37, "SC+PWD": 45}'}
          />
          <label htmlFor="job-validCategoryCertificateRequired">
            Valid category certificate required
            <span className="admin-optional"> (optional)</span>
          </label>
          <select
            id="job-validCategoryCertificateRequired"
            name="validCategoryCertificateRequired"
            value={job.validCategoryCertificateRequired}
            onChange={handleChange}
          >
            <option value="">No category certificate check</option>
            <option value="true">Require a valid category certificate through the closing date</option>
            <option value="false">Do not require a category certificate</option>
          </select>
          <p className="admin-help">
            Users receive an email only when every configured eligibility condition matches their profile.
            Category age keys may be category names such as OBC, SC, ST, EWS, PWD, or a combined category such as SC+PWD.
            Other conditions and fitness status use case-insensitive exact matching.
          </p>
        </section>

        <button type="submit" disabled={loading}>
          {loading ? "Creating..." : "Create Job"}
        </button>
      </form>

      {error && <p className="alert error" role="alert">{error}</p>}

      {result && (
        <div className="admin-result">
          <h2 role="status">Job created successfully</h2>
          <p>{result.title}</p>
          {result.id != null && <p>Job ID: {result.id}</p>}
        </div>
      )}
    </main>
  );
}

export default AdminDashboard;
