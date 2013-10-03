var BiomartVisualization = {}
BiomartVisualization.Network = {
        // ## Graph
        //
        // *    svg     - `Object` d3 selection of an svg.
        // *    nodes   - `Array`  Of objects.
        // *    edges   - `Array`  Of objects of the form `{ source: a, target: b }`. Where `a` and `b` ara integers.
        //      See [d3.force.links()](https://github.com/mbostock/d3/wiki/Force-Layout#wiki-links).
        // *    config  - `Object` Containes the configuration for the graph.
        //      *       radius: bubble's radius
        //      *       nodeClassName: class for a bubble
        //      *       color: color for a bubble
        //      *       edgeClassName: class for a link
        //
        // All the attributes are d3 style: value or callback(d, i).
        Graph: (function (d3) {

                "use strict"

                function makeLines(svg, edges, config) {
                        // Update
                        var lines = svg.selectAll('line')
                                .data(edges)

                        // Enter
                        var enter = lines.enter()
                                .append('line')
                                .attr('class', config.edgeClassName)

                        // Exit
                        lines.exit()
                                .remove()

                        return enter
                }

                // A group with a circle and a text for each data.
                function makeBubbles(svg, nodes, config) {
                        var update = svg.selectAll('circle')
                                .data(nodes)

                        update.exit()
                                .remove()

                        var bubbles = update.enter()
                                .append('circle')
                                .attr({
                                        r: config.radius,
                                        'class': config.nodeClassName,
                                        fill: config.color,
                                        id: config.id })

                        return bubbles
                }

                function graph (svg, nodes, edges, config) {
                        var group = svg.append('svg:g')

                        if ('groupId' in config)
                                group.attr('id', config.groupId)

                        return {
                                links: makeLines(group, edges, config),
                                bubbles: makeBubbles(group, nodes, config)
                        }
                }

                return graph

        })(d3),




        // ## Force
        //
        // *    nodes  - `Array`
        // *    edges  - `Array`
        // *    config - `Object`
        //              *       size
        //              *       gravity
        //              *       linkDistance
        //              *       charge
        //              *       tick
        //
        Force: (function (d3) {

                "use strict"

                function make (nodes, edges, config) {
                        var force = d3.layout.force()
                                .nodes(nodes)
                                .links(edges)
                                .size(config.size)
                                .gravity(config.gravity)
                                .linkDistance(config.linkDistance) // px
                                // .linkStrength(cs.linkStrength)
                                .charge(config.charge)

                        force.on("tick", config.tick)
                        force.start()

                        return force
                }

                return make

        })(d3),


        Text: (function (d3) {

                "use strict"

                // config -
                //      * font-family
                //      * font-size
                //      * stroke
                //      * fill
                //      * text-anchor
                //      * x, y
                //      * text
                //      * id - id of the group
                //      * doubleLayer:
                //              * className
                //
                // elms must contain groups
                function make (svg, data, config) {
                        var attrs = [
                                'font-family', 'font-size', 'stroke', 'fill', 'text-anchor', 'x', 'y'
                        ]

                        var _conf = {}
                        var a

                        for (var i = 0; i < attrs.length; ++i) {
                                a = attrs[i]
                                if (config.hasOwnProperty(a))
                                        _conf[a] = config[a]
                        }

                        var group = svg.append('svg:g')
                        if ('groupId' in config)
                                group.attr('id', config.groupId)

                        var text = group.selectAll('g')
                                .data(data)
                                .enter()
                                .append('svg:g')

                        if (config.doubleLayer) {
                                text.append('svg:text')
                                        .attr(_conf)
                                        .attr('class', config.doubleLayer.className)
                                        .text(config.text)
                        }

                        text.append('svg:text')
                                .attr(_conf)
                                .text(config.text)

                        return text
                }

                return make
        })(d3),




        // We can use this function or a custom one to create the graph
        make: (function (d3) {

                "use strict"

                function make (svg, nodes, edges, config) {
                        // Draw the graph chart without positioning the elements, and return
                        // bubbles and links: { bubbles: ..., links: ... }
                        var graphChart = this.Graph(svg, nodes, edges, config.graph)
                        graphChart.bubbles.on('mouseover', function () {
                                d3.select(this)
                                        .transition()
                                        .attr('r', r * 2) })
                                .on('mouseout', function () {
                                        d3.select(this)
                                                .transition()
                                                .attr('r', config.graph.radius)
                                })

                        var text

                        if (config.text) {
                                text = this.Text(svg, nodes, config.text)
                        }

                        var r = typeof config.graph.radius === 'number'
                                ? config.graph.radius
                                : d3.max(nodes, config.graph.radius)

                        // Layout configuration
                        config.force.tick = function() {
                                var forceSize = force.size()

                                graphChart.links.attr({
                                        x1: function(d) { return d.source.x },
                                        y1: function(d) { return d.source.y },
                                        x2: function(d) { return d.target.x },
                                        y2: function(d) { return d.target.y } })

                                graphChart.bubbles
                                        .attr('transform', function (d) {
                                                d.x = Math.max(r, Math.min(forceSize[0] - r, d.x))
                                                d.y = Math.max(r, Math.min(forceSize[1] - r, d.y))
                                                return 'translate(' + d.x + ',' + d.y + ')' })

                                config.text && text.attr('transform', function (d) {
                                        return 'translate('+ (d.x + 10) +','+ d.y +')' })
                        }

                        function dragstart (d) {
                                d.fixed = true
                        }

                        // Create the layout and place the bubbles and links.
                        var force = this.Force(nodes, edges, config.force)

                        var drag = force.drag().on('dragstart', dragstart)

                        graphChart.bubbles.call(drag)

                        return {
                                graph: graphChart,
                                force: force,
                                text: text
                        }
                }

                return make

        })(d3)
}

