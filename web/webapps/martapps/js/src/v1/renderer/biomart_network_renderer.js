;(function (d3) {
"use strict";

// var _ = window._.noConflict()
biomart.networkRendererConfig = {
        graph: {
                nodeClassName: 'network-bubble',
                edgeClassName: 'network-edge',
                radius: function (d) {
                        return 10 + d.radius
                }
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
                gravity: 0.01, // 0.175
                threshold: 0.005,
                cluster: {
                        padding: 60
                }
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

// biomart.networkRendererConfig.force.linkDistance = 20
biomart.networkRendererConfig.force.charge = function (d) {
        // return d.isHub ? 10 * d.weight * d.x/1000 : -10 * d.weight * d.x/1000
        return d.isHub ? 4 * d.weight : -2 * d.weigth
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

function makeText (svg, nodes, config) {
        return hyperlinks(svg, nodes, config).selectAll('g')
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
                        .theta(config.theta || 2)
                        .gravity(config.gravity)
                        .linkDistance(config.linkDistance) // px
                        // .linkStrength(cs.linkStrength)
                        .charge(config.charge)

                force.on("tick", config.tick)

                return force
        }

        return make

})(d3);

function makeForce (nodes, edges, config) {
        // Create the layout and place the bubbles and links.
        return Force(nodes, edges, config)
}
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

                lines.each(function (d) {
                        var w = 'value' in d ? d.value * 100 : 1
                        if (w > 7) w = 7
                        if (w < 1) w = 1
                        d3.select(this).style('stroke-width', w)
                })

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

})(d3);
/**
 * @param config - Object configuration for the graph only
**/
function makeGraph (svg, nodes, edges, config) {
        // Draw the graph chart without positioning the elements, and return
        // bubbles and links: { bubbles: ..., links: ... }
        var graphChart = Graph(svg, nodes, edges, config)
        graphChart.bubbles.on('mouseover', function () {
                        this.__radius__ || (this.__radius__ = +this.getAttribute('r'))
                        d3.select(this)
                                .transition()
                                .attr('r', this.__radius__ * 2) })
                .on('mouseout', function () {
                        d3.select(this)
                                .transition()
                                .attr('r', this.__radius__)
                })

        return graphChart
}

function tick2 (attrs) {
        var config = attrs.config
        var bubbles = attrs.bubbles
        var links = attrs.links
        var hubIdx = attrs.hubIndexes
        var adj = attrs.adj
        var nodes = bubbles.data()
        var maxDomain = 20000
        var colorScale = d3.scale.linear().domain([0, maxDomain]).range(['#ff0000', '#00ffff'])
        var width = config.force.size[0]
        var height = config.force.size[1]
        var hubFromColor = {}
        var maxRadius = typeof config.graph.radius === 'number'
                ? config.graph.radius
                : d3.max(bubbles.data(), config.graph.radius)

        // BFS
        function searchColor(root, adj, nodes) {
                var queue = [root], q, len, qAdj, i, a
                while (queue.length) {
                        q = queue.pop()
                        q._visited = true
                        i = nodes.indexOf(q)
                        // edges out of q
                        if (adj[i]) {
                                qAdj = adj[i]
                        } else {
                                // edges in q
                                qAdj = []
                                for (var j = 0; j < adj.length; ++j) {
                                        if (adj[j] && adj[j][i])
                                                qAdj[j] = 1
                                }
                        }
                        for (a = 0, len = qAdj.length; a < len; ++a) {
                                if (qAdj[a] && 'color' in nodes[a]) {
                                        q.color = nodes[a].color
                                        break
                                } else {
                                        if (!('_visited' in nodes[a]) && queue.indexOf(a) < 0)
                                                queue.push(a)
                                }
                        }
                }
                // If no color was given, assign random color.
                // With the current hub selection algorithm, this can happen when there
                // are strongly connected components with few edges.
                if (!('color' in root)) {
                        markHub(root)
                        hubIdx.push(nodes.indexOf(root))
                }
        }

        function markHub (node) {
                var color = colorScale(Math.random() * maxDomain)
                node.color = color
                node.isHub = true
                hubFromColor[color] = node
        }

        hubIdx.forEach(function (idx) {
                markHub(nodes[idx])
        })

        // Give colors to nodes based on clusters
        nodes.forEach(function (node) {
                searchColor(node, adj, nodes)
        })

        // Modified version of http://bl.ocks.org/mbostock/1748247
        // Move d to be adjacent to the cluster node.
        function clusterHelper(alpha) {
                return function(d) {
                        var l, r, x, y, k = 1.5, node = hubFromColor[d.color]

                        // For cluster nodes, apply custom gravity.
                        if (d === node) {
                                node = {x: width / 2, y: height / 2, radius: -d.radius}
                                k = .5 * Math.sqrt(d.radius)
                        }

                        // I need them gt zero or they'll have the same value while deciding the bubble position.
                        // Same position causes problems with further recomputation of this func.
                        // if ((x = d.x - node.x) < 0) x = node.x - d.x
                        // if ((y = d.y - node.y) < 0) y = node.y - d.y
                        x = d.x - node.x
                        y = d.y - node.y
                        // distance between this node and the hub
                        l = Math.sqrt(x * x + y * y)
                        r = node.radius + d.radius //('radius' in node ? node.radius : radius)
                        // if distance !== from sum of the two radius, that is, if they aren't touching
                        if (l != r) {
                                l = (l - r) / l * alpha * k
                                // move this node towards the hub of some amount
                                d.x -= x *= l
                                d.y -= y *= l
                                node.x += x
                                node.y += y
                        }
                }
        }

        // Resolves collisions between d and all other circles.
        function collide(alpha) {
                var quadtree = d3.geom.quadtree(nodes)
                var padding = config.force.cluster.padding
                return function(d) {
                        var r = d.radius + maxRadius + padding
                        var nx1 = d.x - r
                        var nx2 = d.x + r
                        var ny1 = d.y - r
                        var ny2 = d.y + r
                        quadtree.visit(function(quad, x1, y1, x2, y2) {
                                if (quad.point && (quad.point !== d)) {
                                        var x = d.x - quad.point.x
                                        var y = d.y - quad.point.y
                                        var l = Math.sqrt(x * x + y * y)
                                        var r = d.radius + quad.point.radius + (d.color !== quad.point.color) * padding
                                        if (l < r) {
                                                l = (l - r) / l * alpha
                                                d.x -= x *= l
                                                d.y -= y *= l
                                                quad.point.x += x
                                                quad.point.y += y
                                        }
                                }
                                return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1
                        })
                }
        }

        return function(evt) {
                bubbles
                        .each(clusterHelper(10 * evt.alpha * evt.alpha))
                        .each(collide(.5))
        }
}

/**
 * @param config - Object must have properties for the configuration of `graph`, `force`
**/
// adj, bubbles, links, config
function cluster (attrs) {
        attrs.config.force.tick = tick2(attrs)
}

function hubIndexes(edges) {
        var freq = [], m, hubIdxs = []
        var degs = function (edge) {
                freq[edge.source] = freq[edge.source] ? freq[edge.source] + 1 : 1
                freq[edge.target] = freq[edge.target] ? freq[edge.target] + 1 : 1
        }

        edges.forEach(degs)
        m = d3.quantile(freq.slice(0).sort(), 0.999)
        freq.forEach(function (f, i) {
                if (f > m)
                        hubIdxs.push(i)
        })

        return hubIdxs
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

// The wrap container is a div.
// This element is created inside oldGetElement
nt.tagName = 'div'

var oldGetElement = nt.getElement

nt.getElement = function () {
        // If already present there isn't need to do anything else here
        if ($('#network-list').size())
                return $('#network-list')

        // Create the container
        var $elem = oldGetElement.call(this)
        // This is the actual tab list
        $elem.append('<ul id="network-list" class="network-tabs"></ul>')
        return $elem
}

nt._init = function () {
        this._nodes = []
        this._edges = []
        this._svg = null
        this._cache = []
        this._rowBuffer = []
        // matrix
        this._adj = []
        this._max = 0
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

function findIndex(collection, cb) {
        for (var i = 0, len = collection.length; i < len; ++i) {
                if (cb(collection[i]))
                        return i
        }
        return -1
}

nt._makeNE = function (row) {
        var nodePair = this._makeNodes(row)
        var val0Func = this.node0.value, val1Func = this.node1.value
        var node0Val = val0Func(nodePair[0]), node1Val = val1Func(nodePair[1])

        var index0 = findIndex(this._nodes, function (n) {
                return val0Func(n) === node0Val
        })
        var index1 = findIndex(this._nodes, function (n) {
                return val1Func(n) === node1Val
        })

        if (index0 < 0)
                index0 = this._nodes.push(nodePair[0]) - 1
        if (index1 < 0)
                index1 = this._nodes.push(nodePair[1]) - 1
        if (! this._adj[index0])
                this._adj[index0] = []

        this._adj[index0][index1] = 1
}

nt._makeNE = function (row) {
        var value = +row[2]
        var nodePair = this._makeNodes(row)
        var val0Func = this.node0.value, val1Func = this.node1.value
        var node0Val = val0Func(nodePair[0]), node1Val = val1Func(nodePair[1])

        var index0 = findIndex(this._nodes, function (n) {
                return val0Func(n) === node0Val
        })
        var index1 = findIndex(this._nodes, function (n) {
                return val1Func(n) === node1Val
        })

        if (index0 < 0)
                index0 = this._nodes.push(nodePair[0]) - 1
        if (index1 < 0)
                index1 = this._nodes.push(nodePair[1]) - 1
        if (! this._adj[index0])
                this._adj[index0] = []

        this._adj[index0][index1] = 1
        if (value > this._max)
                this._max = value

        var n0 = this._nodes[index0]
        var n1 = this._nodes[index1]
        n0.radius = 'radius' in n0 ? n0.radius + 5 : 5
        n1.radius = 'radius' in n1 ? n1.radius + 5 : 5
        this._edges.push({source: this._nodes[index0], target: this._nodes[index1], value: value})
}

function instanceEdges (adj, nodes, edges) {
        for (var i = 0, n = adj.length; i < n; ++i) {
                if (adj[i])
                        for (var j = 0, m = adj[i].length; j < m; ++j) {
                                if (adj[i][j])
                                        edges.push({ source: i, target: j })
                        }
        }
}

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

                        Array.prototype.push.apply(this._rowBuffer, this._cache)
                        this._cache = []
                }
        }, this)
}

