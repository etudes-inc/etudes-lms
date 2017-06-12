// Title: Tigra Calendar
// URL: http://www.softcomplex.com/products/tigra_calendar/
// Version: 3.2 (American date format)
// Date: 10/14/2002 (mm/dd/yyyy)
// Note: Permission given to use this script in ANY kind of applications if
//    header lines are left unchanged.
// Note: Script consists of two files: calendarnotime.js and calendar_notime.html

// if two digit year input dates after this year considered 20 century.
var NUM_CENTYEAR = 30;
// are year scrolling buttons required by default
var BUL_YEARSCROLL = true;
var calendars = [];
var RE_NUM = /^\-?\d+$/;

function calendarnotime(obj_target) {

	// assigning methods
	this.gen_date = ambrosia_format_date; //cal_gen_date2;
	this.prs_date = ambrosia_parse_date; // cal_prs_date2;
	this.prs_tsmp = ambrosia_parse_timeStamp; // cal_prs_tsmp2;
	this.popup    = cal_popup2;
	this.month_names = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
	this.gen_now = ambrosia_now;

	// validate input parameters
	if (!obj_target)
		return cal_error("Error calling the calendar: no target control specified");
	if (obj_target.value == null)
		return cal_error("Error calling the calendar: parameter specified is not valid target control");
	this.target = obj_target;
	this.year_scroll = BUL_YEARSCROLL;
	
	// register in global collections
	this.id = calendars.length;
	calendars[this.id] = this;
}

function ambrosia_now()
{
	var rv = new Date();
	rv.setHours(12);
	rv.setMinutes(0);
	rv.setSeconds(0);
	rv.setMilliseconds(0);
	return rv;
}

function cal_popup2 (str_date) {
	this.dt_current = this.prs_tsmp(str_date ? str_date : this.target.value);

	if (!this.dt_current) return;

	var obj_calwindow = window.open(
		'/sakai-site-manage-tool/calendar/calendar_notime.html?date=' + this.dt_current.valueOf()+
		'&id=' + this.id,
		'Calendar', 'width=200,height=190,status=no,resizable=no,top=200,left=200,dependent=yes,alwaysRaised=yes'
	);
	obj_calwindow.opener = window;
	obj_calwindow.focus();
}

function ambrosia_format_date(timeStamp)
{
	var rv = this.month_names[timeStamp.getMonth()];
	rv += " ";
	rv += timeStamp.getDate();
	rv += ", ";
	rv += timeStamp.getFullYear();
	return rv;
}

function ambrosia_parse_timeStamp(displayStr)
{
	if (displayStr == null) return this.gen_now();

    var time = parseInt(displayStr, 10);
	if (!isNaN(time) && (time >= 0)) return new Date(time);
	
	var displayParts = displayStr.split(" ");
	if (displayParts.length != 5) this.gen_now();

	var datePart = displayParts[0] + " " + displayParts[1] + " " + displayParts[2];
	return this.prs_date(datePart);
}

function ambrosia_parse_date(displayStr)
{
	var displayParts = displayStr.split(" ");
	var rv = this.gen_now();
	//var rv = new Date();
	if (displayParts.length == 3)
	{
		var month = -1;
		for (var i = 0; i <= 11; i++)
		{
			if (this.month_names[i].toLowerCase() == displayParts[0].toLowerCase())
			{
				month = i;
				break;
			}
		}
		if (month == -1) return rv;

		var day = parseInt(displayParts[1], 10);
		if (isNaN(day)) return rv;
		
		var year = parseInt(displayParts[2], 10);
		if (isNaN(year)) return rv;

		//rv.setYear(year);
		//rv.setMonth(month);
		//rv.setDate(day);
		rv.setFullYear(year,month,day);
	}

	return rv;
}


function cal_error (str_message) {
	//alert (str_message);
	return null;
}
