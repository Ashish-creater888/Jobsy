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
public class StateGovernmentScraper implements GovermentJobScraper {


     private final String[] websites = {

                "https://www.maharashtra.gov.in/",
                "https://www.gujarat.gov.in/",
                "https://www.karnataka.gov.in/",
                "https://www.tn.gov.in/",
                "https://www.up.gov.in/",
                "https://rajasthan.gov.in/"

        };


    @Override
    public List<JOB> fetchJobs() {

        List<JOB> jobs = new ArrayList<>();

        for (String website : websites) {

            try {

                Document document = Jsoup.connect(website)
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
                            || text.contains("career")
                            || text.contains("job")) {

                        JOB job = new JOB();

                        job.setTitle(title);
                        job.setDepartment("State Government");
                        job.setDescription(
                                "State Government Job"
                        );
                        job.setOfficialUrl(jobUrl);

                        jobs.add(job);

                        System.out.println(
                                "STATE JOB: " + title
                        );

                        System.out.println(
                                "URL: " + jobUrl
                        );
                    }
                }

            } catch (Exception e) {

                System.out.println(
                        "Unable to scrape: " + website
                );

                e.printStackTrace();
            }
        }

        return jobs;
    }
}