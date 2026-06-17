package knowledge.service;

import pojo.ResultMsg;

import java.io.IOException;

public interface KnowledgeService {
    ResultMsg getKnowledge(String question) throws IOException, InterruptedException;
}