nt.printHeader = function(header, writee) {
        this._init()
        var w = $(window).width()
        var h = $(window).height()
        var $tabsCont = writee.find('#network-list')
        var item, tabNum, svg

        tabNum = $tabsCont.children().size() + 1
        if (tabNum === 1) writee.tabs() //

        item = 'item-'+ tabNum
        // For each attribute list create a tab
        writee.tabs('add', '#'+ item, Object.keys(biomart._state.queryMart.attributes)[tabNum-1])
        // Playground for the new network
        this._svg = svg = d3.select($('#'+ item)[0])
                .append('svg:svg')
                .attr({ width: w, height: h })
                .append('g')
                .call(d3.behavior.zoom().scaleExtent([0, 20]).on('zoom', function () {
                        svg.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")") }))
                .append('g')

        svg.append("rect")
                .attr('class', 'zoom-container')
                .attr('x', -2.5 * w)
                .attr('y', -4 * h)
                .attr("width", w * 5)
                .attr("height", h * 8)

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

nt.draw = function (writee) {
        var config = biomart.networkRendererConfig

        this._drawNetwork(config)

        this._init()
        $.publish('network.completed')
}

function initPosition (nodes, width, height) {
        nodes.forEach(function (node) {
                node.x = Math.random() * width
                node.y = Math.random() * height
        })
}

