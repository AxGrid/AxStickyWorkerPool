![Sticky pool logo](logo.png)

Sticky Worker Pool

Создадим исполнителя

```java

@Service
public class MyWorkService implements AxWork {
    @Override
    public void execute(AxWorkerIndex workerIndex) throws InterruptedException {
        /*
         TODO: Исполняем задачу 
         Параметры исполнения например такие 
         Utils.crc32(session) % workerIndex.getCount() == workerIndex.getIndex()
        */      
    }
}

```


Создадим воркер пул

 ```java

@Service
public class MyStickyWorkerPoolService extends AxStickyWorkerPoolService {

    @Autowired
    protected MyStickyWorkerPoolService(MyWorkService workerClass) {
        // Укажем количество воркеров
        super(10, workerClass);
    }
    // ...
}

```

Отконфигурируем и запустим воркер пул

```java
@Service
public class MyStickyWorkerPoolService extends AxStickyWorkerPoolService {
    // ...
    @PostConstruct
    void start() {
        Map<String, AxWorkerConfiguration> workerConfigurations = new HashMap<>();
        workerConfigurations.put("w1", new AxWorkerConfiguration(5));
        workerConfigurations.put("w2", new AxWorkerConfiguration(3));
        this.rescale(workerConfigurations);
    }
}
```

