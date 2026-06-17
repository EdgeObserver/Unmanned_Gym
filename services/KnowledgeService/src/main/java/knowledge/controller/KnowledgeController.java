package knowledge.controller;

import knowledge.service.impl.KnowledgeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pojo.ResultMsg;

import java.io.IOException;

@RestController
public class KnowledgeController {
    @Autowired
    private KnowledgeServiceImpl knowledgeService;
    @GetMapping("/getKnowledge")
    public ResultMsg getKnowledge(@RequestParam String question) throws IOException, InterruptedException {
        return knowledgeService.getKnowledge(question);
    }
}
