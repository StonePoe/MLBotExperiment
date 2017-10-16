package core.filter;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by stonezhang on 2017/9/13.
 */
public class DefaultFilter {

    private List<String> corpusList;

    private List<String> processedWords;

    private static final String onlpBinPath = "en-token.bin";
    private TokenizerModel model;
    private Tokenizer tokenizer;

    private static final String stopwordsPath = "stopwords.txt";
    private List<String> stopwordsList;

    private static final String recordPath = "filteredText.txt";

    private double eRange = 0.20;

    public DefaultFilter(List<String> corpusList) {
        this.corpusList = corpusList;
        processedWords = new ArrayList<>();

        try {
            InputStream is = new FileInputStream(onlpBinPath);
            model = new TokenizerModel(is);
            tokenizer = new TokenizerME(model);

            loadStopWords();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int defaultWashAndSplit() {
        int eliminatingCount = 0;
        FileWriter writer = null;

        try {
            writer=new FileWriter(recordPath,true);
            SimpleDateFormat format=new SimpleDateFormat();
            String time=format.format(new Date());
            writer.write(String.format("Filtered sentences; task started at: %s \n", time));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String corpus: corpusList) {
            List<String> result = entropyTest(splitWords(corpus));

            if (result.isEmpty() && writer != null) {
                try {
                    writer.write("\t" + corpus + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            processedWords.addAll(result);
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return eliminatingCount;
    }

    public List<String> getProcessedWords() {
        return this.processedWords;
    }

    private void loadStopWords() throws IOException {
        stopwordsList = new ArrayList<>();

        InputStreamReader read = new InputStreamReader(
                new FileInputStream(stopwordsPath));

        BufferedReader bufferedReader = new BufferedReader(read);
        String lineTxt = null;

        while ((lineTxt = bufferedReader.readLine()) != null) {
            stopwordsList.add(lineTxt);
        }

        bufferedReader.close();
        read.close();
    }

    private List<String> splitWords(String corpus) {
        String tokens[] = tokenizer.tokenize(corpus);
        return Arrays.asList(tokens);
    }

    private List<String> entropyTest(List<String> corpusList) {
        Map<String, Integer> wordCounts = new HashMap<>();
        List<String> filteredCorpus = new ArrayList<>();

        for (String corpus: corpusList) {
            if (!stopwordsList.contains(corpus)) {
                filteredCorpus.add(corpus);
                if (wordCounts.keySet().contains(corpus)) {
                    int count = wordCounts.get(corpus);
                    wordCounts.put(corpus, count+1);
                }
                else {
                    wordCounts.put(corpus, 1);
                }
            }
        }

        int sum = filteredCorpus.size();

        double rate = 0;

        for (int counts: wordCounts.values()) {
            rate += counts * -1.0 / sum * Math.log(counts * 1.0 / sum);
        }

        if (rate < eRange) {
            return new ArrayList<>();
        }
        else {
            return filteredCorpus;
        }
    }
}
