/**
 * KineticJS 2d JavaScript Library v1.0.0
 * http://www.kineticjs.com/
 * Copyright 2011, Eric Rowell
 * Licensed under the MIT or GPL Version 2 licenses.
 * Date: July 31 2011
 *
 * Copyright (C) 2011 by Eric Rowell
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
var Kinetic_2d = function(canvasId){
    this.canvas = document.getElementById(canvasId);
    this.context = this.canvas.getContext("2d");
    this.drawStage = undefined;
    this.listening = false;
    
    // Canvas Events 
    this.mousePos = null;
    this.mouseDown = false;
    this.mouseUp = false;
    
    // Region Events
    this.currentRegion = null;
    this.regionCounter = 0;
    this.lastRegionIndex = null;
    
    // Animation 
    this.t = 0;
    this.timeInterval = 0;
    this.startTime = 0;
    this.lastTime = 0;
    this.frame = 0;
    this.animating = false;
    
    // provided by Paul Irish
    window.requestAnimFrame = (function(callback){
        return window.requestAnimationFrame ||
        window.webkitRequestAnimationFrame ||
        window.mozRequestAnimationFrame ||
        window.oRequestAnimationFrame ||
        window.msRequestAnimationFrame ||
        function(callback){
            window.setTimeout(callback, 1000 / 60);
        };
    })();
};

// ======================================= GENERAL =======================================

Kinetic_2d.prototype.getContext = function(){
    return this.context;
};

Kinetic_2d.prototype.getCanvas = function(){
    return this.canvas;
};

Kinetic_2d.prototype.clear = function(){
    this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
};

Kinetic_2d.prototype.getCanvasPos = function(){
    var obj = this.getCanvas();
    var top = 0;
    var left = 0;
    while (obj.tagName != "BODY") {
        top += obj.offsetTop;
        left += obj.offsetLeft;
        obj = obj.offsetParent;
    }
    return {
        top: top,
        left: left
    };
};

// ======================================= STAGE =======================================

Kinetic_2d.prototype.setDrawStage = function(func){
    this.drawStage = func;
    this.listen();
};
Kinetic_2d.prototype.drawStage = function(){
    if (this.drawStage !== undefined) {
        this.drawStage();
    }
};

// ======================================= CANVAS EVENTS =======================================

Kinetic_2d.prototype.reset = function(evt){
    this.setMousePosition(evt);
    this.regionCounter = 0;
    
    if (!this.animating && this.drawStage !== undefined) {
        this.drawStage();
    }
    
    this.mouseDown = false;
    this.mouseUp = false;
};

Kinetic_2d.prototype.isMousedown = function(){
    return this.mouseDown;
};
Kinetic_2d.prototype.isMouseup = function(){
    return this.mouseUp;
};

Kinetic_2d.prototype.listen = function(){
    // store current listeners
    var that = this;
    var canvasOnmouseover = this.canvas.onmouseover;
    var canvasOnmouseout = this.canvas.onmouseout;
    var canvasOnmousemove = this.canvas.onmousemove;
    var canvasOnmousedown = this.canvas.onmousedown;
    var canvasOnmouseup = this.canvas.onmouseup;
    
    if (this.drawStage !== undefined) {
        this.drawStage();
    }
    
    this.canvas.onmouseover = function(e){
        if (!e) {
            e = window.event;
        }
        
        that.setMousePosition(e);
        if (typeof(canvasOnmouseover) == typeof(Function)) {
            canvasOnmouseover(e);
        }
    };
    this.canvas.onmouseout = function(){
        that.mousePos = null;
        if (typeof(canvasOnmouseout) == typeof(Function)) {
            canvasOnmouseout(e);
        }
    };
    this.canvas.onmousemove = function(e){
        if (!e) {
            e = window.event;
        }
        that.reset(e);
        
        if (typeof(canvasOnmousemove) == typeof(Function)) {
            canvasOnmousemove(e);
        }
    };
    this.canvas.onmousedown = function(e){
        if (!e) {
            e = window.event;
        }
        that.mouseDown = true;
        that.reset(e);
        
        if (typeof(canvasOnmousedown) == typeof(Function)) {
            canvasOnmousedown(e);
        }
    };
    this.canvas.onmouseup = function(e){
        if (!e) {
            e = window.event;
        }
        that.mouseUp = true;
        that.reset(e);
        
        if (typeof(canvasOnmouseup) == typeof(Function)) {
            canvasOnmouseup(e);
        }
    };
};

Kinetic_2d.prototype.getMousePos = function(evt){
    return this.mousePos;
};
Kinetic_2d.prototype.setMousePosition = function(evt){
    var mouseX = evt.clientX - this.getCanvasPos().left + window.pageXOffset;
    var mouseY = evt.clientY - this.getCanvasPos().top + window.pageYOffset;
    this.mousePos = {
        x: mouseX,
        y: mouseY
    };
};

// ======================================= REGION EVENTS =======================================

Kinetic_2d.prototype.beginRegion = function(){
    this.currentRegion = {};
    this.regionCounter++;
};
Kinetic_2d.prototype.addRegionEventListener = function(type, func){
    if (type == "onmouseover") {
        this.currentRegion.onmouseover = func;
    }
    else if (type == "onmouseout") {
        this.currentRegion.onmouseout = func;
    }
    else if (type == "onmousemove") {
        this.currentRegion.onmousemove = func;
    }
    else if (type == "onmousedown") {
        this.currentRegion.onmousedown = func;
    }
    else if (type == "onmouseup") {
        this.currentRegion.onmouseup = func;
    }
};
Kinetic_2d.prototype.closeRegion = function(){
    if (this.mousePos !== null && this.context.isPointInPath(this.mousePos.x, this.mousePos.y)) {
    
        // handle onmousemove
        // do this everytime
        if (this.currentRegion.onmousemove !== undefined) {
            this.currentRegion.onmousemove();
        }
        
        // handle onmouseover
        if (this.lastRegionIndex != this.regionCounter) {
            this.lastRegionIndex = this.regionCounter;
            
            if (this.currentRegion.onmouseover !== undefined) {
                this.currentRegion.onmouseover();
            }
        }
        
        // handle onmousedown
        if (this.mouseDown && this.currentRegion.onmousedown !== undefined) {
            this.currentRegion.onmousedown();
            this.mouseDown = false;
        }
        
        // handle onmouseup
        if (this.mouseUp && this.currentRegion.onmouseup !== undefined) {
            this.currentRegion.onmouseup();
            this.mouseUp = false;
        }
        
    }
    else if (this.regionCounter == this.lastRegionIndex) {
        // handle mouseout condition
        this.lastRegionIndex = null;
        
        if (this.currentRegion.onmouseout !== undefined) {
            this.currentRegion.onmouseout();
        }
    }
    
    this.regionCounter++;
};

// ======================================= ANIMATION =======================================

Kinetic_2d.prototype.isAnimating = function(){
    return this.animating;
};

Kinetic_2d.prototype.getFrame = function(){
    return this.frame;
};
Kinetic_2d.prototype.startAnimation = function(){
    this.animating = true;
    var date = new Date();
    this.startTime = date.getTime();
    this.lastTime = this.startTime;
    
    if (this.drawStage !== undefined) {
        this.drawStage();
    }
    
    this.animationLoop();
};
Kinetic_2d.prototype.stopAnimation = function(){
    this.animating = false;
};
Kinetic_2d.prototype.getTimeInterval = function(){
    return this.timeInterval;
};
Kinetic_2d.prototype.getTime = function(){
    return this.t;
};
Kinetic_2d.prototype.getFps = function(){
    return 1000 / this.timeInterval;
};
Kinetic_2d.prototype.animationLoop = function(){
    var that = this;
    
    this.frame++;
    var date = new Date();
    var thisTime = date.getTime();
    this.timeInterval = thisTime - this.lastTime;
    this.t += this.timeInterval;
    this.lastTime = thisTime;
    
    if (this.drawStage !== undefined) {
        this.drawStage();
    }
    
    if (this.animating) {
        requestAnimFrame(function(){
            that.animationLoop();
        });
    }
};

