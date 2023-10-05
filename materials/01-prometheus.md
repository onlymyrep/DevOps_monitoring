# Prometheus

Prometheus is an open-source system monitoring and alerting toolset originally created by SoundCloud. Since its creation in 2012, many companies and organizations have started using Prometheus, and thanks to its open source code, it finds high support among developers. Prometheus contains a database of metrics in the form of a time series database.

Metrics are numerical measurements. A time series means that changes are recorded over time. What users want to measure differs from application to application. For a web server it might be the request time, for a database it might be the number of active connections or the number of active requests, etc.

Metrics play an important role in understanding why an application performs a certain way. Suppose you run a web application and find that it runs slowly. You need some information to know what is going on with your application. For example, the application may be slow if the number of requests is high. If you have a metric for the number of requests, you can determine the cause and increase the number of servers to handle the load.

Usually Prometheus consists of several main components:

- Prometheus Server - system kernel;
- metrics collection agents or exporters;
- client libraries for collecting application metrics (separated by programming languages);
- Alert Manager, which manages the monitoring of critical events in the system.

Prometheus Server aggregates all the metrics collected in the system and provides them through queries in PromQL language. These metrics can be external to the application (collected by exporters such as node_exporter) or internal, collected directly in the application using client libraries.

**Major exporters:**

1. Node exporter - to collect metrics from machines or nodes in a cluster.

2. cAdvisor - to collect metrics from docker containers

3. blackbox exporter - to collect metrics for HTTP, HTTPS, DNS, TCP, ICMP and gRPC endpoint availability.

Application metrics are collected via a special library, such as Micrometer for Java.

Prometheus supports three types of metrics.

| Type | Purpose | Example |
|-------------|------------|----------|
| Gauge | Measurement of resource use, capacity, etc. Values that can increase and decrease, and which have fixed upper limits | Collection size, number of threads running, number of messages in queue, memory usage |
| Counter | The measurement of a series of events or actions is a value that only increases, not decreases. | The total number of processed orders, the total number of completed tasks, etc. |
| Timer | Measurement of short-term events and their frequency | Method execution time, query execution time |

Prometheus has its own configuration file in yml format. The simplest Prometheus configuration file can look like this

```yml
global:
  scrape_interval:     15s              # the time in which Prometheus collects metrics
  evaluation_interval: 15s              # the time in which Prometheys calculates metrics
  scrape_timeout: 10s                   # the time after which when you try to collect the metric, it will be considered as not received

scrape_configs:
  - job_name: 'spring boot scrape'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
    static_configs:
      - targets: ['localhost:8080']     # a host for reading metrics
```

## PromQL

Prometheus query language is PromQL (Prometheus Query Language). PromQL allows the user to query and aggregate the time series of metrics stored in the Prometheus database.

PromQL contains many operations, here are some of them:

- Select metrics: you can select one or more metrics to query. Example: up{job="prometheus"}

- Aggregation: you can aggregate time series metrics using different functions, such as sum(), avg(), max(), min() and others. Example: sum(rate(http_requests_total{job="my-webapp"}[5m])) by (instance)

- Performing mathematical operations: you can perform mathematical operations with metrics, such as addition, subtraction, multiplication, and division. Example: rate(http_requests_total{job="my-webapp"}[5m]) / count(node_cpu_seconds_total{mode="idle"})

- Filtering: you can filter metrics using comparison operators such as =, !=, <, >, <=, >= and others. Example: http_requests_total{status_code="500"}

- Grouping: you can group a time series of metrics by specific labels using the keyword by. Example: sum(rate(http_requests_total{job="my-webapp"}[5m])) by (instance)
