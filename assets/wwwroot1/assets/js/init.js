var CountryLanguage = 'en',
    HOST = 'http://192.168.10.141:9999';
var Lang, ActionType;
var isEnabled = true;
var eAllBox, eLoginBox, eMainBox;

(function() {
    // 检测浏览器当前语言
    if ((navigator.userLanguage || navigator.language).toLowerCase() == 'zh-cn') {
        CountryLanguage = 'cn';
    } else {
        CountryLanguage = 'en';
    }
    getCountry(); 
})();

window.onload = function() {
    eAllBox = $('.J_Box');
    eLoginBox = $('#J_LoginBox');
    eMainBox = $('#J_MainBox');

    // 改变首次展示内容的语言
    // renderLoginBox();
    renderMainBox();
}

// 进入首先看到登录界面
function renderLoginBox() {
    if (Lang) {
        Login.fnInit();
    } else {
        var timer = window.setInterval(function() {
            window.clearInterval(timer);
            renderLoginBox();
        }, 500);
    }
}

function renderMainBox() {
    if (typeof Main != 'undefined') {
        Main.fnInit();
    } else {
        $.common.ajaxLoad(eMainBox, "template/temp_main.html", function() {
            Main.fnInit();
        });
    }
}

// 请求手机所用语言 TODO
function getCountry() {
    var oParam = {};
    oParam.url = HOST;
    oParam.data = {action: 'get_country'};
    // oParam.isAsync = false;
    sendXHR(oParam, function(oData) {
        CountryLanguage = oData.country || CountryLanguage;
        Lang = language[CountryLanguage];
        if (!oData.enable) {
            isEnabled = false;
        }
    });
}

// 改变语言
function changeLangeValue(oLang) {
    if ($) {
        $('[langkey]').each(function() {
            var key = $(this).attr('langkey');
            if (oLang[key]) {
                $(this).text(oLang[key]).removeAttr('langkey');
            }
        });
    }
}

/*
 * 发送ajax请求 TODO
 * 
 */
function sendXHR(oParam, successFunc, errorFunc) {
    
    var xhr = createCORSRequest(oParam);
    if (xhr) {
        xhr.onload = function() {
            var oData = JSON.parse(xhr.responseText);
            successFunc(oData);
        };
        xhr.onerror = function() {
            alert('unsuccessful:' + xhr.status); 
        }

        xhr.send();
    }
    
}

function createCORSRequest(oParam){
    var method = oParam.method || 'GET';
    var isAsync = oParam.isAsync;
    var url = oParam.url;
    if (method.toLowerCase() == 'get') {
        var data = [];
        for (x in oParam.data) {
            data.push(x + "=" + oParam.data[x]);
        }
        url += "?" + data.join('&');
    }

    var xhr = new XMLHttpRequest();
    if ("withCredentials" in xhr) {
        xhr.open(method, url, isAsync);
    } else if (typeof XDomainRequest != "undefined") {
        xhr = new XDomainRequest();
        xhr.open(method, url);
    } else {
        xhr = null;
    }
    return xhr;
}