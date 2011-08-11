(function($) {
	var results = biomart.renderer.results;
	
    /* HEATMAP */
    results.tmamap = Object.create(results.chart);
    results.tmamap.tagName = 'div';
    results.tmamap._heatColumn = 4;
    results.tmamap._max = 3;
    results.tmamap._min = 0;
    results.tmamap._mid = 1.5;
    results.tmamap._maxXY = [];
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
        var range = Math.abs(max - min);
        if(range === 0)
    		return 255;
        return 255 - parseInt(val / range * 255);
    };
    results.tmamap._getGreen = function(val, min, mid, max) {
    	var range = Math.abs(max - min);
    	if(range === 0)
    		return 255;
        return 255 - parseInt(val / range * 255);
    };
    results.tmamap._getRed = function(val, min, mid, max) {
    	var range = Math.abs(max - min);
    	if(range === 0)
    		return 255;
        return 255 - parseInt(val / range * 180);
    };
    results.tmamap.clear = function() {
        this._lines = [];
        this._labels = [];
        this._keyMap = {};
        this._maxXY = [];
    };
    results.tmamap.parse = function(rows, writee) {
        var n = rows.length,
            arr = [],
            currVal;
        if (!rows.length) return;
    	
    	// hard coded col value for now
    	var rowCancerType = 0, rowValue1 = 1, rowValue2 = 2, rowX = 3, rowID = 4, rowGeneID = 5, tmaName=6;
    	this._xaxisLabel = this._header[tmaName] + " " + rows[0][tmaName];
    	
		for (var i=0, row, rawKey, cleanedKey, index, n=rows.length; i<n; i++) {
			row = rows[i];
			rawKey = row[rowCancerType];
			
			index = i;

            var value1 = row[rowValue1],
        	value2 = row[rowValue2],
        	valueX = parseFloat(row[rowX]),
            valueID = parseFloat(row[rowID]);
            var avg = (parseFloat(value1) + parseFloat(value2))/2;
            
            if(rawKey in this._lines){
            }else{
            	this._lines[rawKey] = new Array();
            }
            if(rawKey in this._maxXY){
            	if(valueX > this._maxXY[rawKey][0]){
            		this._maxXY[rawKey][0] = valueX;
                }
                if(avg > this._maxXY[rawKey][1]){
                	this._maxXY[rawKey][1] = avg;
                }
            }else{
            	this._maxXY[rawKey] = [valueX, avg];
            }

            this._lines[rawKey].push({
            	x: valueX,
            	y: avg,
            	value: valueID
            });
            
		}
		
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
    results.tmamap.onMouseMove = function(event) {
    	
    	var a = event.pageX;
    	var b = event.pageY;
    	var content = '('+a +','+b+')';
    	//results.tmamap._showTooltip(a,b,content);
    	
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
        //make sure of max and min for tma map
        results.tmamap._max = 3;
        results.tmamap._min = 0;
        results.tmamap._mid = 1.5;
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
        
        
        this._plot = tmamap;
        tmacanvas = tmacanvas.get(0);
        x1=0; y1=0; x2=300 * this._lines.length; y2=600;
        tmacanvas.width = x2;
        tmacanvas.height = y2;
        this._element.css('width', x2 + 'px');
        this._element.css('height', y2 + 'px');
        
        if (typeof G_vmlCanvasManager != 'undefined')
        	tmacanvas = G_vmlCanvasManager.initElement(tmacanvas);
 
        tmacanvas.onmousemove = this.onMouseMove;
    	//draw TMA map
    	var radius = 15;
    	var gap = 1;
    	var scale = 40;
    	var numCat = 0;
    	var preX = 0;
    	var preY = 0;
    	var shift = 15;
    	var context = tmacanvas.getContext('2d');
        for(var category in this._lines){
        	if(this._lines.hasOwnProperty(category)){
        		//draw sector
        		context.fillStyle = "Black";
        		context.font = '18px Helvetica';
        		context.textBaseline = "top";
        		context.fillText("Sector "+ (numCat+1)
        				,(gap+preX)*scale*(Math.floor(numCat/2))+this._maxXY[category][0]/2*scale
        				,(gap+preY)*scale*(numCat%2));
        		//draw cols and rows
        		context.font = '10px Helvetica';
        		for(var col =1;col <= this._maxXY[category][1];col++){
        			context.fillText(col, shift + (gap+preX)*scale*(Math.floor(numCat/2)),col*scale+ (gap+preY)*scale*(numCat%2));
        		}
        		for(var row =1;row <= this._maxXY[category][0];row++){    				
    				context.fillText(row,row*scale+ (gap+preX)*scale*(Math.floor(numCat/2)),shift+(gap+preY)*scale*(numCat%2));
    			}
        		for(var data in this._lines[category]){
                	if(this._lines[category].hasOwnProperty(data)){
            			var x = this._lines[category][data].x * scale + (gap+preX)*scale*(Math.floor(numCat/2));
            			var y = this._lines[category][data].y * scale + (gap+preY)*scale*(numCat%2);
            			var value = this._lines[category][data].value;
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
        		preX = this._maxXY[category][0];
        		preY = this._maxXY[category][1];
        		numCat ++;
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
        
        if (this._xaxisLabel) {
            $(['<p class="plot-label">', this._xaxisLabel, '</p>'].join(''))
                .width(this._plot.width())
                .appendTo(this._element);
        }
        
        this.clear();
    };
})(jQuery);