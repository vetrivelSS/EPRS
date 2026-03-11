package com.ScrapManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScrapService {

    @Autowired
    private ScrapRepository scrapRepository;

    public List<ScrapRecord> getAllScrapData() {
        return scrapRepository.findAll();
    }

    public ScrapRecord saveScrapRecord(ScrapRecord record) {
        return scrapRepository.save(record);
    }
}
