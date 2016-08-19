var menu;
var sectionInfo;
var sectionFeatures;
var sectionHow;
var sectionContribute;
var sectionInfoScroll;
var sectionFeaturesScroll;
var sectionHowScroll;
var sectionContributeScroll;
//var timeout = null;
var marginOffset = 200;
var scrollAnimationDuration = 1000;

/**
 *	Gets sections current offsets
 */
function getSectionsOffset() {
	sectionInfo = $("#content-info").offset().top - marginOffset;
	sectionFeatures = $("#content-features").offset().top - marginOffset;
	sectionHow = $("#content-howtouse").offset().top - marginOffset;
	sectionContribute = $("#content-contribute").offset().top - marginOffset;
	
	sectionInfoScroll = $("#content-info").offset().top;
	sectionFeaturesScroll = $("#content-features").offset().top;
	sectionHowScroll = $("#content-howtouse").offset().top;
	sectionContributeScroll = $("#content-contribute").offset().top;
}

$(window).on('load', function() {
	//Get menu and sections position on load
	menu = $(".menuPlaceholder").offset().top;
	getSectionsOffset();
	
	//Set menu initial position
	if ($(window).scrollTop() >= menu) {
		$(".menu").addClass("fixed");
		$(".content-a:first-child").addClass("menuMargin");
	} else {
		$(".menu").removeClass("fixed");
		$(".content-a:first-child").removeClass("menuMargin");
	}
	
	//Set events
	//Open menu on click
	$("#btnMenu").click(function() {
		if ($(".menu ul").hasClass("expanded")) {
			$("#btnMenu").removeClass("opened");
			$(".menu ul").removeClass("expanded");
		} else {
			$("#btnMenu").addClass("opened");
			$(".menu ul").addClass("expanded");
		}
	});
	
	//Animated scroll to section on menu buttons click
	$("#menuInfo").click(function() {
		$('html, body').animate({
			scrollTop: sectionInfoScroll
		}, scrollAnimationDuration);
		
		return false;
	});
	
	$("#menuFeatures").click(function() {
		$('html, body').animate({
			scrollTop: sectionFeaturesScroll
		}, scrollAnimationDuration);
		
		return false;
	});
	
	$("#menuHow").click(function() {
		$('html, body').animate({
			scrollTop: sectionHowScroll
		}, scrollAnimationDuration);
		
		return false;
	});
	
	$("#menuContribute").click(function() {
		$('html, body').animate({
			scrollTop: sectionContributeScroll
		}, scrollAnimationDuration);
		
		return false;
	});
});
$(window).on('resize', function() {
	//Get menu and sections position on window resize
	menu = $(".menuPlaceholder").offset().top;
	getSectionsOffset();
});

$(window).scroll(function () {
    //Make menu floating if needed
	if ($(window).scrollTop() >= menu) {
		$(".menu").addClass("fixed");
		$(".content-a:first").addClass("menuMargin");
	} else {
		$(".menu").removeClass("fixed");
		$(".content-a:first").removeClass("menuMargin");
	}
	
	//Check current section to update menu selected elementFromPoint
	if ($(window).scrollTop() >= sectionContribute) {
		$(".menuSelected").removeClass("menuSelected");
		$("#menuContribute").addClass("menuSelected");
	} else if ($(window).scrollTop() >= sectionHow) {
		$(".menuSelected").removeClass("menuSelected");
		$("#menuHow").addClass("menuSelected");
	} else if ($(window).scrollTop() >= sectionFeatures) {
		$(".menuSelected").removeClass("menuSelected");
		$("#menuFeatures").addClass("menuSelected");
	} else if ($(window).scrollTop() >= sectionInfo) {
		$(".menuSelected").removeClass("menuSelected");
		$("#menuInfo").addClass("menuSelected");
	} else {
		$(".menuSelected").removeClass("menuSelected");
	}
	
	/*if (!timeout) {
        timeout = setTimeout(function () {            
            clearTimeout(timeout);
            timeout = null;            
			
			//Code here
        }, 250);
    }*/
});