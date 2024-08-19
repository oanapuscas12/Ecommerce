document.addEventListener('DOMContentLoaded', function() {
    var charts = document.querySelectorAll('[data-chart-id]');

    charts.forEach(function(chartElement) {
        var chartId = chartElement.getAttribute('data-chart-id');
        var labelsElement = document.getElementById(chartId + '-first-data');
        var valuesElement = document.getElementById(chartId + '-second-data');

        if (!labelsElement || !valuesElement) {
            console.error('Required elements are missing for chart ID:', chartId);
            return;
        }

        try {
            var labelsData = labelsElement.value;
            var valuesData = valuesElement.value;

            var labels = JSON.parse(labelsData || '[]');
            var values = JSON.parse(valuesData || '[]');

            if (labels.length !== values.length) {
                console.error('Mismatch between labels and values data lengths for chart ID:', chartId);
                return;
            }

            var dataPoints = labels.map(function(label, index) {
                return {
                    y: parseFloat(values[index]) || 0,
                    name: label
                };
            });

            var chart = new CanvasJS.Chart(chartElement.id, {
                exportEnabled: true,
                animationEnabled: true,
                title: {
                    text: chartElement.getAttribute('data-chart-title') || "Data Overview"
                },
                legend: {
                    cursor: "pointer",
                    itemclick: explodePie
                },
                data: [{
                    type: "pie",
                    showInLegend: true,
                    toolTipContent: "{name}: <strong>{y}%</strong>",
                    indexLabel: "{name} - {y}%",
                    dataPoints: dataPoints
                }]
            });
            chart.render();

        } catch (e) {
            console.error('Error parsing JSON data for chart ID:', chartId, e);
        }
    });
});

function explodePie(e) {
    var dataPoint = e.dataSeries.dataPoints[e.dataPointIndex];
    dataPoint.exploded = !dataPoint.exploded;

    e.chart.render();
}
