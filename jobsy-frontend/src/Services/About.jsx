function About() {
  return (
    <div className="about-page">

      <section className="about-hero">
        <h1>About Jobsy</h1>
        <h2>Find Government Jobs That Match You</h2>

        <p>
          Jobsy is a smart government job discovery platform that helps
          job seekers find relevant government job opportunities without
          having to search multiple websites every day.
        </p>

        <p>
          Jobsy collects job information from government recruitment
          sources and helps identify opportunities based on qualification,
          age, caste, and location.
        </p>
      </section>


      <section className="about-section">
        <h2>What Makes Jobsy Different?</h2>

        <p>
          Searching for government jobs can be time-consuming because
          different organizations publish vacancies on different websites.
          Jobsy brings this process together in one place.
        </p>

        <div className="workflow">
          <div>Government Job</div>
          <span>→</span>
          <div>Job Requirements</div>
          <span>→</span>
          <div>Eligibility Matching</div>
          <span>→</span>
          <div>User Notification</div>
        </div>
      </section>


      <section className="about-section">
        <h2>How It Works</h2>

        <div className="steps">

          <div className="step-card">
            <h3>01 — Create Your Profile</h3>
            <p>
              Register on Jobsy and provide your basic information
              and qualification details.
            </p>
          </div>

          <div className="step-card">
            <h3>02 — Discover Jobs</h3>
            <p>
              Jobsy collects government recruitment information
              and displays available opportunities in one place.
            </p>
          </div>

          <div className="step-card">
            <h3>03 — Check Eligibility</h3>
            <p>
              Our eligibility system compares job requirements with
              your qualification, age, caste, and location.
            </p>
          </div>

          <div className="step-card">
            <h3>04 — Get Notified</h3>
            <p>
              If you are eligible for a suitable job, Jobsy can
              notify you through email notifications.
            </p>
          </div>

          <div className="step-card">
            <h3>05 — Apply Officially</h3>
            <p>
              Review the job details and visit the official recruitment
              website to submit your application.
            </p>
          </div>

        </div>
      </section>


      <section className="about-section">
        <h2>Key Features</h2>

        <div className="features">

          <div className="feature">🔎 Government Job Discovery</div>
          <div className="feature">🤖 Automatic Job Collection</div>
          <div className="feature">🎓 Qualification Matching</div>
          <div className="feature">🎂 Age Eligibility</div>
          <div className="feature">📍 Location Matching</div>
          <div className="feature">📋 Caste Eligibility</div>
          <div className="feature">📧 Email Notifications</div>
          <div className="feature">🔗 Official Application Links</div>
          <div className="feature">👤 User Registration & Login</div>
          <div className="feature">🛠️ Admin Job Management</div>

        </div>
      </section>


      <section className="about-section">
        <h2>Built With</h2>

        <div className="technology">

          <div>
            <h3>Frontend</h3>
            <p>React.js, JavaScript, Axios, React Router</p>
          </div>

          <div>
            <h3>Backend</h3>
            <p>Java, Spring Boot, Spring Data JPA, Spring Security</p>
          </div>

          <div>
            <h3>Database</h3>
            <p>MySQL</p>
          </div>

          <div>
            <h3>Job Data Collection</h3>
            <p>Jsoup</p>
          </div>

          <div>
            <h3>Notifications</h3>
            <p>JavaMail / Gmail SMTP</p>
          </div>

        </div>
      </section>


      <section className="mission">
        <h2>Our Mission</h2>

        <p>
          Our goal is simple:
        </p>

        <h3>
          "Make government job searching easier, faster,
          and more personalized."
        </h3>

        <p>
          Jobsy helps users spend less time searching for opportunities
          and more time preparing for them.
        </p>
      </section>


      <section className="jobsy-footer">
        <h1>Jobsy</h1>
        <p>Discover. Match. Get Notified. Apply.</p>
      </section>

    </div>
  );
}

export default About;