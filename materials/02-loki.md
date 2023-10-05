# Loki

Loki is a horizontally scalable, highly available multi-user log aggregation system inspired by Prometheus. Loki is designed to be very cost-effective and easy to use.

Loki as well as Prometheus has a client-server architecture with a kernel in Loki that stores the logs of the whole system, and numerous agents - local metrics collectors (promtail) that pass the logs themselves to Loki.

To get logs from Loki, you need to use a query in a special language - LogQL, similar to PromQL.

The standard LogQL query consists of two parts: a selector and a filter. The selector is used to search for labels that index logs in Loki, and the filter is used to select a subset of the logs selected by the selector using a search query or a regular expression.

For example:

```
{cluster="ops-tools1",container="ingress-nginx"}
    | json
    | __error__ != "JSONParserErr"
```
where
`{cluster="ops-tools1",container="ingress-nginx"}` is a selector, and `| json` and `| __error__ != "JSONParserErr"` are filters.