d3.BiomartVisualization = BiomartVisualization;

;(function (d3) {

"use strict";


function textCallback (_, config) {
        var attrs = {
                'font-family': config['font-family'],
                'font-size': config['font-size'],
                'stroke': config.stroke,
                'fill': config.fill,
                'text-anchor': config['text-anchor']
        }

        var textGroup = this.append('svg:g')

        // This could be improved returning a different func 
        // chosen by the doubleLayer param
        if (config.doubleLayer) {
                textGroup.append('svg:text')
                        .attr(attrs)
                        .attr('class', config.doubleLayer.className)
                        .text(config.text)
        }

        textGroup.append('svg:text')
                .attr(attrs)
                .text(config.text)
}


biomart.networkRendererConfig = {
        graph: {
                nodeClassName: 'network-bubble',
                edgeClassName: 'network-edge',
                radius: 10,
                color: function(d) { return '#0A6AF7' }
        },

        force: {
                linkDistance: function(link) {
                        // return link.source.weight + link.target.weight > 8 ? 200 : 100
                        if (link.source.weight > 4 ^ link.target.weight > 4)
                            return 150
                        if (link.source.weight > 4 && link.target.weight > 4)
                            return 350
                        return 100
                },
                charge: -500,
                gravity: 0.06, // default 0.1
        },

        text: {
                'font-family': 'serif',
                'font-size': '1em',
                'stroke': '#ff0000',
                'text-anchor': 'start',
                'doubleLayer': { 'className': 'network-shadow' },
                callback: textCallback,
                link: function (d) {
                        return d._link
                }
        }
}

function graph (svg, nodes, edges, config) {
        var network = d3.BiomartVisualization.Network
        // Draw the graph chart without positioning the elements, and return
        // bubbles and links: { bubbles: ..., links: ... }
        var graphChart = network.Graph(svg, nodes, edges, config.graph)
        graphChart.bubbles.on('mouseover', function () {
                d3.select(this)
                        .transition()
                        .attr('r', r * 2) })
                .on('mouseout', function () {
                        d3.select(this)
                                .transition()
                                .attr('r', config.graph.radius)
                })

        var text

        if (config.text) {
                text = hyperlinks(svg, nodes, config.text).selectAll('g')
        }

        var r = typeof config.graph.radius === 'number'
                ? config.graph.radius
                : d3.max(nodes, config.graph.radius)

        // Layout configuration
        config.force.tick = function() {
                var forceSize = force.size()

                graphChart.links.attr({
                        x1: function(d) { return d.source.x },
                        y1: function(d) { return d.source.y },
                        x2: function(d) { return d.target.x },
                        y2: function(d) { return d.target.y } })

                graphChart.bubbles
                        .attr('transform', function (d) {
                                d.x = Math.max(r, Math.min(forceSize[0] - r, d.x))
                                d.y = Math.max(r, Math.min(forceSize[1] - r, d.y))
                                return 'translate(' + d.x + ',' + d.y + ')' })

                config.text && text.attr('transform', function (d) {
                        return 'translate('+ (d.x + 10) +','+ d.y +')' })
        }

        function dragstart (d) {
                d.fixed = true
        }

        // Create the layout and place the bubbles and links.
        var force = network.Force(nodes, edges, config.force)

        var drag = force.drag().on('dragstart', dragstart)

        graphChart.bubbles.call(drag)

        return {
                graph: graphChart,
                force: force,
                text: text
        }
}


function hyperlinks (svg, data, config) {
        var update = svg.selectAll('a')
                .data(data)
                
        var a = update.enter()
                .append('svg:a')
                .attr({
                        'xlink:href': config.link,
                        target: '_blank'
                })

        if (config.callback)
                a.call(config.callback, config)

        update.exit().remove()

        return a
}

// function appendText () {
//         this.each(function (d, i) {
//                 // `this` is a group
//                 var shadow = document.createElementNS('http://www.w3.org/2000/svg', 'text')
//                 var text = document.createElementNS('http://www.w3.org/2000/svg', 'text')
//                 shadow.setAttribute('class', 'network-shadow')
//                 this.appendChild(shadow)
//                 this.appendChild(text)
//         })
// }




function resizeHandler () {
        var w, h
        if (this._svg && !this._svg.empty()) {
                w = $(window).width()
                h = $(window).height()
                this._svg.attr({
                        width: w,
                        height: h
                })
                this._visualization.force.size([w, h])
        }
}

function resize (listener, interval) {
    var resizeTimeout

    window.addEventListener('resize', function() {
        if (! resizeTimeout) {
            resizeTimeout = setTimeout(function() {
                resizeTimeout = null
                listener.apply(null, arguments)
            }, interval || 66)
        }
    })
}


// ============================================================================
// NOTE!!
//
// Just for now ignore renderInvalid Option!
// ============================================================================
var nt = biomart.renderer.results.network = Object.create(biomart.renderer.results.plain)

nt._init = function () {
        this._nodes = []
        this._edges = []
        this._svg = this._visualization = null
}

// row: array of fields
nt._makeNodes = function (row) {
        var n0 = {}, n1 = {}
        var col0 = row[0], col1 = row[1]
        var k0 = this.node0.key, k1 = this.node1.key
        // If it's a link
        if (col0.indexOf('<a') >= 0) {
                col0 = $(col0)
                n0[k0] = col0.text()
                n0._link = col0.attr('href')
        } else {
                n0[k0] = col0
        }

        if (col1.indexOf('<a') >= 0) {
                col1 = $(col1)
                n1[k1] = col1.text()
                n1._link = col1.attr('href')
        } else {
                n1[k1] = col1
        }

        return [n0, n1]
}

nt._makeNE = function (row) {
        var nodePair = this._makeNodes(row)

        // Before pushing check if nodes are already in the list
        var node0Value = this.node0.value(nodePair[0])
        var node1Value = this.node1.value(nodePair[1])
        var alreadyPresent0 = null
        var alreadyPresent1 = null

        this._nodes.some(function (node) {
                if (this.node0.value(node) === node0Value) {
                        alreadyPresent0 = node
                        return true
                }
        }, this)

        this._nodes.some(function (node) {
                if (this.node1.value(node) === node1Value) {
                        alreadyPresent1 = node
                        return true
                }
        }, this)

        // If one of them is not in the node list
        if (!alreadyPresent0) {
                this._nodes.push(nodePair[0])
        }
        if (!alreadyPresent1) {
                this._nodes.push(nodePair[1])
        }

        // Because it could not be a repeated record
        this._edges.push({ source: alreadyPresent0 || nodePair[0],
                           target: alreadyPresent1 || nodePair[1] })
}

// results.network.tagName ?
// rows : array of arrays
nt.parse = function (rows, writee) {
        for (var i = 0, rLen = rows.length; i < rLen; ++i)
                this._makeNE(rows[i])
}

// Intercept the header
nt.printHeader = function(header, writee) {
        this._init()
        this.header = header
        this.header.forEach(function (nodeId, idx) {
                this['node'+idx] = {
                        key: nodeId,
                        value: function (nodeObj) {
                                return nodeObj[nodeId]
                        }
                }
        }, this)
}

function noDraw(svg) {
        if (svg && 'empty' in svg)
                return svg.empty()
        return true
}

nt.draw = function (writee) {
        // Use body because the container is too small now
        var w = $(window).width()
        var h = $(window).height()

        if (noDraw(this._svg)) {
                // writee should be a jQuery object        
                this._svg = d3.select(writee[0])
                        .append('svg:svg')
                        .attr({
                                width: w,
                                height: h,
                                'id': 'network-svg' })

                resize(resizeHandler.bind(this))
        }

        var config = biomart.networkRendererConfig
        var self = this
        config.text.text = config.graph['id'] = function (d) {
                var node = null
                if (self.node0.key in d)
                        node = 'node0'
                else node = 'node1'

                return self[node].value(d)
        }
        config.force.size = [w, h]
                
        this._visualization = graph(this._svg, this._nodes, this._edges, config)
}

nt.clear = function () {
        // Should I delete them?
        // this._nodes = []
        // this._edges = []
        // // this.header = this.node0 = this.node1 = null
        // if (this._svg) this._svg.remove()
        // this._svg = null
        // this._visualization = null
}

nt.destroy = function () {
        // this.clear()
        if (!noDraw(this._svg)) this._svg.remove()
        this._nodes = this._edges = this._svg = this._visualization = null
}

// nt._makeNodes = function (row) {
//         return this.schema.nodes.map(function (node) {
//                 var o = {}, attrIdxs = node.attrIdxs, idx
//                 for (var i = 0, len = attrIdxs.length; i < len; ++i) {
//                         idx = attrIdxs[i]
//                         o[this.header[idx]] = row[idx]
//                 }
//                 return o
//         }, this)
// }
// {
//         nodes: [
//                 {
//                         // indexes of columns that belong to this node
//                         attrIdxs: []
//                 },
//                 ...
//         ]
// }

})(d3);