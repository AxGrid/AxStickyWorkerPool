package com.axgrid.worker;

import com.axgrid.worker.service.TestStickyWorkerPoolPoolService;
import com.axgrid.worker.dto.AxWorkerConfiguration;
import com.axgrid.worker.service.TestWorkService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest()
@Import(TestApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "debug=true")
@Slf4j
public class WorkerPoolTest {

    @Autowired
    TestStickyWorkerPoolPoolService workerService;

    @Autowired
    TestWorkService ws;



    @Test
    public void testCreateWorkers() throws Exception {
        // Конфигурация
        Map<String, AxWorkerConfiguration> workerConfigurations = new HashMap<>();
        workerConfigurations.put("w1", new AxWorkerConfiguration(5));
        workerConfigurations.put("w2", new AxWorkerConfiguration(3));
        ws.clear();
        // Задачи
        Map<String, Queue<Integer>> tasks = ws.getTasks();
        // Сессии пользователей
        List<String> sessionsW1 = IntStream.range(0,10).boxed().map(item -> UUID.randomUUID().toString()).collect(Collectors.toList());
        int w1Count = 1500;
        // Создадим задачи пользователей
        for(int i=0;i<w1Count;i++) {
            String session = "w1-" + sessionsW1.get(i % sessionsW1.size());
            Queue<Integer> sessionTask = tasks.getOrDefault(session, new PriorityQueue<>());
            sessionTask.add(1);
            tasks.put(session, sessionTask);
        }

        // Проделаем все для второй настройки
        List<String> sessionsW2 = IntStream.range(0,20).boxed().map(item -> UUID.randomUUID().toString()).collect(Collectors.toList());
        int w2Count = 4000;
        for(int i=0;i<w2Count;i++) {
            String session = "w2-" + sessionsW2.get(i % sessionsW2.size());
            Queue<Integer> sessionTask = tasks.getOrDefault(session, new PriorityQueue<>());
            sessionTask.add(1);
            tasks.put(session, sessionTask);
        }

        workerService.rescale(workerConfigurations);
        for(int i=0;i<1000;i++) {
            Thread.sleep(10);
            if (ws.isEmpty()) break;
        }
        log.info("Done");
        Assert.assertTrue(ws.isEmpty());

        // Каждая задача только на одном воркере
        Assert.assertTrue(ws.getWorkedOn().values().stream().allMatch(item -> item.size() == 1));
        // Количество совпадает с пользователями
        Assert.assertEquals(sessionsW1.size() + sessionsW2.size(), ws.getWorkedOn().size());
        // Количество результатов верное
        Assert.assertEquals(sessionsW1.size() + sessionsW2.size(), ws.getResult().size());

        // Результаты верные
        Assert.assertEquals(w1Count, ws.getResult().entrySet().stream()
                .filter((kv) -> kv.getKey().startsWith("w1-"))
                .mapToInt(Map.Entry::getValue)
                .sum()
        );

        Assert.assertEquals(w2Count, ws.getResult().entrySet().stream()
                .filter((kv) -> kv.getKey().startsWith("w2-"))
                .mapToInt(Map.Entry::getValue)
                .sum()
        );

    }


    @Test
    public void testDisableWorkerAtError() throws Exception {
        Map<String, AxWorkerConfiguration> workerConfigurations = new HashMap<>();
        workerConfigurations.put("w5", new AxWorkerConfiguration(5));
        workerConfigurations.put("w3", new AxWorkerConfiguration(5));
        ws.clear();
        workerService.rescale(workerConfigurations);
        TestWorkService.throwW3Error = true;

        Map<String, Queue<Integer>> tasks = ws.getTasks();
        List<String> sessionsW1 = IntStream.range(0,10).boxed().map(item -> UUID.randomUUID().toString()).collect(Collectors.toList());
        int w1Count = 1500;
        // Создадим задачи пользователей
        for(int i=0;i<w1Count;i++) {
            String session = "w5-" + sessionsW1.get(i % sessionsW1.size());
            Queue<Integer> sessionTask = tasks.getOrDefault(session, new PriorityQueue<>());
            sessionTask.add(1);
            tasks.put(session, sessionTask);
        }

        // Проделаем все для второй настройки
        List<String> sessionsW2 = IntStream.range(0,20).boxed().map(item -> UUID.randomUUID().toString()).collect(Collectors.toList());
        int w2Count = 35;
        for(int i=0;i<w2Count;i++) {
            String session = "w3-" + sessionsW2.get(i % sessionsW2.size());
            Queue<Integer> sessionTask = tasks.getOrDefault(session, new PriorityQueue<>());
            sessionTask.add(1);
            tasks.put(session, sessionTask);
        }

        for(int i=0;i<1000;i++) {
            Thread.sleep(10);
            if (ws.isEmpty()) break;
        }

        log.info("{} {}", sessionsW1.size(), ws.getWorkedOn().size());
        // Количество совпадает с пользователями
        Assert.assertEquals(sessionsW1.size(), ws.getWorkedOn().size());
        // Количество результатов верное
        Assert.assertEquals(sessionsW1.size(), ws.getResult().size());

        // Результаты верные
        Assert.assertEquals(w1Count, ws.getResult().entrySet().stream()
                .filter((kv) -> kv.getKey().startsWith("w5-"))
                .mapToInt(Map.Entry::getValue)
                .sum()
        );
        Assert.assertFalse(ws.isEmpty());
        TestWorkService.throwW3Error = false;

        for(int i=0;i<1000;i++) {
            Thread.sleep(10);
            if (ws.isEmpty()) break;

        }

        // Каждая задача только на одном воркере
        Assert.assertTrue(ws.getWorkedOn().values().stream().allMatch(item -> item.size() == 1));
        // Количество совпадает с пользователями
        Assert.assertEquals(sessionsW1.size() + sessionsW2.size(), ws.getWorkedOn().size());
        // Количество результатов верное
        Assert.assertEquals(sessionsW1.size() + sessionsW2.size(), ws.getResult().size());

        // Результаты верные
        Assert.assertEquals(w1Count, ws.getResult().entrySet().stream()
                .filter((kv) -> kv.getKey().startsWith("w5-"))
                .mapToInt(Map.Entry::getValue)
                .sum()
        );

        Assert.assertEquals(w2Count, ws.getResult().entrySet().stream()
                .filter((kv) -> kv.getKey().startsWith("w3-"))
                .mapToInt(Map.Entry::getValue)
                .sum()
        );

        Assert.assertTrue(ws.isEmpty());
    }

}
