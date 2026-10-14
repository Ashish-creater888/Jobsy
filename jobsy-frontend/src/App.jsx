import { useEffect, useState } from "react";
import { BrowserRouter, Link, Route, Routes, useNavigate } from "react-router-dom";
import "./App.css";
import AdminDashboard from "./Services/AdminDashboard.jsx";
import api, { getAllJobs, getCachedJobs } from "./Services/api.js";

function useJobs() {
  const [jobs, setJobs] = useState(() => getCachedJobs() || []);
  const [loading, setLoading] = useState(() => getCachedJobs() === null);
  const [error, setError] = useState("");

  useEffect(() => {
    let isActive = true;

    getAllJobs()
      .then(({ data }) => {
        if (!Array.isArray(data)) {
          throw new Error("The jobs service returned an unexpected response.");
        }
        if (isActive) setJobs(data);
      })
      .catch((requestError) => {
        if (isActive) {
          const message = requestError.code === "ERR_NETWORK"
            ? "Could not connect to the jobs service. Check that the backend is running on port 8081."
            : requestError.response?.data?.message || requestError.message || "Could not load jobs. Please try again.";
          if (getCachedJobs() === null) setError(message);
        }
      })
      .finally(() => {
        if (isActive) setLoading(false);
      });

    return () => {
      isActive = false;
    };
  }, []);

  return { jobs, loading, error };
}

function Home() {
  const { jobs, loading, error } = useJobs();

  return (
    <main className="home-layout">
      <section className="home-hero">
        <div>
          <span className="eyebrow">YOUR NEXT OPPORTUNITY STARTS HERE</span>
          <h1>Find your next <span>government job</span></h1>
          <p>Explore current opportunities and take the next step in your career with Jobsy.</p>
          <Link className="nav-btn" to="/jobs">Explore jobs</Link>
        </div>
        <div className="summary-box" aria-label="Jobsy highlights">
          <div><strong>{loading ? "…" : error ? "—" : jobs.length}</strong><span>Available jobs</span></div>
          <div><strong>1</strong><span>Easy platform</span></div>
          <div><strong>24/7</strong><span>Explore anytime</span></div>
        </div>
      </section>
      <section>
        <h2>Featured opportunities</h2>
        {loading && <p role="status">Loading jobs...</p>}
        {error && <p className="alert error" role="alert">{error}</p>}
        {!loading && !error && jobs.length === 0 && <p className="empty-state">No jobs are available right now.</p>}
        {!loading && !error && <JobCards items={jobs.slice(0, 2)} />}
      </section>
      <footer className="footer">Jobsy — find opportunities that fit your future.</footer>
    </main>
  );
}

function JobCards({ items }) {
  return (
    <div className="job-listings">
      {items.map((job, index) => {
        const officialUrl = getOfficialJobUrl(job.officialUrl);

        return (
          <article className="job-card" key={job.id ?? `${job.department}-${job.officialUrl || job.title}-${index}`}>
            <p className="job-card-label">Post name</p>
            <h3>{getPostTitle(job.title) || "Post name unavailable"}</h3>
            <p className="job-card-label">URL</p>
            {officialUrl ? (
              <a
                className="job-card-url"
                href={officialUrl}
                target="_blank"
                rel="noreferrer"
              >
                {officialUrl}
              </a>
            ) : (
              <p className="job-card-url-unavailable">Official URL unavailable</p>
            )}
          </article>
        );
      })}
    </div>
  );
}

function getOfficialJobUrl(value) {
  if (!value?.trim()) return "";

  try {
    const url = new URL(value.trim());
    return ["http:", "https:"].includes(url.protocol) ? url.href : "";
  } catch {
    return "";
  }
}

