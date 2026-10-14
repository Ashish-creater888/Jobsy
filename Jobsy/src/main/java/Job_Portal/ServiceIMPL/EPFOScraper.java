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
public class EPFOScraper implements GovermentJobScraper {

    private static final String URL =
            "https://www.epfindia.gov.in/";

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
                        || text.contains("vacancy")
                        || text.contains("notification")
                        || text.contains("appointment")) {

                    JOB job = new JOB();

                    job.setTitle(title);
                    job.setDepartment("EPFO");
                    job.setDescription(
                            "Government recruitment through EPFO"
                    );
                    job.setOfficialUrl(jobUrl);

                    jobs.add(job);

                    System.out.println("EPFO JOB: " + title);
                    System.out.println("URL: " + jobUrl);
                }
            }

        } catch (Exception e) {

            System.out.println("EPFO scraping failed");
            e.printStackTrace();
        }

        return jobs;
    }
}