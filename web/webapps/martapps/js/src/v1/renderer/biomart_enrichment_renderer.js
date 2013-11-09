;(function (d3) {
"use strict";

// var _ = window._.noConflict()
biomart.enrichmentRendererConfig = {
        graph: {
                nodeClassName: 'network-bubble',
                edgeClassName: 'network-edge',
                radius: function (d) {
                        return 5 + d.radius
                },
                'id': function (d) {
                        return d._key
                }
        },

        force: {
                linkDistance: 0,
                charge: 0,
                gravity: 0, // 0.175
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




var Table = (function (d3) {
        "use strict"

        function Table (config) {
                this.init(config)
        }

        Table.prototype = {
                init: function (config) {
                        this.config = config
                        this.numCol = config.numCol
                        this.header = config.header
                        this.table = this.body = null
                        config.className || (config.className = "report-tb")
                        this._makeTable(config.wrapper)
                },

                _makeTable: function (wrapper) {
                        // make table
                        var t = this.table = _make("table", null, this.config.className)
                        var h

                        // append header
                        t.appendChild(h = _make("thead"))
                        // header is of one row
                        h.appendChild(_makeRow(this.header))

                        t.appendChild(this.body = _make("tbody"))

                        wrapper.appendChild(this.table)
                },

                addRow: function (content) {
                        var r = _makeRow(content, this.numCol)
                        this.body.appendChild(r)
                        return r
                },

                clear: function () {
                        this.table.removeChild(this.body)
                        this.body = null
                },

                destroy: function () {
                        this.table.parentNode.removeChild(this.table)
                        this.table = this.body = this.header = this.config = null
                }
        }

        function _makeRow (content, c) {
                var i = 0, len = c || content.length, r = _make("tr")
                for (; i < len; ++i) {
                        r.appendChild(_makeCol(content[i]))
                }
                return r
        }


        function _makeCol (text) {
                var t = _make("td")
                t.textContent = text
                return t
        }

        function _make (el, idName, className) {
                var e = document.createElement(el)
                if (idName) e.id = idName
                if (className) e.className = className
                return e
        }

        return Table
}) (d3)
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
                        r = 2 * (node.radius + d.radius) //('radius' in node ? node.radius : radius)
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
                        var r = 2 * (d.radius + maxRadius) + padding
                        var nx1 = d.x - r
                        var nx2 = d.x + r
                        var ny1 = d.y - r
                        var ny2 = d.y + r
                        quadtree.visit(function(quad, x1, y1, x2, y2) {
                                if (quad.point && (quad.point !== d)) {
                                        var x = d.x - quad.point.x
                                        var y = d.y - quad.point.y
                                        var l = Math.sqrt(x * x + y * y)
                                        var r = 2 * (d.radius + quad.point.radius) + (d.color !== quad.point.color) * padding + padding
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


var concat = Array.prototype.push
// ============================================================================
// NOTE!!
//
// Just for now ignore renderInvalid Option!
// ============================================================================
var nt = biomart.renderer.results.enrichment = Object.create(biomart.renderer.results.plain)

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

        var $tabWrap = $('<div class="network-tabs-wrapper">')
        $tabWrap.append('<ul id="network-list" class="network-tabs"></ul>')
        $elem.append($tabWrap)
        $elem.append('<div id="network-report-table" class="network-report-table"></div>')
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

function annotation (keys, values) {
        var a = { typeId: 'annotation' }
        addProp(a, keys[0], values[0])
        a._key = a[keys[0]]
        addProp(a, keys[1], values[1])
        a.radius = a[keys[1]] * 100
        return a
}

function gene (k, v) {
        var g = { typeId: 'gene' }
        addProp(g, k, v)
        g._key = g[k]
        g.radius = 8
        return g
}

function genes (keys, values) {
        var g = [], vs = values[2].split(',')
        for (var i = 0, len = vs.length; i < len; ++i) {
                g.push(gene(keys[2], vs[i]))
        }
        return g
}

nt._makeNodes = function (row) {
        var a = [annotation(this.header, row)]
        var gs = genes(this.header, row)
        concat.apply(a, gs)
        return a
}

function addProp (node, key, value) {
        if (value.indexOf('<a') >= 0) {
                value = $(value)
                node[key] = value.text()
                node._link = value.attr('href')
        } else {
                node[key] = value
        }

        return node
}

function findIndex(collection, cb) {
        for (var i = 0, len = collection.length; i < len; ++i) {
                if (cb(collection[i]))
                        return i
        }
        return -1
}

nt._makeNE = function (row) {
        var self = this
        var nodes = this._makeNodes(row)
        var newNode
        var ann = nodes[0]
        var annIndex
        var geneIndex

        function comp (n) {
                var h
                return n.typeId === newNode.typeId &&
                        ((h = self.header[0]) in n
                                ? n[h] === newNode[h]
                                : n[h = self.header[2]] === newNode[h])
        }

        newNode = ann
        annIndex = findIndex(this._nodes, comp)
        if (annIndex < 0) {
                annIndex = this._nodes.push(ann) - 1
        }
        ann = this._nodes[annIndex]

        nodes.slice(1).forEach(function (n) {
                var value
                newNode = n
                geneIndex = findIndex(self._nodes, comp)
                if (geneIndex < 0) {
                        geneIndex = self._nodes.push(newNode) - 1
                }
                newNode = self._nodes[geneIndex]

                if (! self._adj[annIndex])
                        self._adj[annIndex] = []

                self._adj[annIndex][geneIndex] = 1

                self._edges.push({source: self._nodes[geneIndex], target: self._nodes[annIndex], value: 0.01})
        })
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
                .attr({ width: "100%", height: "100%" })
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
}

nt.draw = function (writee) {
        var config = biomart.enrichmentRendererConfig

        this._drawNetwork(config)

        this._init()
        $.publish('enrichment.completed')
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

        // config.graph['id'] = function (d) {
        //         var node = null
        //         if (self.node0.key in d)
        //                 node = 'node0'
        //         else node = 'node1'

        //         return self[node].value(d)
        // }

        config.force.size = [w, h]

        this.table = new Table({
                wrapper: $("#"+ "network-report-table")[0],
                header: this.header.slice(0, -1),
                numCol: 2
        })

        for (var i = 0, nLen = this._rowBuffer.length; i < nLen; ++i) {
                this._makeNE(this._rowBuffer[i])
                this.table.addRow(this._rowBuffer[i])
        }

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
        force = makeForce(this._nodes, [], config.force)

        // resize(function () {
        //         if (self._svg && !self._svg.empty()) {
        //                 self._svg.attr({
        //                         width: w,
        //                         height: h
        //                 })
        //                 force.size([w, h])
        //         }
        // })

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
        this.table.destroy()
        this._nodes = this.table = this._edges = this._svg = this._rowBuffer = this._cache = this._adj = null
}

})(d3);