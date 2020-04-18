package com.axgrid.worker.dto;

/**
 * Исполнитель задачи
 */
public interface AxWork {
    /**
     * Выполнить задачу
     * @param workerIndex параметры шарда-задачи
     * @throws InterruptedException
     */
    void execute(AxWorkerIndex workerIndex) throws InterruptedException;
}
