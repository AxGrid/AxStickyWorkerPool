package com.axgrid.worker.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AxWorkerConfiguration {
    /**
     * Количество воркеров
     */
    int count = 4;
    /**
     * Через какое количество ошибок нужно исключить задачу
     */
    int disableTaskAtErrorsCount = 10;
    /**
     * На сколько нужно исключить задачу
     */
    int disableTaskTimeout = 3_000;



    public AxWorkerConfiguration(int count) {
        this.count = count;
    }
}
