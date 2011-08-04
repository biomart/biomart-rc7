(function($) {
    var results = biomart.renderer.results;

    /* CHART */
    results.chart = Object.create(results.plain);
    results.chart.tagName = 'div';
    results.chart._keyMap = {};
    results.chart._lines = [];
    results.chart._lineIndices = [1];
    results.chart._labels = [];
    results.chart._max = 20;
    results.chart._header = null;
    results.chart.initExport = function(url) {
        var div = $([
                '<div class="chart-export clearfix">',
                    '<h6 class="chart-export-title">', _('export chart', biomart.CAPITALIZE), '<span class="ui-icon ui-icon-disk"/></h6>',
                '</div>'
            ].join('')),

            dialog,
            results = ['PNG', 'JPEG', 'TIFF', 'PDF', 'EPS', 'SVG'],
            arr = [],
            self = this,
            id = biomart.uuid(),
            id2 = biomart.uuid(),
            id3 = biomart.uuid();

        for (var i=0, f; f=results[i]; i++) {
            arr.push(['<option value="', f, '">', f, '</option>'].join(''));
        }

        dialog = $([
            '<div class="chart-export-dialog gradient-grey-reverse" style="display:none">',
                '<form target="_blank" method="GET" action="', BIOMART_CONFIG.service.url, 'chart/', url,'">',
                    '<label for="', id, '">Format: </label>',
                    '<select id="', id, '" name="f">',
                        arr.join(''),
                    '</select><br/>',
                    '<label for="', id2, '">Width: </label>',
                    '<input size="4" type="text" id="', id2, '" name="w" value="800"/>pixels<br/>',
                    '<label for="', id3, '">Height: </label>',
                    '<input size="4" type="text" id="', id3, '" name="h" value="', this._max*40, '"/>pixels<br/>',
                    '<input type="submit" value="', _('go', biomart.CAPITALIZE), '"/>',
                '</form>',
                '<p class="info">Select a format and press <em>Go</em> to generate the chart in a new window.</p>',
            '</div>'
        ].join(''));

        dialog.find('form').bind('submit.chart', function() {
            return self._doExport($(this));
        });

        div
            .hoverIntent({
                over: function() {
                    dialog.fadeIn(200);
                },
                out: function() {
                    dialog.fadeOut(200);
                },
                timeout: 500
            })
            .insertBefore(this._element)
            .append(dialog);

        this._exportDiv = div;
        this._exportDialog = dialog;
    };
    results.chart._doExport = function(form) {
        var data = [],
            lines = this._lines,
            n = Math.min(this._max, this._lines.length),
            ck = [],
            sl = this._header[0],
            sk = [];

        while (n--) {
            for (var j=0, m=lines[n].values.length; j<m; j++) {
                if (!data[j]) data[j] = [];
                data[j][n] = lines[n].values[j];
            }
            ck[n] = lines[n].key;
        }

        for (var i=1; i<this._header.length; i++) {
            sk.push(this._header[i]);
        }

        while (m--) {
            data[m] = data[m].join(',');
        }
        data = data.join('|');
        form.prepend(['<input type="hidden" name="d" value="', data, '"/>'].join(''));
        //form.prepend(['<input type="hidden" name="ck" value="', ck.join(','), '"/>'].join(''));
        form.prepend(['<input type="hidden" name="ck" value="', ck.join(String.fromCharCode(29)), '"/>'].join(''));
        form.prepend(['<input type="hidden" name="sl" value="', sl, '"/>'].join(''));
        form.prepend(['<input type="hidden" name="cl" value="', this._xaxisLabel, '"/>'].join(''));
        form.prepend(['<input type="hidden" name="sk" value="', sk.join(','), '"/>'].join(''));

        return true;
    };
    results.chart.clear = function() {
        this._lines = [];
        this._labels = [];
        this._keyMap = {};
    };
    results.chart.destroy = function() {
        // cleanup
        this.clear();
        this.tagName = 'div';
        this._lineIndices = [1];
        this._max = 20;
        this._header = null;
        if (this._exportDialog) this._exportDialog.remove();
        if (this._exportDiv) this._exportDiv.remove();
    };
    results.chart.printHeader = function(header, writee) {
        this._header = header;
        // Not all columns are returned, truncate our indices array
        if (header.length-1 < this._lineIndices.length) {
            this._lineIndices = this._lineIndices.slice(0 ,header.length-1);
        }
    };
    results.chart._prevGetElement = results.chart.getElement;
    results.chart.getElement = function() {
        this._element = this._prevGetElement();
        this._tooltip = $('<div class="chart-tooltip"/>').hide().appendTo(document.body);
        return this._element;
    };
    results.chart.parse = function(rows, writee) {
        if (!rows.length) return;
        for (var i=0, row, rawKey, cleanedKey, index, n=rows.length; i<n; i++) {
            row = rows[i];
            rawKey = row[0],
            cleanedKey = typeof rawKey == 'string' ? biomart.stripHtml(rawKey) : rawKey,
            index = this._keyMap[cleanedKey];

            if (typeof index == 'undefined') {
                index = this._keyMap[cleanedKey] = this._lines.length;
                this._lines[index] = {
                    key: cleanedKey,
                    raw: rawKey,
                    values: [],
                    totals: []
                };
            }

            for (var j=0, value, m=this._lineIndices.length; j<m; j++) {
                var prevVal, prevTotal;
                value = row[this._lineIndices[j]];
                if (typeof value == 'object') {
                    prevVal = this._lines[index].values[j] || 0;
                    prevTotal = this._lines[index].totals[j] || 0;
                    this._lines[index].values[j] = prevVal + (parseInt(value.count) || 0);
                    this._lines[index].totals[j] = prevTotal + (parseInt(value.total) || 0);
                } else {
                    prevVal = this._lines[index].values[j] || 0;
                    this._lines[index].values[j] = prevVal + (parseInt(value) || 0);
                }
            }
        }
    };
    results.chart._sort = function(index) {
        var self = this;
        this._lines.sort(function(left, right) {
            var total_r = 0,
                total_l = 0;
            for (var i=0, n=self._lineIndices.length; i<n; i++) {
                total_r += right.values[i ]|| 0;
                total_l += left.values[i] || 0;
            }
            if (total_r > total_l) return 1;
            else if (total_r < total_l) return -1;

            if (right.key > left.key) return 1;
            if (right.key < left.key) return -1;

            return 0;
        });
    };
    results.chart.draw = function() {
        if (!this._lines.length || this._hasError) return;
        this.initExport('bar/stacked');

        // sort by total
        this._sort(false);

        var topRows = this._lines.slice(0, this._max),
            x_options = this._getXOptions(topRows, false),
            chartLines = [],
            chartLabels = [];
        
        this._element.css('height', (topRows.length * 40 + 155) + 'px');
        this._attachEvents();

        for (var i=0, n=topRows.length, item; item=topRows[i]; i++) {
            for (var j=0, m=this._lineIndices.length; j<m; j++) {
                if (!chartLines[j]) chartLines[j] = { data: [], label: this._header[j+1] };
                chartLines[j].data[i] = [item.values[j] || 0, n-i];
            }
            chartLabels.push([n-i, item.raw]);
        }

        this._plot = $.plot(this._element, chartLines, {
            series: {
                stack: true,
                bars: {
                    align: 'center',
                    show: true,
                    horizontal: true,
                    lineWidth: 0,
                    fill: true,
                    barWidth: .6
                }
            },
            xaxis: x_options,
            yaxis: {
                min: 0,
                max: topRows.length + 3,
                ticks: chartLabels
            },
            grid: {
                clickable: true,
                hoverable: true,
                autoHighlight: true
            },
            legend: {
                margin: [5, 5],
                backgroundOpacity: .6,
                  show: true,
                position: 'ne'
            }
        });

        if (this._xaxisLabel) {
            $(['<p class="plot-label">', this._xaxisLabel, '</p>'].join(''))
                .width(this._plot.width())
                .appendTo(this._element);
        }
    };
    results.chart['export'] = function() {
    };
    results.chart._getXOptions = function(rows, index) {
        var largestValue = -Infinity,
            interval,
            x_ticks = [];

        for (var i=0, n=rows.length, item, value; item=rows[i]; i++) {
            if (index) {
                value = item.values[index];
            } else {
                value = 0;
                for (var j=0, m=this._lineIndices.length; j<m; j++) {
                    value += item.values[j];
                }
            }
            if (value > largestValue) largestValue = value;
        }

        largestValue = Math.max(5, largestValue);

        if (largestValue < 20) interval = 1;
        else if (largestValue < 50) interval = 5;
        else if (largestValue < 100) interval = 10;
        else interval = ~~(largestValue / 100) * 10;

        for (var i=0; i<=largestValue; i+=interval) {
            x_ticks.push([i,i]);
        }
        
        return {ticks: x_ticks, min: 0, max: largestValue+1};
    };
    results.chart._attachEvents = function() {
        var self = this;
        this._element.bind('plothover', function (ev, pos, item) {
            var total, content;
            if (item) {
                clearTimeout(self._t);
                if (this._prevPt != item.datapoint) {
                    this._prevPt = item.datapoint;

                    total = self._lines[item.dataIndex].totals[item.seriesIndex];

                    if (total) {
                        content = [
                            item.series.data[item.dataIndex][0], '/', 
                            self._lines[item.dataIndex].totals[item.seriesIndex]
                        ].join('');
                    } else {
                        content = item.series.data[item.dataIndex][0];
                    }

                    self._showTooltip(item.pageX, item.pageY, content);

                    self._t = setTimeout(function() {
                        self._tooltip.fadeOut(100);
                    }, 3000);
                }
            } else {
                self._tooltip.fadeOut(100);
            }
        });
    };
    results.chart._showTooltip = function(x, y, contents) {
        var left = x - this._tooltip.width() - 5,
            w = this._element.width(),
            pw = this._plot.width();
            diff = w - pw + this._element.offset().left;

        this._tooltip
            .text(contents)
            .css({
                'top': y - 6,
                left: diff < left ? left : x + 5
            })
            .fadeIn(100);
    };

    /* HISTOGRAM */
    results.histogram = Object.create(results.chart);
    results.histogram._counts  = [];
    results.histogram._ids = [];
    results.histogram._countCol = 2;
    results.histogram._totalCol = 3;
    results.histogram._idCol = 1;
    results.histogram._detailsUrl = null;
    results.histogram._lines = [];
    results.histogram.parse = function(rows, writee) {
        var max = Math.min(rows.length, this._max);
        for (var i=0, row, count, total; (row=rows[i]); i++) {
            if (i == max) break;
            count = parseInt(row[this._countCol]);
            total = parseInt(row[this._totalCol]);
            this._lines.push([count, max-i]);
            this._labels[max-i-1] = [max-i, row[0]];
            this._counts[max-i-1] = [ 
                count, '/', total
            ].join(''); // because jqplot will sort y values but not associated count labels
            this._ids[max-i-1] = row[this._idCol];
        }
    };
    results.histogram.printHeader = function(header, writee) {
        this._xLabel = ['Number of ', header[this._idCol]].join('');
        this._yLabel = header[0];
    };
    results.histogram._doExport = function(form) {
        var data = [],
            lines = this._lines,
            n = this._lines.length,
            ck = [];

        while (n--) {
            data[n] = this._lines[n][0];
            ck[n] = biomart.stripHtml(this._labels[n][1]);
        }

        data = data.join(',');
        form.prepend(['<input type="hidden" name="d" value="', data, '"/>'].join(''));
        form.prepend(['<input type="hidden" name="sl" value="', this._yLabel, '"/>'].join(''));
        form.prepend(['<input type="hidden" name="cl" value="', this._xaxisLabel, '"/>'].join(''));
        //form.prepend(['<input type="hidden" name="ck" value="', ck.join(','), '"/>'].join(''));
        //using group separator to separate ck values
        form.prepend(['<input type="hidden" name="ck" value="', ck.join(String.fromCharCode(29)), '"/>'].join(''));
    };
    results.histogram.draw = function(writee) {
        if (!this._lines.length) {
            writee.html(['<p class="empty">', _('no_results'), '</p>'].join(''));
            return;
        }
        if (this._hasError) return;

        this.initExport('bar');
        var x_options = this._getXOptions();

        this._attachEvents();

        this._element.css('height', (this._lines.length * 40 + 55) + 'px');

        this._plot = $.plot(this._element, [
            {
                data: this._lines,
                stack: null,
                bars: {
                    lineWidth: 0,
                    align: 'center',
                    show: true,
                    barWidth: .7,
                    horizontal: true
                }
            }
        ], {
            xaxis: x_options,
            yaxis: {
                min: 0,
                ticks: this._labels
            },
            grid: {
                clickable: true,
                hoverable: true,
                autoHighlight: true
            }
        });

        if (this._xaxisLabel) {
            $(['<p class="plot-label">', this._xaxisLabel, '</p>'].join(''))
                .width(this._plot.width())
                .appendTo(this._element);
        }
    };
    results.histogram._getXOptions = function() {
        var largestValue = this._lines[0][0],
            interval,
            x_ticks = [];

        if (largestValue < 20) interval = 1;
        else if (largestValue < 50) interval = 5;
        else if (largestValue < 100) interval = 10;
        else interval = ~~(largestValue / 100) * 10;

        for (var i=0; i<=largestValue; i+=interval) {
            x_ticks.push([i,i]);
        }
        
        return {ticks: x_ticks, min: 0, max: largestValue +1};
    };
    results.histogram._prevPt = null;
    results.histogram._t = null;
    results.histogram._attachEvents = function() {
        var self = this;
        this._element.bind('plothover', function (ev, pos, item) {
            if (item) {
                clearTimeout(self._t);
                if (this._prevPt != item.datapoint) {
                    this._prevPt = item.datapoint;

                    self._showTooltip(item.pageX, item.pageY, 
                            self._counts[self._counts.length-item.dataIndex-1], item.dataIndex);

                    self._t = setTimeout(function() {
                        self._tooltip.fadeOut(100);
                    }, 3000);
                }
            } else {
                self._tooltip.fadeOut(100);
            }
        });

        this._element.bind('plotclick', function (ev, pos, item) {
            if (item) {
                self._showDetails(self._labels[self._labels.length-item.dataIndex-1][1], 
                    self._ids[self._ids.length-item.dataIndex-1],
                    self._counts[self._counts.length-item.dataIndex-1], item.dataIndex);
            }
        });

        this._tooltip.bind('click.histogram', function() {
            var index = $(this).data('index');
            if (typeof index != 'undefined') {
                self._showDetails(self._labels[self._labels.length-index-1][1], 
                    self._ids[self._ids.length-index-1],
                    self._counts[self._counts.length-index-1], index);
            }
        });
    };
    results.histogram._showTooltip = function(x, y, contents, dataIndex) {
        var left = x - this._tooltip.width() - 5,
            w = this._element.width(),
            pw = this._plot.width();
            diff = w - pw + this._element.offset().left;

        this._tooltip
            .data('index', dataIndex)
            .text(contents + ' (click for details)')
            .css({
                'top': y - 6,
                left: diff < left ? left : x + 5
            })
            .fadeIn(100);
    };
    results.histogram._attrRegex = /\${.+?}/g;
    results.histogram._attrRegex2 = /\${(.+)}/;
    results.histogram._urlReplace = function(index) {
        var url = this._detailsUrl,
            matches = url.match(this._attrRegex),
            n = matches.length,
            match,
            actual;
        while (n--) {
            match = matches[n].match(this._attrRegex2)[1];
            if (match == 'dataset' ) {
                actual = this._rawData[index].dataset;
            } else {
                actual = biomart.stripHtml(this['_' + match][this['_' + match].length-index-1]);
            }
            url = url.replace(matches[n], actual);
        }
        return url.replace(/ /g, '');
    };
    results.histogram._showDetails = function(title, contents, counts, index) {
        var arr = [];
        if (this._detailsUrl) {
            arr.push(['<p style="float:right"><a rel="view-more" target="_blank" href="', this._urlReplace(index), '">', _('view_more_details', biomart.CAPITALIZE), '</a></p>'].join(''));
        }
        arr.push(['<p><strong>IDs returned for this dataset (', counts, '):</strong></p>'].join(''));
        arr.push(['<p class="ids">', contents, '</p>'].join(''));

        $('<div class="chart-details"/>').appendTo(document.body)
            .html(arr.join(''))
            .dialog({
                title: title,
                autoOpen: true,
                width: 500,
                height: 200,
                close: function() { $(this).dialog('destroy').remove() }
            });
    };
    
    /* SCATTER PLOT */
    results.scatterplot = Object.create(results.chart);
    results.scatterplot.tagName = 'div';
    results.scatterplot._keyMap = {};
    results.scatterplot._lines = [];
    results.scatterplot._lineIndices = [1];
    results.scatterplot._labels = [];
    results.scatterplot._max = 20;
    results.scatterplot._header = null;
    results.scatterplot.initExport = function(url) {};
    results.scatterplot._doExport = function(form) {};
    results.scatterplot.printHeader = function(header, writee) {
        this._header = header;
    };
    results.scatterplot.parse = function(rows, writee) {
    	if (!rows.length) return;
    	
    	// hard coded col value for now
    	var rowCancerType = 0, rowValue1 = 1, rowValue2 = 2, rowX = 3, rowID = 4, rowGeneID = 5;
    	this._xaxisLabel = this._header[rowGeneID] + " " + rows[0][rowGeneID];
        for (var i=0, row, rawKey, cleanedKey, index, n=rows.length; i<n; i++) {
            row = rows[i];
            rawKey = row[rowCancerType],
            cleanedKey = typeof rawKey == 'string' ? biomart.stripHtml(rawKey) : rawKey;
            
            /*this._lines.push({
                key: cleanedKey,
                raw: rawKey,
                values: [],
                totals: []
            });*/
            
            index = this._lines.length - 1;
           
            var value1 = row[rowValue1],
            	value2 = row[rowValue2],
            	valueX = row[rowX],
            	valueID = row[rowID];
            var avg = (parseFloat(value1) + parseFloat(value2))/2;
            
            if(rawKey in this._lines){
            	
            }else{
            	this._lines[rawKey] = [];
            }
            this._lines[rawKey].push( [parseInt(valueX) , avg , valueID] );
        }
	
    };
    results.scatterplot._attachEvents = function() {
    	var self = this;
        this._element.bind('plothover', function (ev, pos, item) {
        	if (item) {
                if (previousPoint != item.dataIndex) {
                    previousPoint = item.dataIndex;
                    clearTimeout(self._t);
                    //$("#tooltip").remove();
                    var x = item.datapoint[0].toFixed(2),
                        y = item.datapoint[1].toFixed(2),
                        tooltip = '';
                    
                    for( var index = 0; index< item.series.data.length; index++){
                    	var d = item.series.data[index];
                    	if(item.datapoint[0] === d[0] && item.datapoint[1] === d[1]){
                    		tooltip = d[2];
                    		break;
                    	}                    		
                    }
                    
                    self._showTooltip(item.pageX, item.pageY,
                                tooltip + " ("+x+","+y+")");
                    
                    self._t = setTimeout(function() {
                        self._tooltip.fadeOut(100);
                    }, 3000);
                }
            }
            else {
            	self._tooltip.fadeOut(100);
                //$("#tooltip").remove();
                previousPoint = null;            
            }
        
        });
    };
   
    results.scatterplot.draw = function() {
    	if (this._hasError) return;
        this.initExport('plot');

        // sort by total
        //this._sort(false);

        //var topRows = this._lines.slice(0, this._max);
        var rowCancerType = 0, rowValue1 = 1, rowValue2 = 2, rowX = 3, rowID = 4, rowGeneID = 5;
    	
		this._attachEvents();
		//set height for scatter plot render
		this._element.css('height', ( 200 + 155) + 'px');
		
        var chartLines = [];
        for (var key in this._lines) {
        	if(this._lines.hasOwnProperty(key)){
	        	chartLines.push({
	        		data : this._lines[key],
	        		label : key
	        	});
        	}
            //if (!chartLines[j]) chartLines[j] = { data: [] , label:''};
            //chartLines[0].data[j] = line.values;
            //chartLines[0].label = line.rawKey;
        }
        
        this._plot = $.plot(this._element, chartLines, {
            series: {
                points: { show: true }
            },
            xaxis: {},
            yaxis: {},
            grid: {
                clickable: true,
                hoverable: true,
                autoHighlight: true
            },
            legend: {
                margin: [5, 5],
                backgroundOpacity: .6,
                  show: true,
                position: 'ne'
            }
        });
        
        //var series = this._plot.getData();
        
        if (this._xaxisLabel) {
            $(['<p class="plot-label">', this._xaxisLabel, '</p>'].join(''))
                .width(this._plot.width())
                .appendTo(this._element);
        }
    };
    
    /* BOX PLOT */
    results.boxplot = Object.create(results.chart);
    results.boxplot.tagName = 'div';
    results.boxplot._keyMap = {};
    results.boxplot._lines = [];
    results.boxplot._lineIndices = [1];
    results.boxplot._labels = [];
    results.boxplot._max = 20;
    results.boxplot._header = null;
    results.boxplot.initExport = function(url) {};
    results.boxplot._doExport = function(form) {};
    results.boxplot.printHeader = function(header, writee) {
        this._header = header;
    };
    results.boxplot.parse = function(rows, writee) {
    	if (!rows.length) return;
    	
    	// hard coded col value for now
    	var rowCancerType = 0, rowValue1 = 1, rowValue2 = 2, rowX = 3, rowID = 4, rowGeneID = 5;
    	this._xaxisLabel = this._header[rowGeneID] + " " + rows[0][rowGeneID];
		for (var i=0, row, rawKey, cleanedKey, index, n=rows.length; i<n; i++) {
			row = rows[i];
			rawKey = row[rowCancerType];
			
			index = i;

            var value1 = row[rowValue1],
        	value2 = row[rowValue2],
        	valueX = row[rowX];
            var avg = (parseFloat(value1) + parseFloat(value2))/2;
            
            if(rawKey in this._lines){
            	if(valueX in this._lines[rawKey]){
            		
            	}else{
            		this._lines[rawKey][valueX] = {
                			Group : [],
                			boxValue : []
                	};
            	}
            }else{
            	this._lines[rawKey] = new Array();
            	this._lines[rawKey][valueX] = {
            			Group : [],
            			boxValue : []
            	};
            }

            this._lines[rawKey][valueX].Group.push(avg);
            
		}
		//calculate all values for box plot
		function sortNumber(a,b)
		{
			return a - b;
		}
		var index = 0;
		for( var key in this._lines){
			if(!this._lines.hasOwnProperty(key)){
				continue;
			}
			for( var xkey in this._lines[rawKey]){
				if(!this._lines[rawKey].hasOwnProperty(xkey)){
					continue;
				}
				this._lines[rawKey][xkey].Group.sort(sortNumber);
	    		var size = this._lines[rawKey][xkey].Group.length;
	    		
	    		index ++;
	    		if(size == 0){
	    			this._lines[rawKey][xkey].boxValue = [index, 0, 0 , 0, 0, 0];
	    		}else{
	    			this._lines[rawKey][xkey].boxValue = [index,
	    			                                   this._lines[rawKey][xkey].Group[0],
	    			                                   this._lines[rawKey][xkey].Group[Math.floor(size/4)],
	    			                                   this._lines[rawKey][xkey].Group[Math.floor(size/2)],
	    			                                   this._lines[rawKey][xkey].Group[Math.floor(size*3/4)],
	    			                                   this._lines[rawKey][xkey].Group[size-1]];
	    		}
	    		
			}
    		
		}
	
    };
   
    results.boxplot.draw = function() {
    	if (this._hasError) return;
        this.initExport('plot');

        // sort by total
        //this._sort(false);

        //var topRows = this._lines.slice(0, this._max);
        var rowCancerType = 0, rowValue1 = 1, rowValue2 = 2, rowX = 3, rowID = 4, rowGeneID = 5;
    	
		//set height for box plot render
		this._element.css('height', 500 + 'px');
		var chartLines = [];
		var xTicks = [];
		var index = 0;
		for( var key in this._lines){
			if(this._lines.hasOwnProperty(key)){
				var chartLine = {
		        		data : [],
		        		label : key
		        };
				for(var xkey in this._lines[key]){
					if(this._lines[key].hasOwnProperty(xkey)){
						chartLine.data.push(this._lines[key][xkey].boxValue);
						index ++;
						xTicks.push([index,xkey]);
					}
				}
    			chartLines.push(chartLine);
			}
		}
		
		
		
		var ymin = chartLines[0].data[0][1];
		var ymax = chartLines[0].data[0][5];
    	for(var i = 0; i < chartLines.length ; i++){
    		if( ymin > chartLines[i].data[0][1])
    			ymin = chartLines[i].data[0][1];
    		if( ymin > chartLines[i].data[1][1])
    			ymin = chartLines[i].data[1][1];
    		if( ymax < chartLines[i].data[0][5])
    			ymax = chartLines[i].data[0][5];
    		if( ymax < chartLines[i].data[1][5])
    			ymax = chartLines[i].data[1][5];
    	}

        this._plot = $.plot(this._element, chartLines, {
        	series : {
        		boxplot: {active : true, show : true}
        	},
            xaxis: {min: 0, max: index+1 , ticks: xTicks},
            yaxis: {min: ymin - 1, max: ymax + 1, ticks : 20},
            grid: {
                clickable: true,
                hoverable: true,
                autoHighlight: true
            },
            legend: {
                margin: [5, 5],
                backgroundOpacity: .6,
                  show: true,
                position: 'ne'
            }
        });
        
	
        if (this._xaxisLabel) {
            $(['<p class="plot-label">', this._xaxisLabel, '</p>'].join(''))
                .width(this._plot.width())
                .appendTo(this._element);
        }
    };
    
    /* DOT PLOT */
    results.dotplot = Object.create(results.chart);
    results.dotplot.tagName = 'div';
    results.dotplot._keyMap = {};
    results.dotplot._lines = [];
    results.dotplot._lineIndices = [1];
    results.dotplot._labels = [];
    results.dotplot._max = 20;
    results.dotplot._header = null;
    results.dotplot.initExport = function(url) {};
    results.dotplot._doExport = function(form) {};
    results.dotplot.printHeader = function(header, writee) {
        this._header = header;
    };
    results.dotplot.parse = function(rows, writee) {
    	if (!rows.length) return;
    	
    	// hard coded col value for now
    	var rowCancerType = 0, rowValue1 = 1, rowValue2 = 2, rowX = 3, rowID = 4, rowGeneID = 5;
    	this._xaxisLabel = this._header[rowGeneID] + " " + rows[0][rowGeneID];
		for (var i=0, row, rawKey, cleanedKey, index, n=rows.length; i<n; i++) {
			row = rows[i];
			rawKey = row[rowCancerType];
			
			index = i;

            var value1 = row[rowValue1],
        	value2 = row[rowValue2],
        	valueX = row[rowX],
            valueID = row[rowID];
            var avg = (parseFloat(value1) + parseFloat(value2))/2;
            
            if(rawKey in this._lines){
            	if(valueX in this._lines[rawKey]){
            		
            	}else{
            		this._lines[rawKey][valueX] = {
                			Group : [],
                			boxValue : []
                	};
            	}
            }else{
            	this._lines[rawKey] = new Array();
            	this._lines[rawKey][valueX] = {
            			Group : [],
            			boxValue : []
            	};
            }

            this._lines[rawKey][valueX].Group.push([avg, valueID]);
		}
	
    };
    results.dotplot._attachEvents = function() {
    	var self = this;
        this._element.bind('plothover', function (ev, pos, item) {
        	if (item) {
                if (previousPoint != item.dataIndex) {
                    previousPoint = item.dataIndex;
                    clearTimeout(self._t);
                    //$("#tooltip").remove();
                    var x = item.datapoint[0].toFixed(2),
                        y = item.datapoint[1].toFixed(2),
                        tooltip = '';
                    
                    for( var index = 0; index< item.series.data.length; index++){
                    	var d = item.series.data[index];
                    	if(item.datapoint[0] === d[0] && item.datapoint[1] === d[1]){
                    		tooltip = d[2];
                    		break;
                    	}                    		
                    }
                    
                    self._showTooltip(item.pageX, item.pageY,
                                tooltip + " ("+x+","+y+")");
                    
                    self._t = setTimeout(function() {
                        self._tooltip.fadeOut(100);
                    }, 3000);
                }
            }
            else {
            	self._tooltip.fadeOut(100);
                //$("#tooltip").remove();
                previousPoint = null;            
            }
        
        });
    };
    
    results.dotplot.draw = function() {
    	if (this._hasError) return;
        this.initExport('plot');

        // sort by total
        //this._sort(false);
        this._attachEvents();
        //var topRows = this._lines.slice(0, this._max);
        var rowCancerType = 0, rowValue1 = 1, rowValue2 = 2, rowX = 3, rowID = 4, rowGeneID = 5;
    	
		//set height for box plot render
		this._element.css('height', 500 + 'px');
		var chartLines = [];
		var xTicks = [];
		var index = 0;
		for( var key in this._lines){
			if(this._lines.hasOwnProperty(key)){
				var chartLine = {
		        		data : [],
		        		label : key
		        };
				for(var xkey in this._lines[key]){
					if(this._lines[key].hasOwnProperty(xkey)){
						index ++;
						for(var i = 0; i<this._lines[key][xkey].Group.length; i++){
							chartLine.data.push([index, this._lines[key][xkey].Group[i][0], this._lines[key][xkey].Group[i][1]]);
						}
						
						xTicks.push([index,xkey]);
					}
				}
    			chartLines.push(chartLine);
			}
		}

        this._plot = $.plot(this._element, chartLines, {
        	series: {
                points: { show: true }
            },
            xaxis: {min: 0, max: index+1 , ticks: xTicks},
            yaxis: {},
            grid: {
                clickable: true,
                hoverable: true,
                autoHighlight: true
            },
            legend: {
                margin: [5, 5],
                backgroundOpacity: .6,
                  show: true,
                position: 'ne'
            }
        });
        
	
        if (this._xaxisLabel) {
            $(['<p class="plot-label">', this._xaxisLabel, '</p>'].join(''))
                .width(this._plot.width())
                .appendTo(this._element);
        }
    };
    
    /* Bar chart */
    results.barchart = Object.create(results.chart);
    results.barchart.tagName = 'div';
    results.barchart._keyMap = {};
    results.barchart._lines = [];
    results.barchart._lineIndices = [1];
    results.barchart._labels = [];
    results.barchart._max = 20;
    results.barchart._header = null;
    results.barchart.initExport = function(url) {};
    results.barchart._doExport = function(form) {};
    results.barchart.printHeader = function(header, writee) {
        this._header = header;
    };
    results.barchart.parse = function(rows, writee) {
    	if (!rows.length) return;
    	
    	// hard coded col value for now
    	var rowCancerType = 0, rowValue1 = 1, rowValue2 = 2, rowX = 3, rowID = 4, rowGeneID = 5;
    	this._xaxisLabel = this._header[rowGeneID] + " " + rows[0][rowGeneID];
        for (var i=0, row, rawKey, cleanedKey, index, n=rows.length; i<n; i++) {
            row = rows[i];
            rawKey = row[rowCancerType],
            cleanedKey = typeof rawKey == 'string' ? biomart.stripHtml(rawKey) : rawKey;
            
            /*this._lines.push({
                key: cleanedKey,
                raw: rawKey,
                values: [],
                totals: []
            });*/
            
            index = this._lines.length - 1;
           
            var value1 = row[rowValue1],
            	value2 = row[rowValue2],
            	valueX = row[rowX],
            	valueID = row[rowID];
            var avg = (parseFloat(value1) + parseFloat(value2))/2;
            
            if(rawKey in this._lines){
            	
            }else{
            	this._lines[rawKey] = [];
            }
            this._lines[rawKey].push( [parseInt(valueX) , avg , valueID] );
        }
	
    };
    results.barchart._attachEvents = function() {
    	var self = this;
        this._element.bind('plothover', function (ev, pos, item) {
        	if (item) {
                if (previousPoint != item.dataIndex) {
                    previousPoint = item.dataIndex;
                    clearTimeout(self._t);
                    //$("#tooltip").remove();
                    var x = item.datapoint[0].toFixed(2),
                        y = item.datapoint[1].toFixed(2),
                        tooltip = '';
                    
                    for( var index = 0; index< item.series.data.length; index++){
                    	var d = item.series.data[index];
                    	if(item.datapoint[0] === d[0] && item.datapoint[1] === d[1]){
                    		tooltip = d[2];
                    		break;
                    	}                    		
                    }
                    
                    self._showTooltip(item.pageX, item.pageY,
                                tooltip + " ("+x+","+y+")");
                    
                    self._t = setTimeout(function() {
                        self._tooltip.fadeOut(100);
                    }, 3000);
                }
            }
            else {
            	self._tooltip.fadeOut(100);
                //$("#tooltip").remove();
                previousPoint = null;            
            }
        
        });
    };
   
    results.barchart.draw = function() {
    	if (this._hasError) return;
        this.initExport('plot');

        // sort by total
        //this._sort(false);

        //var topRows = this._lines.slice(0, this._max);
        var rowCancerType = 0, rowValue1 = 1, rowValue2 = 2, rowX = 3, rowID = 4, rowGeneID = 5;
    	
		this._attachEvents();
		//set height for scatter plot render
		this._element.css('height', ( 200 + 155) + 'px');
		
        var chartLines = [];
        for (var key in this._lines) {
        	if(this._lines.hasOwnProperty(key)){
	        	chartLines.push({
	        		data : this._lines[key],
	        		label : key
	        	});
        	}
            //if (!chartLines[j]) chartLines[j] = { data: [] , label:''};
            //chartLines[0].data[j] = line.values;
            //chartLines[0].label = line.rawKey;
        }
        
        this._plot = $.plot(this._element, chartLines, {
            series: {
            	stack: true,
                bars: { show: true }
            },
            xaxis: {},
            yaxis: {},
            grid: {
                clickable: true,
                hoverable: true,
                autoHighlight: true
            },
            legend: {
                margin: [5, 5],
                backgroundOpacity: .6,
                  show: true,
                position: 'ne'
            }
        });
        
        //var series = this._plot.getData();
        
        if (this._xaxisLabel) {
            $(['<p class="plot-label">', this._xaxisLabel, '</p>'].join(''))
                .width(this._plot.width())
                .appendTo(this._element);
        }
    };
    
    results.barchart.clear = function() {
        this._lines = [];
        this._labels = [];
        this._donorIds = [];
        this._keyMap = {};
    };
})(jQuery);
