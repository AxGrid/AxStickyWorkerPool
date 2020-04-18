package com.axgrid.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Ротационная запись задачи
 */
@Data
public class AxWorkerIndex {
    /**
     * Слаг задачи
     */
    final String slug;

    /**
     * Индекс группы внутри задачи
     * Выбрать из списка можно например как
     * Utils.crc32(session) % getCount() == getIndex()
     */
    final int index;

    /**
     * Количество воркеров
     * @return число
     */
    public long getCount() { return configuration.getCount(); }

    /**
     * конфигурация
     */
    final AxWorkerConfiguration configuration;

    private final String key;

    @Override
    public String toString() { return key; }

    public AxWorkerIndex(String slug, int index, AxWorkerConfiguration configuration) {
        this.slug = slug;
        this.index = index;
        this.configuration = configuration;
        this.key = slug +"-"+ index;
    }


}
