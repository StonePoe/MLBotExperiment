package core.lda;

/**
 * Created by stonezhang on 2017/10/16.
 */

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;


import com.google.common.io.Files;
import core.service.LDAGibbsModel;

/**
 * LDA 的主类
 *
 * @author ansj
 *
 */
public class LDA {

    /**
     * 训练模型类
     */
    private LDAModel ldaAModel = null;

    public LDA(int topicNum) {
        this.ldaAModel = new LDAGibbsModel(topicNum, 50/(double)topicNum, 0.1, 100, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     *
     * @param ldaModel 模型
     */
    public LDA(LDAModel ldaModel) {
        this.ldaAModel = ldaModel;
    }

    /**
     * LDA 根据文本训练,一个流相当于一个文档
     */
    public void addDoc(String name, List<String> words) {
        ldaAModel.addDoc(name,words);
    }

    public void trainAndSave(String modelPath, String charset) throws IOException {
        ldaAModel.trainAndSave(modelPath, charset);
    }

}