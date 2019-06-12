var clicked = 0;

function toast(text, error) {
	var $toast = $('#toast');
	$toast.removeClass('alert-danger').removeClass('alert-success');
	if (error === undefined) {
		$toast.addClass('alert-success');
	} else {
		$toast.addClass('alert-danger');
	}
	$toast.html(text);
}

function errorFn(errorResponse) {
    var errorText = JSON.stringify(errorResponse);
    LOG.error(errorText);
    toast('ERROR ' + errorText, 'error');
    $('#ready').text('err'); // for selenium synchronisation
}

function successFn(response) {
	toast("ok " + clicked);
    $('#output').text(JSON.stringify(response));
    $('#ready').text('ok'); // for selenium synchronisation
}

var json = {
    "limit" : 10,
    "number" : 20
};

function doSomething() {
    clicked++;
    LOG.info('doSomething is called ' + clicked);
    COMM.get("/rest/simple/hello", successFn, "server-call " + clicked);
}

function doOther1() {
	clicked++;
	LOG.info('doOther is called ' + clicked);
	COMM.get("/rest/simple/hallo", successFn, "server-call " + clicked);
}

function doOther2() {
	clicked++;
	LOG.info('doOther is called ' + clicked);
	json.number = clicked;
	COMM.json("/rest/json/delegate", json, successFn, "server-call " + clicked);
}

function init() {
    COMM.setErrorFn(errorFn);
    $('#doSomething').onWrap('click', doSomething); // use this instead of on for robust error handling
    $('#doOther1').onWrap('click', doOther1);
    $('#doOther2').onWrap('click', doOther2);
};

$(document).ready(WRAP.fn3(init, 'page init'));