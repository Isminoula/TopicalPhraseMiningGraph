function process(data) {
    var final_arr = data.split("\n");
    displayphrases(final_arr);
}

function displayphrases(final_arr) {
    $(".phraselist").empty();
    for (var i in final_arr) {
        var phrase = final_arr[i].split(",");
        phrase = phrase.join(",");
        text = "<p class='phrase'>" + phrase + "</p>"
        $(".phraselist").append(text);
    }
}

function updatecsv(fname) {
    $.ajax({
        type: "GET",
        url: fname,
        dataType: "text",
        success: function(data) {
            process(data);
        }
     });
}

$(document).ready(function() {
    updatecsv("phrases1.csv");

    $.getScript("app.js", function() {
        generateD3graph("force1.csv");
        $('.info').css('min-height', 0.05*$(document).height());

        $(".phraselist").on("click", ".phrase", function() {
            $(".phrase").css("color", "gray");
            $(this).css("color", "red");
            var nodenames = $(this).text().split(",");
            // console.log(nodenames);
            var nodeobjects = getNodeObjects(nodenames);
            d3.selectAll("circle").style("fill","lightgray").attr('r',5);
            d3.selectAll(nodeobjects).selectAll("circle").transition(200).style("fill","lightgreen").attr("r",10);
        });

        $("select").change(function() {
            var selected = $(this).val();
            updatecsv("phrases"+selected+".csv");
            $(".graph").empty();
            generateD3graph("force"+selected+".csv");
        });
    });



    //     



});