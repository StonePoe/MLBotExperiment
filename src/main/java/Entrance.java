import core.DefaultFilter;
import org.json.JSONArray;
import util.JDBCConnector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stonezhang on 2017/10/16.
 */
public class Entrance {
    public static void main(String[] args) {
        JDBCConnector jdbcConnector = new JDBCConnector("root", "root", "nla");

        JSONArray jsonCorpus = jdbcConnector.select("select content from businessReview");

        List<String> corpus = new ArrayList<>();
        for(int i=0; i<jsonCorpus.length();i++) {
            corpus.add(jsonCorpus.getJSONObject(i).getString("content"));
        }

        DefaultFilter defaultFilter = new DefaultFilter(corpus);
        defaultFilter.defaultWashAndSplit();
        System.out.println(defaultFilter.getProcessedWords());
    }
}
