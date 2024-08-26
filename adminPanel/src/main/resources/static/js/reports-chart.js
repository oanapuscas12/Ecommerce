document.addEventListener('DOMContentLoaded', function() {
    var charts = document.querySelectorAll('[data-chart-id]');

    charts.forEach(function(chartElement) {
        var chartId = chartElement.getAttribute('data-chart-id');
        var labelsElement = document.getElementById(chartId + '-first-data');
        var valuesElement = document.getElementById(chartId + '-second-data');
        var valuesElementNew = document.getElementById(chartId + '-second-data-new');
        var valuesElementReturning = document.getElementById(chartId + '-second-data-returning');
        var valuesElementInactive = document.getElementById(chartId + '-second-data-inactive');

        if (chartId === 'chart3') {
            if (!labelsElement || !valuesElementNew || !valuesElementReturning || !valuesElementInactive) {
                console.error('Required elements are missing for chart ID:', chartId);
                return;
            }

            try {
                var labelsData = labelsElement.value;
                var newValuesData = valuesElementNew.value;
                var returningValuesData = valuesElementReturning.value;
                var inactiveValuesData = valuesElementInactive.value;

                var labels = JSON.parse(labelsData || '[]');
                var newValues = JSON.parse(newValuesData || '[]');
                var returningValues = JSON.parse(returningValuesData || '[]');
                var inactiveValues = JSON.parse(inactiveValuesData || '[]');

                // Combine new, returning, and inactive values
                var totalNew = newValues.reduce((sum, value) => sum + value, 0);
                var totalReturning = returningValues.reduce((sum, value) => sum + value, 0);
                var totalInactive = inactiveValues.reduce((sum, value) => sum + value, 0);

                var dataPoints = [
                    { y: totalNew, name: "New Merchants" },
                    { y: totalReturning, name: "Returning Merchants" },
                    { y: totalInactive, name: "Inactive Merchants" }
                ];

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
                        toolTipContent: "{name}: <strong>{y}</strong>",
                        indexLabel: "{name} - {y}",
                        dataPoints: dataPoints
                    }]
                });
                chart.render();

            } catch (e) {
                console.error('Error parsing JSON data for chart ID:', chartId, e);
            }

        } else {
            // Existing code for chart1 and chart2
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
                        toolTipContent: "{name}: <strong>{y}</strong>",
                        indexLabel: "{name} - {y}",
                        dataPoints: dataPoints
                    }]
                });
                chart.render();

            } catch (e) {
                console.error('Error parsing JSON data for chart ID:', chartId, e);
            }
        }
    });
});

function explodePie(e) {
    var dataPoint = e.dataSeries.dataPoints[e.dataPointIndex];
    dataPoint.exploded = !dataPoint.exploded;

    e.chart.render();
}