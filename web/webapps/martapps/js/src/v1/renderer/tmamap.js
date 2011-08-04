(function($) {
	var results = biomart.renderer.results;
	
    /* HEATMAP */
    results.tmamap = Object.create(results.plain);
    results.tmamap.tagName = 'div';
    results.tmamap._heatColumn = 4;
    results.tmamap._max = 5.0;
    results.tmamap._min = -5.0;
    results.tmamap._mid = 0;
    results.tmamap._lines = [];
    results.tmamap._getColor = function(val) {
        var min = this._min,
            max = this._max,
            mid = this._mid;

        if (val > max) return 'rgb(255,255,255)';
        if (val < min) return 'rgb(255,255,255)';

        var r = this._getRed(val, min, mid, max),
            b = this._getBlue(val, min, mid, max),
            g = this._getGreen(val, min, mid, max);

        return ['rgb(', r, ',', g, ',', b, ')'].join('')
    };
    results.tmamap._getBlue = function(val, min, mid, max) {
        if (val >= 0) return 0;
        var range = Math.abs(min - mid);
        val = Math.abs(val - mid);
        return parseInt(val / range * 255);
    };
    results.tmamap._getGreen = function(val, min, mid, max) {
        if (val <= 0) return 0;
        var mid2 = (max + mid) / 2,
        val2 = Math.abs(mid2 - val);
        return 180 - parseInt(val2 / mid2 * 180);
    };
    results.tmamap._getRed = function(val, min, mid, max) {
        if (val <= 0) return 0;
        var mid2 = (max + mid) / 2;
        if (val >= mid2) return 255;
        return parseInt(val / mid2 * 255);
    };
    results.tmamap.clear = function() {
        this._lines = [];
        this._labels = [];
        this._keyMap = {};
    };
    results.tmamap.parse = function(rows, writee) {
        var n = rows.length,
            arr = [],
            currVal;
        if (!rows.length) return;
    	
    	// hard coded col value for now
    	var rowCancerType = 0, rowValue1 = 1, rowValue2 = 2, rowX = 3, rowID = 4, rowGeneID = 5;
    	this._xaxisLabel = this._header[rowGeneID] + " " + rows[0][rowGeneID];
    	this._max = parseFloat(rows[0][rowID]);
    	this._min = parseFloat(rows[0][rowID]);
    	
    	
		for (var i=0, row, rawKey, cleanedKey, index, n=rows.length; i<n; i++) {
			row = rows[i];
			rawKey = row[rowCancerType];
			
			index = i;

            var value1 = row[rowValue1],
        	value2 = row[rowValue2],
        	valueX = row[rowX],
            valueID = parseFloat(row[rowID]);
            var avg = (parseFloat(value1) + parseFloat(value2))/2;
            
            if(valueID > this._max){
            	this._max = valueID;
            }
            if(valueID < this._min){
            	this._min = valueID;
            }
            if(rawKey in this._lines){
            }else{
            	this._lines[rawKey] = new Array();
            }

            this._lines[rawKey].push({
            	x: valueX,
            	y: avg,
            	value: valueID
            });
            
		}
		
		this._mid = (this._max + this._min) / 2;
    };
    results.tmamap.setHighlightColumn = function(i) { this._highlight = i };
    results.tmamap.printHeader = function(header, writee) {
        var arr = [null, []];
        writee.addClass('clearfix');
        this._header = header;
        this._arr = [];
    };
    results.tmamap.option = function(name, value) {
        this['_' + name] = value;
    };
    results.tmamap._showTooltip = function(x, y, contents) {
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
    results.tmamap.draw = function(writee) {
        if (this._hasError) return;

        if (!this._lines || !this._lines.length) {
            writee.html(['<p class="empty">', _('no_results'), '</p>'].join(''));
            return;
        }

        if (!this._lines.length) {
            writee.parent().parent().html(['<p class="empty">', _('no_results'), '</p>'].join(''));
            return;
        }

        writee.find('div.heat-box').tipsy({
            fade: true,
            gravity: 'w',
            opacity: .9
        });

        // Use canvas to draw the legend
        var legend,
        	tmamap,
        	tmacanvas = $('<canvas id="tmacanvas"/>'),
            canvas = $('<canvas id="legend"/>'),
            ctx,
            grad,
            x1,
            y1,
            x2,
            x2,
            color1 = this._getColor(this._min),
            color2 = this._getColor(this._mid),
            color3 = this._getColor((this._mid+this._max)/2),
            color4 = this._getColor(this._max),
            grad,
            heading = this._header[this._heatColumn];

        writee
            .parent().addClass('clearfix')
            .find('div.heat-box') 
            .hover(function() {
                $(this).children('span.value').fadeIn(300);
            }, function() {
                $(this).children('span.value').fadeOut(300);
            });

        tmamap = $('<div class="tmamap"/>')
        	.append(tmacanvas)
        	.disableSelection()
        	.appendTo(writee);
        
        tmacanvas = tmacanvas.get(0);
        x1=0; y1=0; x2=400 * this._lines.length-1; y2=400;
        tmacanvas.width = x2;
        tmacanvas.height = y2;
        
        if (typeof G_vmlCanvasManager != 'undefined')
        	tmacanvas = G_vmlCanvasManager.initElement(tmacanvas);
 
    	
    	//draw TMA map
    	var radius = 15;
    	var gap = 2;
        for(var category in this._lines){
        	if(this._lines.hasOwnProperty(category)){
        		for(var data in this._lines[category]){
                	if(this._lines[category].hasOwnProperty(data)){
            			var x = this._lines[category][data].x * 40 + 450 * parseInt(category-1);
            			var y = this._lines[category][data].y * 40;
            			var value = this._lines[category][data].value;
            			var context = tmacanvas.getContext('2d');
            			// draw the TMA map dots
            			
            			
            			context.fillStyle = this._getColor(value);
            			context.strokeStyle = this._getColor(this._max);
            			
            			context.beginPath();
            			context.arc(x,y,radius, 0, Math.PI*2,true);
            			context.closePath();
	            		
            			context.fill();
            			context.stroke();
	            		
                	}
        		}
        	}
        }
    
        
        legend = $('<div class="heat-legend"/>')
            .append(canvas)
            .append(['<div class="max">', this._max, '</div>'].join(''))
            .append(['<div class="mid">', this._mid, '</div>'].join(''))
            .append(['<div class="min">', this._min, '</div>'].join(''))
            .append(['<p>', heading, '</p>'].join(''))
            .disableSelection();

        $('<div class="heat-legend-wrap"/>')
            .insertAfter(writee)
            .append(legend);        
        

        canvas = canvas.get(0);
        x1 = 0; y1 = 0; x2 = 200; y2 = 20;
        canvas.width = x2;
        canvas.height = y2;

        if (typeof G_vmlCanvasManager != 'undefined')
            canvas = G_vmlCanvasManager.initElement(canvas);

        
        if (canvas.getContext('2d')) {
            ctx = canvas.getContext('2d');
            //create gradient color bar
            grad = ctx.createLinearGradient(x1, y1, x2, y1);
            grad.addColorStop(0, color1);
            grad.addColorStop(.5, color2);
            grad.addColorStop(.75, color3);
            grad.addColorStop(1, color4);
            ctx.fillStyle = grad;
            ctx.fillRect(x1, y1, x2, y2);
        }
        
        this.clear();
    };
})(jQuery);