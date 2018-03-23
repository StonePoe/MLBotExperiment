import core.filter.DefaultFilter;
import core.lda.LDA;
import org.json.JSONArray;
import util.JDBCConnector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stonezhang on 2017/10/16.
 */
public class Entrance {
    public static void main(String[] args) {
        JDBCConnector jdbcConnector = new JDBCConnector("root", "root", "nla");

        JSONArray jsonCorpus = jdbcConnector.select("select content from businessReview where appInfo > 550 and appInfo < 560");

        System.out.println(jsonCorpus);

        List<String> corpus = new ArrayList<>();
        for(int i=0; i<jsonCorpus.length();i++) {
            corpus.add(jsonCorpus.getJSONObject(i).getString("content"));
        }

        DefaultFilter defaultFilter = new DefaultFilter(corpus);
        defaultFilter.defaultWashAndSplit();
        List<String> processedCorpus = defaultFilter.getProcessedWords();

        LDA lda = new LDA(5);
        lda.addDoc("business", processedCorpus);
        try {
            lda.trainAndSave("./result", "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
