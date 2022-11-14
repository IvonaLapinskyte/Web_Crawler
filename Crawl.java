import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Crawl {

    int limit = 10000;
    int used = 0;
    ArrayList<String> visited = new ArrayList<String>();
    ArrayList<LinkStats> linkStats = new ArrayList<LinkStats>();
    String[] terms;
    int[] found;
    String url;

    public Crawl(String url, String[] terms, int limit) {
        this.url = url;
        this.limit = limit;
        this.terms = terms;
        found = new int[terms.length];
    }

    public void start(){
        try{
            crawl(1,url);
            System.out.println("Base url: "+url);
            int total=0;
            for(int i=0;i<terms.length;i++){
                System.out.println("Term: "+ terms[i]+" Found: "+found[i]);
                total+=found[i];
            }
            System.out.println("Total: "+total);
            generateCSV();
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    private void generateCSV(){
        try{
            FileWriter file = new FileWriter("test.csv");
            int total=0;
            for(int i=0;i<terms.length;i++){
                file.write(terms[i]+","+found[i]+"\r\n");
                total+=found[i];
            }
            file.write("Total,"+total);
            file.close();

            FileWriter top = new FileWriter("top10.csv");
            linkStats.sort(Comparator.comparingInt(o -> o.total));
            Collections.reverse(linkStats);
            top.write("URL,"+String.join(",",terms)+"\r\n");
            top.write("\r\n");
            for(int i=0;i<10;i++){
                top.write(linkStats.get(i).generateCsvLine());

            }
            top.close();
        }
        catch (Exception e){}
    }

    private void crawl(int level, String url) throws Exception {
        if(level <= 8 && used < limit) {
            Document doc = request(url);
            if(doc != null) {
                System.out.println("Level:" + level);
                System.out.println("Link:" + url);
                System.out.println("Title:"+doc.title());
                System.out.println("used:"+used);
                System.out.println();
                LinkStats stats = new LinkStats(url, terms.length);
                for(Element element : doc.getAllElements()){
                    int i=0;
                    for (String term : terms){
                        if (element.text().contains(term)) {
                            found[i] += 1;
                            stats.updateStat(i);
                        }
                        i++;
                    }
                }
                linkStats.add(stats);

                if (level  < 8) {
                    for (Element link : doc.select("a[href]")) {
                        String next_link = link.absUrl("href");
                        int z = next_link.indexOf("#");
                        if (z > 0)
                            next_link = next_link.substring(0, z);
                        if (visited.contains(next_link) == false) {
                            crawl(level + 1, next_link);
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param url request url
     * @return Document html document got from request
     */
    private Document request(String url) throws Exception {
        try {
            Connection.Response con = Jsoup.connect(url).execute();
            if(con.statusCode() == 200){
                Document doc = con.parse();
                visited.add(url);
                used += 1;
                return doc;
            }
            return null;
        }
        catch(Exception e) {
            return null;
            //throw new Exception("Error occured "+e.getMessage());

        }
    }

    /**
     * Class used for tracking link statistics
     */
    class LinkStats {
        String url;
        public int total=0;
        int[] found;

        public LinkStats(String url,int amount){
            this.url = url;
            found = new int[amount];
        }

        /**
         * Used to update found term amount based on term index
         * @param index term index
         */
        public void updateStat(int index){
            found[index] +=1;
            total +=1;
        }

        /**
         * Generate CSV line based on collected statistics
         * @return String
         */
        public String generateCsvLine(){
            return url+","+ IntStream.of(found).mapToObj(i -> String.valueOf(i)).collect(Collectors.joining(","))+"\r\n";
        }
    }
}


