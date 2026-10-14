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
public class IBPSScraper implements GovermentJobScraper {

    private static final String URL =
            "https://www.ibps.in/";

    @Override
    public List<JOB> fetchJobs() {

        List<JOB> jobs = new ArrayList<>();

        try {

            System.out.println("Scraping IBPS website: " + URL);

            Document doc = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/154.0.0.0 Safari/537.36")
                    .timeout(30000)
                    .get();

            Elements links = doc.select("a[href]");

            for (Element link : links) {

                String title = link.text().trim();
                String jobUrl = link.absUrl("href");

                if (title.isEmpty()) {
                    continue;
                }

                String text = title.toLowerCase();

                if (text.contains("recruitment")
                        || text.contains("notification")
                        || text.contains("po")
                        || text.contains("clerk")
                        || text.contains("specialist")
                        || text.contains("rrb")) {

                    JOB job = new JOB();

                    job.setTitle(title);
                    job.setDepartment("IBPS");
                    job.setDescription(
                            "Banking recruitment through IBPS"
                    );
                    job.setOfficialUrl(jobUrl);

                    jobs.add(job);

                    System.out.println("IBPS JOB: " + title);
                    System.out.println("URL: " + jobUrl);
                }
            }

        } catch (Exception e) {

            System.out.println("IBPS scraping failed");
            e.printStackTrace();
        }

        return jobs;
    }
}

