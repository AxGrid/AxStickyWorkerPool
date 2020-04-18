package com.axgrid.worker.service;

import com.axgrid.worker.Utils;
import com.axgrid.worker.dto.AxWork;
import com.axgrid.worker.dto.AxWorkerIndex;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Тсполнитель задач
 */
@Slf4j
@Service
public class TestWorkService implements AxWork {


    // Какие-то результирующие данные
    @Getter
    Map<String, Integer> result = new ConcurrentHashMap<>();



    @Getter
    Map<String, Set<String>> workedOn = new ConcurrentHashMap<>();

    // Типа задачи
    @Getter
    Map<String, Queue<Integer>> tasks = new ConcurrentHashMap<>();
    public boolean isEmpty() { return tasks.values().stream().allMatch(item->item.size() == 0); }

    final Random r = new Random(new Date().getTime());

    @Override
    public void execute(AxWorkerIndex workerIndex) throws InterruptedException {
        tasks.keySet().stream().filter((key) -> key.startsWith(workerIndex.getWorkerSlug()) && (Utils.crc32(key) % workerIndex.getConfiguration().getCount() == workerIndex.getWorkerIndex()))
                .forEach(key -> {
                    if (tasks.get(key) != null && tasks.get(key).size() > 0) {
                        workedOn.compute(key, (k,v) -> {
                                if (v == null)
                                    return new HashSet<>(Collections.singletonList(workerIndex.toString()));
                                else {
                                    v.add(workerIndex.toString());
                                    return v;
                                }
                        });

                        workedOn.getOrDefault(key, new HashSet<>()).add(workerIndex.toString());
                        Integer t = tasks.get(key).poll();
                        if (t == null) { log.warn("Value for key {} is null", key); return; }
                        result.compute(key, (k,v) -> v == null ? t : v + t);
                    }
                });
    }
}