function getPostTitle(title) {
  const cleanedTitle = title
    ?.replace(/^\s*#+\s*/, "")
    .replace(/\s*read\s*mor(?:e)?\b[\s\S]*$/i, "")
    .replace(/\s+/g, " ")
    .trim();

  if (!cleanedTitle) return "";

  const roleTitles = cleanedTitle.match(
    /recruitment\s+to\s+the\s+post\s+of\s+[^()]*\(([^()]*)\)/i
  )?.[1];
  if (roleTitles) return roleTitles.replace(/\s*,\s*/g, ", ").trim();

  return cleanedTitle
    .replace(/^\s*advt\.?\s*no\.?\s*[\w/-]+(?:\s+dated\.?\s*[\d./-]+)?\s*/i, "")
    .replace(/^\s*recruitment\s+to\s+the\s+post\s+of\s+/i, "")
    .trim();
}

function Jobs() {
  const [search, setSearch] = useState("");
  const { jobs, loading, error } = useJobs();
  const postNames = [...new Set(
    jobs.map((job) => job.title?.trim()).filter(Boolean)
  )].sort((first, second) => first.localeCompare(second));
  const filteredJobs = jobs.filter((job) =>
    `${job.title} ${job.department} ${job.location}`.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <main className="home-layout">
      <h1>Available government jobs</h1>
      <p>Browse government vacancies saved by the Jobsy admin team.</p>
      <label className="job-search-wrap">
        <span className="sr-only">Search jobs</span>
        <input
          className="job-search"
          type="search"
          list="job-post-names"
          aria-label="Search jobs by post name, department, or location"
          placeholder="Type a post name to search jobs"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
        />
        <datalist id="job-post-names">
          {postNames.map((name) => <option key={name} value={name} />)}
        </datalist>
      </label>
      {loading && <p role="status">Loading saved jobs...</p>}
      {error && <p className="alert error" role="alert">{error}</p>}
      {!loading && !error && search.trim() && (
        <p role="status">Found {filteredJobs.length} matching post{filteredJobs.length === 1 ? "" : "s"}.</p>
      )}
      {!loading && !error && filteredJobs.length > 0 && <JobCards items={filteredJobs} />}
      {!loading && !error && filteredJobs.length === 0 && (
        <p className="empty-state">No jobs match your search.</p>
      )}
    </main>
  );
}

function Dashboard() {
  const [user] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem("jobsy-user") || "null");
    } catch {
      return null;
    }
  });
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(
    () => Number.isInteger(user?.id) && user.id > 0
  );
  const [error, setError] = useState("");

  useEffect(() => {
    if (!Number.isInteger(user?.id) || user.id <= 0) {
      return undefined;
    }

    let isActive = true;
    api.get(`/eligibility/users/${user.id}/jobs`)
      .then(({ data }) => {
        if (!Array.isArray(data)) {
          throw new Error("The dashboard service returned an unexpected response.");
        }
        if (isActive) setJobs(data);
      })
      .catch((requestError) => {
        if (isActive) {
          setError(
            requestError.code === "ERR_NETWORK"
              ? "Could not connect to the jobs service. Check that the backend is running on port 8081."
              : requestError.response?.data?.message
                || requestError.response?.data?.detail
                || requestError.message
                || "Could not load your matching jobs."
          );
        }
      })
      .finally(() => {
        if (isActive) setLoading(false);
      });

    return () => {
      isActive = false;
    };
  }, [user]);

  if (!Number.isInteger(user?.id) || user.id <= 0) {
    return (
      <main className="dashboard-layout">
        <section className="dashboard-empty">
          <span className="eyebrow">PERSONALIZED DASHBOARD</span>
          <h1>Sign in to see your matches</h1>
          <p>Your dashboard shows saved government jobs that match your profile and their application deadlines.</p>
          <Link className="nav-btn" to="/login">Login to continue</Link>
        </section>
      </main>
    );
  }

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const closingSoon = jobs.filter((job) => {
    if (!job.applicationEnd) return false;
    const deadline = new Date(`${job.applicationEnd}T00:00:00`);
    const daysUntilDeadline = Math.ceil((deadline - today) / 86_400_000);
    return daysUntilDeadline >= 0 && daysUntilDeadline <= 7;
  }).length;

  return (
    <main className="dashboard-layout">
      <section className="dashboard-header">
        <div>
          <span className="eyebrow">PERSONALIZED DASHBOARD</span>
          <h1>Welcome{user.name ? `, ${user.name}` : " back"}</h1>
          <p>Here are the saved government vacancies that match your profile.</p>
        </div>
        <Link className="dashboard-browse-link" to="/jobs">Browse all jobs</Link>
      </section>

      <section className="dashboard-stats" aria-label="Dashboard summary">
        <article className="dashboard-stat">
          <span>Matching jobs</span>
          <strong>{loading ? "…" : error ? "—" : jobs.length}</strong>
        </article>
        <article className="dashboard-stat">
          <span>Closing in 7 days</span>
          <strong>{loading ? "…" : error ? "—" : closingSoon}</strong>
        </article>
        <article className="dashboard-stat">
          <span>Email notifications</span>
          <strong>On</strong>
        </article>
      </section>

      <section className="dashboard-jobs" aria-labelledby="dashboard-jobs-heading">
        <div className="dashboard-section-heading">
          <div>
            <h2 id="dashboard-jobs-heading">Jobs matching your profile</h2>
            <p>Review the official notice and confirm every requirement before applying.</p>
          </div>
        </div>
        {loading && <p role="status">Finding jobs that match your profile...</p>}
        {error && <p className="alert error" role="alert">{error}</p>}
        {!loading && !error && jobs.length === 0 && (
          <div className="dashboard-empty">
            <h3>No matching jobs yet</h3>
            <p>When saved vacancies match your profile, they will appear here. Check your profile details and browse all saved jobs in the meantime.</p>
            <Link className="nav-btn" to="/jobs">Browse saved jobs</Link>
          </div>
        )}
        {!loading && !error && jobs.length > 0 && (
          <div className="dashboard-job-list">
            {jobs.map((job) => {
              const officialUrl = getOfficialJobUrl(job.officialUrl);
              const isClosed = job.applicationEnd
                && new Date(`${job.applicationEnd}T23:59:59`) < new Date();

              return (
                <article className="dashboard-job-card" key={job.id}>
                  <div className="dashboard-job-content">
                    <span className={`dashboard-job-status${isClosed ? " is-closed" : ""}`}>
                      {isClosed ? "Closed" : "Profile match"}
                    </span>
                    <h3>{getPostTitle(job.title) || "Government vacancy"}</h3>
                    <p className="dashboard-job-meta">
                      {[job.department, job.location].filter(Boolean).join(" · ") || "Government recruitment"}
                    </p>
                    <p className="dashboard-deadline">
                      {job.applicationEnd
                        ? `Application deadline: ${new Date(`${job.applicationEnd}T00:00:00`).toLocaleDateString()}`
                        : "Check the official notice for the application deadline"}
                    </p>
                  </div>
                  {officialUrl && (
                    <a className="dashboard-apply-link" href={officialUrl} target="_blank" rel="noreferrer">
                      View official notice
                    </a>
                  )}
                </article>
              );
            })}
          </div>
        )}
        <p className="dashboard-disclaimer">
          Matches are based on the profile information provided. Always verify eligibility and deadlines in the official recruitment notice.
        </p>
      </section>
    </main>
  );
}

