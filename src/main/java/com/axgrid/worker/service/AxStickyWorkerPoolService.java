package com.axgrid.worker.service;

import com.axgrid.worker.dto.AxErrorsCollection;
import com.axgrid.worker.dto.AxWork;
import com.axgrid.worker.dto.AxWorkerConfiguration;
import com.axgrid.worker.dto.AxWorkerIndex;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Сервис организации пула исполнения задач
 */
@Slf4j
public abstract class AxStickyWorkerPoolService implements HealthIndicator {

    /**
     * Воркер
     */
    private class Worker implements Runnable {
        final AxStickyWorkerPoolService parent;
        final int index;

        @Override
        public void run() {
            while(parent.enable) {
                AxWorkerIndex currentWork = null;
                try {
                    if (parent.configurations != null) {
                        if (log.isDebugEnabled()) log.debug("Stop worker {} for reconfiguring...", index);
                        break;
                    }
                    currentWork = parent.workerIndexBlockingQueue.take();
                    if (parent.errors.getErrorCount(currentWork.getSlug(), currentWork.getConfiguration().getDisableTaskTimeout()) <= currentWork.getConfiguration().getDisableTaskAtErrorsCount()) {
                        parent.work.execute(currentWork);
                    }
                }catch (InterruptedException ignore) {
                    break;
                }catch (Exception e) {
                    if (log.isWarnEnabled()) log.warn("Exception in worker", e);
                    if (currentWork != null) {
                        parent.errors.put(currentWork.getSlug());
                    }
                }

                if (currentWork != null) {
                    try {
                        parent.workerIndexBlockingQueue.put(currentWork);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
            runningWorkers.decrementAndGet();
        }

        public Worker(int index, AxStickyWorkerPoolService parent) {
            this.index = index;
            this.parent = parent;
        }


    }

    /**
     * Имя пула
     */
    final String name;

    /**
     * Количество запущенных воркеров
     */
    final AtomicInteger runningWorkers = new AtomicInteger();

    /**
     * Коллекция ошибок
     */
    final AxErrorsCollection errors = new AxErrorsCollection();

    final ExecutorService executorService;

    /**
     * Очередь заданий для воркеров
     */
    final BlockingQueue<AxWorkerIndex> workerIndexBlockingQueue = new LinkedBlockingDeque<>();

    /**
     * Кол-во воркеров
     */
    final int workerCount;

    /**
     * Исполнитель
     */
    final AxWork work;

    boolean enable = false;

    /**
     * Флаг изменения конфигурации
     */
    Map<String, AxWorkerConfiguration> configurations;

    public int getWorkerCount() {
        return this.runningWorkers.get();
    }

    /**
     * Создает воркеры
     */
    private void startWorkers() {
        this.enable = true;
        for(int i=0;i<workerCount;i++) {
            Worker w = new Worker(runningWorkers.getAndIncrement(), this);
            executorService.execute(w);
        }
    }

    /**
     * Изменить конфигурацию пула
     * @param configurations новая конфигурация
     */
    public void rescale(Map<String, AxWorkerConfiguration> configurations) {
        this.configurations = configurations;
    }



    @Scheduled(fixedDelay = 1000)
    private void rescale() throws InterruptedException {
        // Если есть новая конфигурация, и все воркуеры остановлены
        if (configurations == null || runningWorkers.get() > 0) return;
        if (log.isDebugEnabled()) log.debug("Rescaling worker pool {}", this.name);
        // Очистим очередь привязок
        workerIndexBlockingQueue.clear();
        // Создадим заново очередь привязок
        for(Map.Entry<String, AxWorkerConfiguration> kv : configurations.entrySet()){
            for(int index = 0; index < kv.getValue().getCount(); index++){
                if (log.isDebugEnabled()) log.debug("Create task:{}/{} = {}",kv.getKey(), index, kv.getValue());
                workerIndexBlockingQueue.put(new AxWorkerIndex(kv.getKey(), index, kv.getValue()));
            }
        }
        this.configurations = null;
        startWorkers();
    }

    /**
     * Конструктор
     * @param workerCount количество воркеров
     * @param work исполнитель
     */
    protected AxStickyWorkerPoolService(int workerCount, AxWork work) {
        this.workerCount = workerCount;
        this.work = work;
        executorService = Executors.newFixedThreadPool(workerCount);
        this.name = this.getClass().getSimpleName();
        if (log.isDebugEnabled()) log.debug("Worker pool {} started", this.name);
    }

    @Override
    public Health health() {
        return this.runningWorkers.get() == workerCount ?
                Health.up().build() :
                Health.down().withDetail("Workers:", String.format("%d/%d", this.runningWorkers.get(), workerCount)).build();
    }
}
