package com.productionCreation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductionResultService {

    @Autowired
    private ProductionResultRepository productionResultRepository;

    public void deleteProductionResult(Integer id) {
        productionResultRepository.deleteById(id);
    }
}