const featureCards = [
  {
    title: "AI Job Extractor",
    description: "Automatically collect vacancy details from permitted official sources and recruitment PDFs.",
    icon: "🤖",
  },
  {
    title: "Smart Eligibility Engine",
    description: "Use Java services to match each vacancy requirement against the user's profile and qualifications.",
    icon: "⚙️",
  },
  {
    title: "Personalized Dashboard",
    description: "Display matching jobs, closing dates, and timely notifications in a polished React interface.",
    icon: "📊",
  },
  {
    title: "Government Job Discovery",
    description: "Bring official opportunities together in one place and make job discovery faster and more transparent.",
    icon: "🏛️",
  },
];

function About() {
  return (
    <main className="home-layout">
      <span className="eyebrow">ABOUT JOBSY</span>
      <h1>Opportunities, made easier to find.</h1>
      <p>Jobsy brings government job opportunities together in one place so you can explore openings and find the details you need.</p>

      <div className="feature-grid" aria-label="Jobsy features">
        {featureCards.map((feature) => (
          <article className="feature-card" key={feature.title}>
            <div className="feature-icon" aria-hidden="true">{feature.icon}</div>
            <h3>{feature.title}</h3>
            <p>{feature.description}</p>
          </article>
        ))}
      </div>

    </main>
  );
}

