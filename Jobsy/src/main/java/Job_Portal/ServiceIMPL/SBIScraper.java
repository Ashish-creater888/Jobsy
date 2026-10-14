package Job_Portal.ServiceIMPL;



import Job_Portal.Module.JOB;
import Job_Portal.Service.GovermentJobScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SBIScraper implements GovermentJobScraper {

    private static final String URL =
            "https://sbi.co.in/web/careers";

    @Override
    public List<JOB> fetchJobs() {

        List<JOB> jobs = new ArrayList<>();

        try {

            Document document = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(15000)
                    .get();

            Elements links = document.select("a[href]");

            for (Element link : links) {

                String title = link.text().trim();
                String jobUrl = link.absUrl("href");

                if (title.isEmpty()) {
                    continue;
                }

                String text = title.toLowerCase();

                if (text.contains("recruitment")
                        || text.contains("probationary officer")
                        || text.contains("po")
                        || text.contains("clerk")
                        || text.contains("specialist officer")) {

                    JOB job = new JOB();

                    job.setTitle(title);
                    job.setDepartment("SBI");
                    job.setDescription(
                            "Recruitment through SBI Careers"
                    );
                    job.setOfficialUrl(jobUrl);

                    jobs.add(job);

                    System.out.println("SBI JOB: " + title);
                    System.out.println("URL: " + jobUrl);
                }
            }

        } catch (Exception e) {

            System.out.println("SBI scraping failed");
            e.printStackTrace();
        }

        return jobs;
    }
}
