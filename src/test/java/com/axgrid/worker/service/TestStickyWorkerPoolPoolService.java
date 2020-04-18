package com.axgrid.worker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Создадим воркер пул
 */
@Service
public class TestStickyWorkerPoolPoolService extends AxStickyWorkerPoolService {

    @Autowired
    protected TestStickyWorkerPoolPoolService(TestWorkService workerClass) {
        super(10, workerClass);
    }
}
