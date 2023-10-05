# Grafana

Grafana is one of the most popular tools for visualizing and analyzing metrics and application logs and their infrastructures. Grafana visualizes metrics using widgets (graphs, bars, numbers, tables, and so on) formed into dashboards.

Grafana uses data source-provided query languages to extract series of metrics and logs, which are visualized as widgets.

Grafana provides a set of standard widgets, such as:

**Graphs & charts**

- Time series - standard graphs.
- State timeline - graph with state changes over time.
- Status history - graph with periodic state over time.
- Bar chart - histogram by category.
- Histogram - histogram by groups of values.
- Heatmap - a two-dimensional matrix that shows the magnitude of the phenomenon as color in two dimensions.
- Pie chart
- Candlestick

**Stats & numbers**

- Stat for big stats and optional sparkline.
- Bar gauge is a horizontal or vertical bar gauge.

**Misc**

- Table.
- Logs.
- Node Graph for directed graphs.

## Grafana configuration

The Grafana configuration file is in /etc/grafana/grafana.ini (when installing from packages) or /usr/share/grafana/conf/ (when installing from the source). Here are examples of settings that might be in a Grafana configuration file:

```
# http port
http_port = 3000

# type of the database that stores metrics
database_type = mysql

# database connection parameters
[database]
host = localhost
name = grafana
user = grafana
password = mypassword

# Authorization and authentication settings
[auth]
disable_login_form = false
disable_signout_menu = false
login_remember_days = 7

# mail notification settings
[smtp]
enabled = true
host = smtp.gmail.com:587
user = myemail@gmail.com
password = mypassword
from_address = myemail@gmail.com
```

These are just a few examples of settings that can be included in a Grafana configuration file. Depending on your needs, you may have other settings and parameters. You can find more information about Grafana configuration files in the official Grafana documentation.
