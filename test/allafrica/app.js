function getNodeObjects(node_names) {
    for (var i in node_names) {
        node_names[i] = $.trim(node_names[i]);
    }
    var nodes = [];
    var children = $("svg").children();
    // return children[9].__data__.name;
    children.each(function(ind, node) {
        var data = node.__data__
        if (data && $.inArray(data.name, node_names) > -1 ) {
            // highlight(node);
            nodes.push(node);
        }
    });
    // callback(nodes);
    return nodes
}

// function highlight(nodes) {
//     $.each(nodes, function(idx, node) {
//         $(node).trigger("click");
//     });
// }


// d3 code:
// need to make it tidy!
function generateD3graph(fname) {
    console.log("generating graph for " + fname);
    d3.csv(fname, function(error, links) {
        var average = 0;
        var nodes = {};
        // Compute the distinct nodes from the links.

        var width = 0.75*$(document).width()
        var height = 0.95*$(document).height();
        $('.graph-info').height(0.8*$(document).height());
        $('.graph-info').width(0.20*$(document).width());
        
        links.forEach(function(link) {
            // each link is an object
            // link source is node, link target is outlink and link value is probability
            // console.log(link.source + " -> " + link.target + " " + link.value);
            link.source = nodes[link.source] ||  (nodes[link.source] = {name: link.source});
            link.target = nodes[link.target] || (nodes[link.target] = {name: link.target});
            link.value = +link.value;
            average += link.value;
        });

        // console.log(nodes);

    var force = d3.layout.force()
        .nodes(d3.values(nodes))
        .links(links)
        .size([width, height])
        .linkDistance(110)
        .charge(-170)
        .on("tick", tick)
        .start();


    function median(links) {
        // The median of an empty list is null
        if (links.length === 0) return null; 
        
        // Sorting the array makes it easy to find the center, but
        // use `.slice()` to ensure the original array `x` is not modified
        var sorted = links.slice().sort(function (a, b) { return a - b; });
        // If the length of the list is odd, it's the central number
        if (sorted.length % 2 === 1) {
            //console.log("Wtf");
            //console.log(sorted[(sorted.length - 1) / 2].value);
            return sorted[(sorted.length - 1) / 2].value;
        // Otherwise, the median is the average of the two numbers
        // at the center of the list
        } else {
            var a = sorted[(sorted.length / 2) - 1];
            var b = sorted[(sorted.length / 2)];
            //console.log("Ok");
            return (a.value + b.value) / 2;
        }
    };
    
   function isObject(obj) {
     return obj === Object(obj);
    }
 
    // Set the range
    var  v = d3.scale.linear().range([0, 100]);
    // Scale the range of the data
    v.domain([0, d3.max(links, function(d) { return d.value; })]);
    var median_value = 0;

    if(isObject(median_value)){
         median_value = median(links).value;
        //console.log("Its an object");
        }
    else {
        median_value = median(links);
        //console.log("Its not an object");
    }
    
    // console.log(median_value);
    // assign a type per value to encode opacity
    links.forEach(function(link) {
        if ((link.value) <= median_value) {
            link.type = "twofive";
            //console.log("Well "+(link.value));
        } else if ((link.value) > median_value) {
            link.type = "onezerozero";
            //console.log("No " +(link.value));
        }
    });
    //links.forEach(function(link) {
    //  if (v(link.value) <= 5) {
    //      link.type = "twofive";
    //  } else if (v(link.value) <= 10 && v(link.value) > 5) {
    //      link.type = "fivezero";
    //  } else if (v(link.value) <= 50 && v(link.value) > 10) {
    //      link.type = "sevenfive";
    //  } else if (v(link.value) <= 100 && v(link.value) > 50) {
    //      link.type = "onezerozero";
    //  }
    //});

    
    
    var svg = d3.select(".graph").append("svg")
        .attr("width", width)
        .attr("height", height);
    
    //var text=svg
    //.append("text")
    //.text(average/links.length)
    //.attr("y",50);
    // build the arrow.
    
    svg.append("svg:defs").selectAll("marker")
        .data(["end"])      // Different link/path types can be defined here
      .enter().append("svg:marker")    // This section adds in the arrows
        .attr("id", String)
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 15)
        .attr("refY", -1.5)
        .attr("markerWidth", 5)
        .attr("markerHeight", 5)
        .attr("orient", "auto")
      .append("svg:path")
        .attr("d", "M0,-5L10,0L0,5");
    
    // add the links and the arrows
    var path = svg.append("svg:g").selectAll("path")
        .data(force.links())
      .enter().append("svg:path")
        .attr("class", function(d) { return "link " + d.type; })
        .attr("marker-end", "url(#end)");
    
    // define the nodes
    node = svg.selectAll(".node")
        .data(force.nodes())
        .enter().append("g")
        .attr("class", "node")
        .on("click", click)
        .on("dblclick", click)
        .call(force.drag);
    // add the nodes

    // console.log(node);
    
    node.append("circle")
        .attr("r", 7);
    // add the text 
    node.append("text")
        .attr("x", 14)
        .attr("dy", ".45em")
        .text(function(d) { return d.name; });
    // add the curvy lines
    
    function tick() {
        path.attr("d", function(d) {
            var dx = d.target.x - d.source.x,
                dy = d.target.y - d.source.y,
                dr = Math.sqrt(dx * dx + dy * dy);
            return "M" + 
                d.source.x + "," + 
                d.source.y + "A" + 
                dr + "," + dr + " 0 0,1 " + 
                d.target.x + "," + 
                d.target.y;
        });
    
    node.attr("transform", function(d) { 
            return "translate(" + d.x + "," + d.y + ")"; });
    }

    node.attr("name", function(node) {
        return node.name;
    });

    node.attr("active", function(node) {
        return "0";
    });
    
    // action to take on mouse click
    function click() {
        console.log("click triggered");
        var active = d3.select(this).attr("active") == "1";
        if (!active) {
            d3.select(this).attr("active", "1");
            d3.select(this).select("text").transition()
                .duration(150)
                .attr("x", 22)
                .style("fill", "gray")
                .style("stroke", "lightred")
                .style("stroke-width", "2px")
                .style("font", "15px sans-serif");
            d3.select(this).select("circle").transition()
                .duration(150)
                .attr("r", 8)
                .style("fill", "red");
        } else {
            d3.select(this).attr("active", "0");
            d3.select(this).select("text").transition()
                .duration(150)
                .attr("x", 22)
                .style("fill", "black")
                .style("stroke", "black")
                .style("stroke-width", ".5px")
                .style("font", "12px sans-serif");
            d3.select(this).select("circle").transition()
                .duration(150)
                .attr("r", 5)
                .style("fill", "lightgray");
        }
        
    }
    // action to take on mouse double click
    function dblclick() {
        d3.select(this).select("circle").transition()
            .duration(150)
            .attr("r", 7)
            .style("fill", "e");
        d3.select(this).select("text").transition()
            .duration(750)
            .attr("x", 12)
            .style("stroke", "none")
            .style("fill", "black")
            .style("stroke", "none")
            .style("font", "10px sans-serif");
    }
    
    }); // end of svg
}



