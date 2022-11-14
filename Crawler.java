

public class Crawler {

        public static void main(String[] args) {
            String url = "https://en.wikipedia.org/wiki/Elon_Musk";
            String[] terms = new String[]{"Elon Musk", "Tesla", "Jupiter","it"};
            Crawl crawl = new Crawl(url,terms,10000);
            crawl.start();
        }

    }

