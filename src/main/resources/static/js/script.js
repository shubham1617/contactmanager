
console.log("Welcome to Contact Manager")

const toggleSidebar = () => {

    if ($(".sidebar").is(":visible")) {
        $(".sidebar").css("display", "none");
        $(".content").css("margin-left", "0px");
    }
    else {
        $(".sidebar").css("display", "block");
        $(".content").css("margin-left", "20%");
    }
};


