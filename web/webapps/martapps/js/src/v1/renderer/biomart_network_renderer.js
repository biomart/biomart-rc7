;(function (d3) {

"use strict";


biomart.networkRendererConfig = {
        graph: {
                nodeClassName: 'network-bubble',
                edgeClassName: 'network-edge',
                radius: 5
        },

        force: {
                linkDistance: function(link) {
                        // return link.source.weight + link.target.weight > 8 ? 200 : 100
                        if (link.source.weight > 4 ^ link.target.weight > 4)
                            return 100
                        if (link.source.weight > 4 && link.target.weight > 4)
                            return 200
                        return 50
                },
                charge: -300,
                gravity: 0.175, // 0.06
        },

        text: {
                className: 'network-text',
                'doubleLayer': { 'className': 'network-shadow' },
                callback: textCallback,
                link: function (d) {
                        return d._link
                }
        }
}

function textCallback (_, config) {
        var keys = ['font-family', 'font-size', 'stroke', 'stroke-width', 'text-anchor']
        var attrs = {}
        keys.forEach(function (k) {
                if (k in config)
                        this[k] = config[k]
        }, attrs)

        if ('className' in config)
                attrs['class'] = config.className

        // `this` will be the selection this cb is invoked on
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
var Force = (function (d3) {

        "use strict";

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

})(d3)

// ## Graph
//
// *    svg     - `Object` d3 selection of an svg.
// *    nodes   - `Array`  Of objects.
// *    edges   - `Array`  Of objects of the form `{ source: a, target: b }`. Where `a` and `b` ara integers.
//      See [d3.force.links()](https://github.com/mbostock/d3/wiki/Force-Layout#wiki-links).
// *    config  - `Object` Containes the configuration for the graph.
//      *       radius: bubble's radius
//      *       nodeClassName - Optional: class for a bubble
//      *       fill - Optional : color for a bubble
//      *       edgeClassName - Optional: class for a link
//
//
// All the attributes are d3 style: value or callback(d, i).
var Graph = (function (d3) {

        "use strict";

        function makeLines(svg, edges, config) {
                // Update
                var update = svg.selectAll('line')
                        .data(edges)

                var attrs = {}

                if ('edgeClassName' in config)
                        attrs['class'] = config.edgeClassName

                // Enter
                var lines = update.enter()
                        .append('svg:line')
                        .attr(attrs)

                // Exit
                update.exit()
                        .remove()

                return lines
        }

        // A group with a circle and a text for each data.
        function makeBubbles(svg, nodes, config) {
                var update = svg.selectAll('circle')
                        .data(nodes)

                update.exit()
                        .remove()

                var attrs = { r: config.radius }

                if ('fill' in config)
                        attrs.fill = config.fill
                if (config.hasOwnProperty('id'))
                        attrs['id'] = config['id']
                if ('nodeClassName' in config)
                        attrs['class'] = config.nodeClassName

                var bubbles = update.enter()
                        .append('svg:circle')
                        .attr(attrs)

                return bubbles
        }

        function graph (svg, nodes, edges, config) {
                var group = svg
                if ('groupId' in config) {
                        group = d3.select('#'+config.groupId).empty()
                                ? svg.append('svg:g')
                                : d3.select('#'+config.groupId)
                        group.attr('id', config.groupId)
                }
                return {
                        links: makeLines(group, edges, config),
                        bubbles: makeBubbles(group, nodes, config)
                }
        }

        return graph

})(d3)
function makeGraph (svg, nodes, edges, config) {
        // Draw the graph chart without positioning the elements, and return
        // bubbles and links: { bubbles: ..., links: ... }
        var graphChart = Graph(svg, nodes, edges, config.graph)
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
        var force = Force(nodes, edges, config.force)

        var drag = force.drag().on('dragstart', dragstart)

        graphChart.bubbles.call(drag)

        setTimeout(function () {
                force.stop()
                graphChart.bubbles.data().forEach(function (d) { d.fixed = true })
        }, 1e4)

        return {
                graph: graphChart,
                force: force,
                text: text
        }
}


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
nt.tagName = 'div'

var oldGelElement = nt.getElement

nt.getElement = function () {
        if ($('#network-list').size())
                return $('#network-list')

        var $elem = oldGelElement.call(this)
        $elem.append('<ul id="network-list" class="container-tabs"></ul>')
        return $elem
}

nt._init = function () {
        this._nodes = []
        this._edges = []
        this._svg = this._visualization = null
        this._cache = []
        this._nodeBuffer = []
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
       rows.forEach(function (row) {
                if (row[1].trim() === '') {
                        this._cache.push(row)
                } else {
                        this._cache.forEach(function (cacheRow) {
                                cacheRow[1] = row[1]
                        })

                        this._cache.push(row)

                        Array.prototype.push.apply(this._nodeBuffer, this._cache)
                        this._cache = []
                }
       }, this)
}

// Intercept the header
nt.printHeader = function(header, writee) {
        this._init()

        var $tabsCont = writee.find('#network-list')
        var item, tabNum

        tabNum = $tabsCont.children().size() + 1
        item = 'item-'+ tabNum
        $tabsCont.append('<li><a href="#'+ item +'" >Network'+ tabNum +'</a></li>')
        writee.append('<div id="'+ item +'" class="subcontainer"></div>')
        this._svg = d3.select($('#'+ item)[0])
                .append('svg:svg')
                .attr({
                        width: $(window).width(),
                        height: $(window).height(),
                        'id': 'network-svg' })

        resize(resizeHandler.bind(this))

        this.header = header
        this.header.forEach(function (nodeId, idx) {
                this['node'+idx] = {
                        key: nodeId,
                        value: function (nodeObj) {
                                return nodeObj[nodeId]
                        }
                }
        }, this)

        $tabsCont.tabs()
}

nt.draw = function (writee) {
        var config = biomart.networkRendererConfig
        var self = this
        config.text.text = config.graph['id'] = function (d) {
                var node = null
                if (self.node0.key in d)
                        node = 'node0'
                else node = 'node1'

                return self[node].value(d)
        }

        config.force.size = [$(window).width(), $(window).height()]

        for (var i = 0, nLen = this._nodeBuffer.length; i < nLen; ++i)
                this._makeNE(this._nodeBuffer[i])

        this._nodeBuffer = []
        this._visualization = makeGraph(this._svg, this._nodes, this._edges, config)
        this._init()
        $.publish('network.completed')
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
        this._svg && this._svg.remove()
        this._nodes = this._edges = this._svg = this._visualization = this._nodeBuffer = this._cache = null
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