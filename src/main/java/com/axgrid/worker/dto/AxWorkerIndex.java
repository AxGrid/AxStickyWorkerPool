package com.axgrid.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Ротационная запись задачи
 */
@Data
@AllArgsConstructor
public class AxWorkerIndex {
    /**
     * Слаг задачи
     */
    String workerSlug;

    /**
     * Индекс группы внутри задачи
     * Выбрать из списка можно например как
     * Utils.crc32(session) % configuration.workerCount == workerIndex
     */
    int workerIndex;

    /**
     * конфигурация
     */
    AxWorkerConfiguration configuration;

    @Override
    public String toString() { return workerSlug+"-"+workerIndex; }
}