const qualificationOptions = [
  "10th", "12th", "ITI", "Diploma", "B.A.", "B.Sc.", "B.Com.", "BCA",
  "BCS", "B.E.", "B.Tech", "M.A.", "M.Sc.", "M.Com.", "MCA", "M.Tech",
  "MBA", "PhD", "Other",
];

const emptyEducation = () => ({
  qualification: "",
  degreeName: "",
  stream: "",
  universityBoard: "",
  collegeInstitute: "",
  passingYear: "",
  percentage: "",
  cgpa: "",
  totalMarks: "",
  obtainedMarks: "",
  resultStatus: "",
});

function AuthPage({ register = false }) {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: "",
    email: "",
    mobile: "",
    password: "",
    dateOfBirth: "",
    gender: "",
    fatherName: "",
    motherName: "",
    nationality: "",
    state: "",
    district: "",
    address: "",
    pincode: "",
    profilePhoto: "",
    category: "",
    obcType: "",
    casteName: "",
    casteCertificateNumber: "",
    casteCertificateIssueDate: "",
    casteCertificateValidity: "",
    ewsCertificateNumber: "",
    ewsCertificateValidity: "",
    pwd: "",
    disabilityType: "",
    disabilityPercentage: "",
    disabilityCertificateNumber: "",
    exServiceman: "",
    exServicemanServiceDetails: "",
    sportsQuota: "",
    freedomFighterDependent: "",
    governmentEmployeeStatus: "",
    workExperienceYears: "",
    certificatesAndLicences: "",
    heightCm: "",
    chestCm: "",
    physicalFitnessStatus: "",
    otherEligibilityDetails: "",
    qualifications: [],
  });
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  const handleEducationChange = (index, event) => {
    setForm((current) => ({
      ...current,
      qualifications: current.qualifications.map((education, rowIndex) =>
        rowIndex === index
          ? { ...education, [event.target.name]: event.target.value }
          : education
      ),
    }));
  };

  const addEducation = () => {
    setForm((current) => ({
      ...current,
      qualifications: [...current.qualifications, emptyEducation()],
    }));
  };

  const removeEducation = (index) => {
    setForm((current) => ({
      ...current,
      qualifications: current.qualifications.filter((_, rowIndex) => rowIndex !== index),
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setMessage("");
    try {
      if (register) {
        const educationRows = form.qualifications.filter((education) =>
          Object.values(education).some((value) => String(value).trim() !== "")
        );
        if (educationRows.some((education) => !education.qualification)) {
          setMessage("Choose a qualification for each education entry.");
          setLoading(false);
          return;
        }

        const profile = { ...form };
        delete profile.qualifications;
        const toBoolean = (value) => value === "" ? null : value === "true";
        const toNumber = (value) => value === "" ? null : Number(value);
        const dateFields = [
          "dateOfBirth",
          "casteCertificateIssueDate",
          "casteCertificateValidity",
          "ewsCertificateValidity",
        ];
        const profilePayload = { ...profile };
        dateFields.forEach((field) => {
          profilePayload[field] = profilePayload[field] || null;
        });
        profilePayload.pwd = toBoolean(profile.pwd);
        profilePayload.disability = profilePayload.pwd;
        profilePayload.exServiceman = toBoolean(profile.exServiceman);
        profilePayload.sportsQuota = toBoolean(profile.sportsQuota);
        profilePayload.freedomFighterDependent = toBoolean(profile.freedomFighterDependent);
        profilePayload.governmentEmployeeStatus = toBoolean(profile.governmentEmployeeStatus);
        profilePayload.disabilityPercentage = toNumber(profile.disabilityPercentage);
        profilePayload.workExperienceYears = toNumber(profile.workExperienceYears);
        profilePayload.heightCm = toNumber(profile.heightCm);
        profilePayload.chestCm = toNumber(profile.chestCm);

        const numericEducationFields = [
          "passingYear", "percentage", "cgpa", "totalMarks", "obtainedMarks",
        ];
        const savedEducation = educationRows.map((education) => {
          const entry = { ...education };
          numericEducationFields.forEach((field) => {
            entry[field] = toNumber(entry[field]);
          });
          return entry;
        });
        await api.post("/users/register", {
          ...profilePayload,
          qualifications: savedEducation,
        });
      } else {
        const { data } = await api.post("/users/login", { email: form.email, password: form.password });
        if (!Number.isInteger(data?.id) || data.id <= 0) {
          throw new Error("Login succeeded but the backend did not return a valid user profile.");
        }
        localStorage.setItem("jobsy-user", JSON.stringify({
          id: data.id,
          name: data.name || "",
          email: data.email || form.email,
        }));
      }
      setMessage(register ? "Registration successful. You can now sign in." : "Login successful.");
      if (!register) navigate("/dashboard");
    } catch (error) {
      setMessage(
        error.response?.data?.message
          || error.response?.data?.detail
          || (error.code === "ERR_NETWORK"
            ? "Could not connect to the backend. Make sure it is running on port 8081."
            : error.message)
          || "Please check your details and try again."
      );
    } finally {
      setLoading(false);
    }
  };

  const renderInput = (label, name, type = "text", attributes = {}) => (
    <div className="form-field" key={name}>
      <label htmlFor={name}>{label}</label>
      <input
        id={name}
        name={name}
        type={type}
        value={form[name]}
        onChange={handleChange}
        {...attributes}
      />
    </div>
  );

  const renderSelect = (label, name, options, required = false) => (
    <div className="form-field" key={name}>
      <label htmlFor={name}>{label}</label>
      <select id={name} name={name} value={form[name]} onChange={handleChange} required={required}>
        <option value="">Select {label.toLowerCase()}</option>
        {options.map((option) => (
          <option key={option.value ?? option} value={option.value ?? option}>
            {option.label ?? option}
          </option>
        ))}
      </select>
    </div>
  );

  const renderYesNoSelect = (label, name) => renderSelect(label, name, [
    { value: "true", label: "Yes" },
    { value: "false", label: "No" },
  ]);

  const numberAttributes = (min, max, step = "1") => ({ min, max, step });

  return (
    <main className={register ? "register-layout" : "auth-layout"}>
      <section className="welcome-panel">
        <span className="eyebrow">WELCOME TO JOBSY</span>
        <h1>{register ? "Create your account" : "Welcome back"}</h1>
        <p>
          {register
            ? "Create your profile once and keep your personal, reservation, and education details together."
            : "Sign in or register to continue exploring job opportunities."}
        </p>
      </section>
      <section className={`auth-card${register ? " registration-card" : ""}`}>
        <h2>{register ? "Register" : "Login"}</h2>
        <p className="auth-subtitle">Enter your details below.</p>
        {message && <p className="alert" role="alert">{message}</p>}
        <form className={register ? "register-form" : ""} onSubmit={handleSubmit}>
          {register && (
            <>
              <section className="registration-section">
                <h3>Personal Details</h3>
                <div className="form-grid">
                  {renderInput("Full Name", "name", "text", { autoComplete: "name", required: true })}
                  {renderInput("Mobile Number", "mobile", "tel", { autoComplete: "tel", required: true })}
                  {renderInput("Date of Birth", "dateOfBirth", "date", { max: new Date().toISOString().slice(0, 10) })}
                  {renderSelect("Gender", "gender", ["Male", "Female", "Other", "Prefer not to say"])}
                  {renderInput("Father's Name", "fatherName")}
                  {renderInput("Mother's Name", "motherName")}
                  {renderInput("Nationality", "nationality")}
                  {renderInput("State", "state", "text", { autoComplete: "address-level1" })}
                  {renderInput("District", "district", "text", { autoComplete: "address-level2" })}
                  {renderInput("Pincode", "pincode", "text", { inputMode: "numeric", autoComplete: "postal-code" })}
                  {renderInput("Profile Photo URL", "profilePhoto", "url", { placeholder: "https://..." })}
                  <div className="form-field full-width">
                    <label htmlFor="address">Address</label>
                    <textarea id="address" name="address" autoComplete="street-address" value={form.address} onChange={handleChange} />
                  </div>
                </div>
              </section>

              <section className="registration-section">
                <h3>Category / Reservation Details</h3>
                <div className="form-grid">
                  {renderSelect("Category", "category", [
                    "General / UR", "OBC", "SC", "ST", "EWS",
                  ], true)}
                  {form.category === "OBC" && renderSelect("OBC Type", "obcType", ["OBC-NCL", "OBC-CL"])}
                  {renderInput("Caste Name", "casteName")}
                  {renderInput("Caste Certificate Number", "casteCertificateNumber")}
                  {renderInput("Caste Certificate Issue Date", "casteCertificateIssueDate", "date")}
                  {renderInput("Caste Certificate Validity", "casteCertificateValidity", "date")}
                  {renderInput("EWS Certificate Number", "ewsCertificateNumber")}
                  {renderInput("EWS Certificate Validity", "ewsCertificateValidity", "date")}
                  {renderYesNoSelect("PwD / Disability Status", "pwd")}
                  {renderInput("Disability Type", "disabilityType")}
                  {renderInput("Disability Percentage", "disabilityPercentage", "number", numberAttributes(0, 100, "0.01"))}
                  {renderInput("Disability Certificate Number", "disabilityCertificateNumber")}
                  {renderYesNoSelect("Ex-Serviceman", "exServiceman")}
                  {form.exServiceman === "true" && (
                    <div className="form-field full-width">
                      <label htmlFor="exServicemanServiceDetails">Ex-Serviceman Service Details</label>
                      <textarea id="exServicemanServiceDetails" name="exServicemanServiceDetails" value={form.exServicemanServiceDetails} onChange={handleChange} />
                    </div>
                  )}
                  {renderYesNoSelect("Sports Quota", "sportsQuota")}
                  {renderYesNoSelect("Freedom Fighter Dependent", "freedomFighterDependent")}
                  {renderYesNoSelect("Government Employee Status", "governmentEmployeeStatus")}
                </div>
              </section>

              <section className="registration-section">
                <h3>Experience and Other Eligibility Details</h3>
                <p>These details are used only when an official job notice makes them mandatory.</p>
                <div className="form-grid">
                  {renderInput("Work Experience (years)", "workExperienceYears", "number", numberAttributes(0, undefined, "0.1"))}
                  {renderInput("Height (cm)", "heightCm", "number", numberAttributes(0, undefined, "0.1"))}
                  {renderInput("Chest Measurement (cm)", "chestCm", "number", numberAttributes(0, undefined, "0.1"))}
                  {renderInput("Physical Fitness Status", "physicalFitnessStatus")}
                  <div className="form-field full-width">
                    <label htmlFor="certificatesAndLicences">Certificates / Licences</label>
                    <textarea
                      id="certificatesAndLicences"
                      name="certificatesAndLicences"
                      value={form.certificatesAndLicences}
                      onChange={handleChange}
                      placeholder="List each certificate or licence, separated by commas"
                    />
                  </div>
                  <div className="form-field full-width">
                    <label htmlFor="otherEligibilityDetails">Other Post-Specific Eligibility Details</label>
                    <textarea
                      id="otherEligibilityDetails"
                      name="otherEligibilityDetails"
                      value={form.otherEligibilityDetails}
                      onChange={handleChange}
                      placeholder="Enter the exact value/phrase that matches an official job requirement"
                    />
                  </div>
                </div>
              </section>

              <section className="registration-section">
                <div className="section-heading-row">
                  <div>
                    <h3>Education Details</h3>
                    <p>Add a separate entry for each qualification.</p>
                  </div>
                  <button className="secondary-btn" type="button" onClick={addEducation}>Add Qualification</button>
                </div>
                {form.qualifications.length === 0 && (
                  <p className="education-empty">No education entries added yet.</p>
                )}
                {form.qualifications.map((education, index) => {
                  const educationField = (label, name, type = "text", attributes = {}) => (
                    <div className="form-field" key={name}>
                      <label htmlFor={`education-${index}-${name}`}>{label}</label>
                      <input
                        id={`education-${index}-${name}`}
                        name={name}
                        type={type}
                        value={education[name]}
                        onChange={(event) => handleEducationChange(index, event)}
                        {...attributes}
                      />
                    </div>
                  );
                  const educationSelect = (label, name, options) => (
                    <div className="form-field" key={name}>
                      <label htmlFor={`education-${index}-${name}`}>{label}</label>
                      <select
                        id={`education-${index}-${name}`}
                        name={name}
                        value={education[name]}
                        onChange={(event) => handleEducationChange(index, event)}
                      >
                        <option value="">Select {label.toLowerCase()}</option>
                        {options.map((option) => <option key={option} value={option}>{option}</option>)}
                      </select>
                    </div>
                  );

                  return (
                    <div className="education-card" key={index}>
                      <div className="section-heading-row">
                        <h4>Qualification {index + 1}</h4>
                        <button className="text-btn" type="button" onClick={() => removeEducation(index)}>
                          Remove
                        </button>
                      </div>
                      <div className="form-grid">
                        {educationSelect("Qualification", "qualification", qualificationOptions)}
                        {educationField("Degree Name", "degreeName")}
                        {educationField("Specialization / Branch", "stream")}
                        {educationField("University / Board", "universityBoard")}
                        {educationField("College / Institute", "collegeInstitute")}
                        {educationField("Passing Year", "passingYear", "number", numberAttributes(1900, 2100))}
                        {educationField("Percentage", "percentage", "number", numberAttributes(0, 100, "0.01"))}
                        {educationField("CGPA", "cgpa", "number", numberAttributes(0, 10, "0.01"))}
                        {educationField("Total Marks", "totalMarks", "number", numberAttributes(0, undefined, "0.01"))}
                        {educationField("Obtained Marks", "obtainedMarks", "number", numberAttributes(0, undefined, "0.01"))}
                        {educationSelect("Result Status", "resultStatus", ["Passed", "Appearing", "Failed", "Other"])}
                      </div>
                    </div>
                  );
                })}
              </section>
            </>
          )}
          <section className={`registration-section account-section${register ? "" : " login-section"}`}>
            <h3>{register ? "Account Details" : "Sign In"}</h3>
            <div className={register ? "form-grid" : ""}>
              {renderInput("Email", "email", "email", { autoComplete: "email", required: true })}
              {renderInput("Password", "password", "password", {
                autoComplete: register ? "new-password" : "current-password",
                required: true,
              })}
            </div>
          </section>
          <button className="submit-btn" type="submit" disabled={loading}>
            {loading ? "Please wait..." : register ? "Create account" : "Login"}
          </button>
        </form>
        <p className="switch-auth">
          {register ? "Already registered? " : "New to Jobsy? "}
          <Link to={register ? "/login" : "/register"}>{register ? "Login" : "Register"}</Link>
        </p>
      </section>
    </main>
  );
}

function App() {
  return (
    <BrowserRouter>
      <div className="jobsy-app">
        <header className="navbar">
          <Link className="brand" to="/home" aria-label="Jobsy home">
            <img className="brand-logo" src="/jobsy-logo.png" alt="Jobsy logo" />
          </Link>
          <nav aria-label="Main navigation">
            <Link className="nav-link" to="/home">Home</Link>
            <Link className="nav-link" to="/jobs">Jobs</Link>
            <Link className="nav-link" to="/dashboard">Dashboard</Link>
            <Link className="nav-link" to="/about">About</Link>
            <Link className="nav-link" to="/admin">Admin</Link>
            <Link className="nav-link" to="/login">Login</Link>
            <Link className="nav-btn" to="/register">Register</Link>
          </nav>
        </header>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/home" element={<Home />} />
          <Route path="/jobs" element={<Jobs />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/about" element={<About />} />
          <Route path="/login" element={<AuthPage />} />
          <Route path="/register" element={<AuthPage register />} />
          <Route path="/admin" element={<AdminDashboard />} />
          <Route
            path="*"
            element={
              <main className="home-layout">
                <h1>Page not found</h1>
                <p>The page you requested does not exist.</p>
                <Link to="/home">Return home</Link>
              </main>
            }
          />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
