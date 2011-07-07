(function($) {
$.namespace('biomart.streaming', function(exports) {
    var STREAMERS = {};

    exports.write = function(uuid, s) {
        var scope = STREAMERS[uuid];
        if (scope.preprocess) {
            var value = scope.preprocess(s);
            if (value) scope.write.apply(scope, [value]);
        } else {
             scope.write.apply(scope, [s]);
        }
    };

    exports.done = function(uuid) {
        var scope = STREAMERS[uuid];
          scope.done.apply(scope);
    };

    exports.Streamer = function(element, o) {
        var self = this,
            total = 0,
            skip = 0,
            end = 0,
            limit = Infinity,
            onDone,
            highlight,
            buffer = [],
            write = [
                function(s) { // headers
                    if (s) {
                        s = s.split('\t');
                        self.writee.data('headers_cache', s);
                        self.renderer.printHeader(s, self.writee);
                        self.write = write[1];
                    }
                },
                function(s) { // normal data
                    if (biomart.errorRegex.test(s)) return;
                    if (s) {
                        s = s.split('\t');
                        self.writee.data('results_cache').push(s);

                        if (skip <= ++total && end < limit) {
                            buffer.push(s);
                            end++;
                        }
                    }
                    if (end%self.bufferSize == 0) {
                        self.renderer.parse(buffer, self.writee);
                        buffer = [];
                    }
                }
            ],

            bufferSize = $.browser.msie ? 50 : 20; // Larger buffer for IE (performance)

        self.uuid = biomart.uuid();
        self.write = write[1];
        self.renderer = biomart.renderer.get('table');

        // Set options
        if (o) {
            if (o.displayType) self.renderer = biomart.renderer.get(o.displayType);
            if (o.headers) self.write = write[0];
            if (o.skip) skip = o.skip;
            if (o.limit) limit = o.limit + skip;
            if (o.bufferSize) bufferSize = o.bufferSize;
            if (o.done) onDone  = o.done;
            if (o.squish) self.renderer.option('squish', o.squish);
            if (o.preprocess) self.preprocess = o.preprocess;
        }

        end = skip;

        // prepare writee DOM Element
        self.writee = self.renderer.getElement().addClass('writee').appendTo(element);

        self.writee.data('results_cache', []);

        self.paginate = function(skip, limit) {
            var cache = self.writee.data('results_cache'),
                rows = [],
                end = skip + limit - 1;

            self.writee.find('tbody').empty();

            for (var i=skip, curr; curr=cache[i]; i++) {
                if (i > end) break;
                rows.push(curr);
            } 
            self.renderer.parse(rows, self.writee);

            return [total, skip+1, i];
        };

        // Only valid for renderers with 'setHighlightColumn' defined
        self.setHighlightColumn = function(i) {
            if (self.renderer.setHighlightColumn) self.renderer.setHighlightColumn(i);
        };

        self.done = function() {
            self.renderer.parse(buffer, self.writee);
            self.renderer.draw(self.writee);
            buffer = [];
            if (onDone) onDone.apply(self, [total, skip+1, end]); // 1-based indexing
        };

        STREAMERS[self.uuid] = self;
    };
});
})(jQuery);

