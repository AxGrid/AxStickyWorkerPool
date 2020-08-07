package com.axgrid.worker.dto;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Коллекция ошибок, для обработки отключения задач
 * TODO: Подумать над ConcurrentHashMap
 */

public class AxErrorsCollection extends ConcurrentHashMap<String, List<Long>> {

    /**
     * Интервал, сколько вообще будут хранится каунтеры ошибок
     * (10 мин)
     */
    final static long storeTime = 600_000;

    /**
     * Добавить ошибку
     * @param slug слаг задачи
     */
    public void put(String slug) {
        this.compute(slug, (k, v) -> {
            if (v == null)
                return new ArrayList<>(Collections.singletonList(new Date().getTime()));
            else {
                long d = new Date().getTime();
                v.add(new Date().getTime());
                v.removeIf(i -> i < d - storeTime);
                return v;
            }
        });
    }

    /**
     * Узнать количество ошибок
     * @param slug слаг задачи
     * @param timeInterval интервал
     * @return количество ошибок в заданном интервале
     */
    public int getErrorCount(String slug, long timeInterval) {
        List<Long> errorsTimes = this.getOrDefault(slug, null);
        if (errorsTimes == null) return 0;
        errorsTimes = new ArrayList<>(errorsTimes);
        long currentTime = new Date().getTime();
        return (int)errorsTimes.stream().filter(item -> item > currentTime - timeInterval).mapToLong(item -> item).count();
    }

}
