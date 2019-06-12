var COMM = {};
(function($) {
    /**
     * default error function
     */
    var errorFn = function(response) {
        LOG.error('COMM errorfn is called. Data follows: ' + response);
    };

    /**
     * set a error fn. A error function must accept one parameter: the response.
     */
    COMM.setErrorFn = function(newErrorFn) {
        errorFn = newErrorFn;
    };

    /**
     * URL-encode a JSON object, issue a GET and expect a JSON object as response.
     */
    COMM.get = function(url, successFn, message) {
        return $.ajax({
            url : url,
            type : 'GET',
            success : WRAP.fn3(successFn, message),
            error : errorFn
        });
    };

    /**
     * POST a JSON object as ENTITY and expect a JSON object as response.
     */
    COMM.json = function(url, data, successFn, message) {
        return $.ajax({
            url : url,
            type : 'POST',
            contentType : 'application/json; charset=utf-8',
            dataType : 'json',
            data : JSON.stringify(data),
            success : WRAP.fn3(successFn, message),
            error : errorFn
        });
    };

    /**
     * redirect the browser to the URL given as parameter. Report that to the server.
     */
    COMM.totoUrl = function(url) {
        LOG.info('document.location.href = ' + url);
        document.location.href = url;
    };
})($);

var LOG = {};
(function($) {
    var markerINFO = '[[INFO]] ';
    var markerERROR = '[[ERR]] ';

    /**
     * log info text to the console
     */
    LOG.info = function(obj) {
        LOG.text(obj, markerINFO);
    };

    /**
     * log error text to the console
     */
    LOG.error = function(obj) {
        LOG.text(obj, markerERROR);
    };

    /**
     * log to the console
     * 
     * @memberof LOG
     */
    LOG.text = function(obj, marker) {
        if (marker === undefined) {
            marker = markerINFO;
        }
        console.log(marker + obj);
    };
})($);

var WRAP = {};
(function($) {
    /**
     * wrap a function with up to 3 parameters (!) to catch and display errors. An not undefined 2nd parameter is a log message
     * 
     * @memberof WRAP
     */
    WRAP.fn3 = function(fnToBeWrapped, message) {
        var wrap = function(p0, p1, p2) {
            var markerTIMER = '[[TIME]] ';
            var start = new Date();
            try {
                fnToBeWrapped(p0, p1, p2);
                if (message !== undefined) {
                    var elapsed = new Date() - start;
                    LOG.text(elapsed + " msec: " + message, markerTIMER);
                }
            } catch (e) {
                if (message !== undefined) {
                    var elapsed = new Date() - start;
                    LOG.error(markerTIMER + elapsed + " msec: " + message + ", then EXCEPTION: " + e);
                } else {
                    LOG.error("fn3 caught an EXCEPTION: " + e);
                }
            }
        };
        return wrap;
    };
})($);

/**
 * add "onWrap" to jquery. It provides a safe, wrapped version of the on(event,callbackOrFilter,callbackOrMessage) method
 * 
 * @memberof JQUERY
 */
(function($) {
    $.fn.onWrap = function(event, callbackOrFilter, callbackOrMessage, optMessage) {
        if (typeof callbackOrFilter === 'string') {
            if (typeof callbackOrMessage === 'function') {
                return this.on(event, callbackOrFilter, WRAP.fn3(callbackOrMessage, optMessage));
            } else {
                LOG.error("illegal wrapping. Parameter: " + event + " ::: " + callbackOrFilter + " ::: " + callbackOrMessage + " ::: " + optMessage);
            }
        } else if (typeof callbackOrFilter === 'function') {
            if (typeof callbackOrMessage === 'string' || callbackOrMessage === undefined) {
                return this.on(event, WRAP.fn3(callbackOrFilter, callbackOrMessage));
            } else {
                LOG.error("illegal wrapping. Parameter: " + event + " ::: " + callbackOrFilter + " ::: " + callbackOrMessage + " ::: " + optMessage);
            }
        }
    };
})($);