nt._drawNetwork = function (config) {
        var w = $(window).width()
        var h = $(window).height()
        var clusterParams
        var graph
        var force
        var drag
        var text
        var hubIdxs
        var hubs = []
        var drawText = 'text' in config
        var self = this

        self.timers = []

        config.graph['id'] = function (d) {
                var node = null
                if (self.node0.key in d)
                        node = 'node0'
                else node = 'node1'

                return self[node].value(d)
        }

        config.force.size = [w, h]

        for (var i = 0, nLen = this._rowBuffer.length; i < nLen; ++i)
                this._makeNE(this._rowBuffer[i])

        this._rowBuffer = []
        if (! this._edges.length)
                instanceEdges(this._adj, this._nodes, this._edges)

        graph = makeGraph(this._svg, this._nodes, this._edges, config.graph)

        hubIdxs = hubIndexes(this._edges)
        initPosition(this._nodes, w, h)
        // for (var i = 0; i < hubIdxs.length; ++i)
        //         hubs.push(this._nodes[hubIdxs[i]])
        // initPosition(hubs, w / 4, h / 4)

        clusterParams = {
                adj: this._adj,
                bubbles: graph.bubbles,
                links: graph.links,
                config: config,
                hubIndexes: hubIdxs
        }

        if (drawText) {
                config.text.text = config.graph['id']
                text = makeText(this._svg, this._nodes, config.text)
        }

        // `cluster` defines the right force configuration: e.g. charge, tick
        cluster(clusterParams)

        // Now we can create the force layout. This actually starts the symulation.
        force = makeForce(this._nodes, this._edges, config.force)

        resize(function () {
                if (self._svg && !self._svg.empty()) {
                        self._svg.attr({
                                width: w,
                                height: h
                        })
                        force.size([w, h])
                }
        })

        function loop (thr, iter) {
                var t
                if (iter < 1000 && force.alpha() > thr) {
                        force.tick()
                        t = setTimeout(function () {
                                loop(thr, ++iter)
                        }, 1)
                        self._addTimer(t)
                } else {
                        endSimulation()
                }
        }
        // Make the simulation in background and then draw on the screen
        force.start()
        console.time('simulation ticks')
        loop(config.force.threshold, 0)
        // for (var safe = 0; safe < 2000 && force.alpha() > config.force.threshold; ++safe)
        //         force.tick()
        // console.timeEnd('simulation ticks')
        // force.stop()

        var oneTick = function (graph, text) {
                graph.bubbles
                        .attr('transform', function (d) {
                                d.fixed = true
                                d3.select(this).style('fill', d3.rgb(d.color).darker(d.weight/3))
                                return 'translate(' + d.x + ',' + d.y + ')'
                        })

                graph.links
                        .attr({
                                x1: function(d) { return d.source.x },
                                y1: function(d) { return d.source.y },
                                x2: function(d) { return d.target.x },
                                y2: function(d) { return d.target.y }
                        })

                if (text) {
                        text.attr('transform', function (d) {
                                return 'translate('+ (d.x + 5) +','+ d.y +')'
                        })
                }

                oneTick = function () {
                        graph.bubbles
                                .attr('transform', function (d) {
                                        return 'translate(' + d.x + ',' + d.y + ')'
                                })

                        graph.links
                                .attr({
                                        x1: function(d) { return d.source.x },
                                        y1: function(d) { return d.source.y },
                                        x2: function(d) { return d.target.x },
                                        y2: function(d) { return d.target.y }
                                })

                        if (text) {
                                text.attr('transform', function (d) {
                                        return 'translate('+ (d.x + 5) +','+ d.y +')'
                                })
                        }
                }
        }

        function showNetwork () {
                oneTick(graph, text)
                setEventHandlers()
        }

        function setEventHandlers() {
                force.on('tick', function () { oneTick(graph, text) })

                drag = force.drag().on('dragstart', function (d) {
                        force.stop()
                        d3.event.sourceEvent.stopPropagation()
                        d.fixed = true
                })
                graph.bubbles.call(drag)
        }

        function endSimulation () {
                console.timeEnd('simulation ticks')
                force.stop()
                showNetwork()
        }
}

nt._addTimer = function (t) {
        this.timers || (this.timers = [])
        this.timers.push(t)
}

nt._clearTimers = function () {
        this.timers.forEach(function (t) {
                clearTimeout(t)
        })
        self.timers = []
}

nt.clear = function () {
        console.log('network-renderer#clear: not implemented yet.')
}

nt.destroy = function () {
        this._clearTimers()
        if (this._svg) {
                d3.select(this._svg.node().nearestViewportElement).remove()
        }
        this._max = 0
        this._nodes = this._edges = this._svg = this._rowBuffer = this._cache = this._adj = null
}

})(d3);