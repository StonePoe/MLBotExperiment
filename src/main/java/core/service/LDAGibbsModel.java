package core.service;

import core.lda.Doc;
import core.lda.LDAModel;
import core.lda.Topic;
import core.lda.Vector;

/**
 * Created by stonezhang on 2017/10/16.
 */
public class LDAGibbsModel extends LDAModel {

    public LDAGibbsModel(int topicNum, double alpha, double beta, int iteration, int saveStep, int beginSaveIters) {
        super(topicNum, alpha, beta, iteration, saveStep, beginSaveIters);
        // TODO Auto-generated constructor stub
    }

    // Compute p(z_i = k|z_-i, w) 抽样
    protected void sampleTopic(Doc doc, Vector vector) {

        int oldTopic = vector.topicId;

        doc.removeVector(vector);

        topics[oldTopic].removeVector(vector);

        double[] p = new double[topicNum];

        for (int k = 0; k < topicNum; k++) {
            p[k] = (topics[k].vectorIdArray[vector.id] + beta) / (topics[k].vCount + vCount * beta) * (doc.topicArray[k] + alpha)
                    / (doc.topicArray.length - 1 + topicNum * alpha);
        }

        // 轮盘赌最后累计使得p[k]是前面所有topic可能性的和
        for (int k = 1; k < topicNum; k++) {
            p[k] += p[k - 1];
        }

        double u = Math.random() * p[topicNum - 1];

        int newTopic;
        for (newTopic = 0; newTopic < topicNum; newTopic++) {
            if (u < p[newTopic]) {
                break;
            }
        }

        vector.topicId = newTopic;

        topics[newTopic].addVector(vector);

        doc.updateVecotr(vector);

    }

    @Override
    protected void updateEstimatedParameters(double[][] phi, double[][] theta) {
        // TODO Auto-generated method stub

        Topic topic = null;
        for (int k = 0; k < topicNum; k++) {
            topic = topics[k];
            for (int v = 0; v < vCount; v++) {
                phi[k][v] = (topic.vectorIdArray[v] + beta) / (topic.vCount + vCount * beta);
            }
        }

        Doc doc = null;
        for (int d = 0; d < dCount; d++) {
            doc = docs.get(d);
            for (int k = 0; k < topicNum; k++) {
                theta[d][k] = (doc.topicArray[k] + alpha) / (doc.vectors.size() + topicNum * alpha);
            }
        }
    }
}