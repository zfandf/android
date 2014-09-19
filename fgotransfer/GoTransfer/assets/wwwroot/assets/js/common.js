$.common = {
    storage: (window.localStorage) ? window.localStorage : false,
    oLoading: {
        show: function(content) {
            $('#J_LoadingModal').modal({
                backdrop:false,
                show: true
            });
            if (content) {
                $('#site-loading').html(content);
            }
        },
        hide: function() {
            $('#J_LoadingModal').modal('hide');
            $('#site-loading').html('Loading');
        }
    },
    oInfoModal: {
        show: function(content, withHeader, showBtn, fnSubmit) {
            var showHeader = (withHeader == undefined) ? true : withHeader,
                showFooter = (showBtn == undefined) ? false : showBtn;
            var eModal = $('#J_ErrorModal'),
                eModalHeader = eModal.find('.modal-header'),
                eModalFooter = eModal.find('.modal-footer'),
                eModalBody = eModal.find('.J_ModalBody'),
                eErrorBtn = eModalFooter.find('.J_ErrorModal_Close'),
                eCloseBtn = eModal.find('.J_ModalClose'),
                eSubmitBtn = eModalFooter.find('.J_ModalSubmit');

            var modalParam = {
                show: true,
                keyboard: false
            };
            if (withHeader || showBtn) {
                modalParam.backdrop = 'static';
            }

            eModal.modal(modalParam);
            content = content || Lang.networkerror;
            eModalBody.html(content);

            if (false === showHeader) {
                eModalHeader.hide();
            } else {
                eModalHeader.show();
            }

            if (true === showBtn) {
                eModalFooter.removeClass('hide');
                if (fnSubmit) {
                    eErrorBtn.addClass('hide');
                    eCloseBtn.removeClass('hide');
                    eSubmitBtn.removeClass('hide');
                } else {
                    eErrorBtn.removeClass('hide');
                    eCloseBtn.addClass('hide');
                    eSubmitBtn.addClass('hide');
                }
            } else {
                eModalFooter.addClass('hide');
            }

            eErrorBtn.off('click').on('click', function() {
                eModal.modal('hide');
            });
            eCloseBtn.off('click').on('click', function() {
                eModal.modal('hide');
            });
            eSubmitBtn.off('click').on('click', function() {
                fnSubmit();
            });
        },
        hide: function() {
            $('#J_ErrorModal').modal('hide');
            $('#J_ErrorModal .J_ModalBody').html('');
            $.common.oLoading.hide();
        }
    },

    ajax: function (oParam, hideLoading) {
        oParam.data = oParam.data || {action: 'file'};
        var callback = oParam.callback || null;
        var type = oParam.type || 'POST';
        var loadingText = oParam.loadingText || Lang.loading;

        oParam.data['timestamp'] = new Date().getTime();
        return jQuery.ajax({
            type: type,
            url: GO.host,
            async: (oParam.data.action != 'get_country'),
            data: oParam.data,
            // timeout: 5000,
            beforeSend: function(jqXHR, settings) {
                // show loading
                var action = oParam.data.action;
                if (!hideLoading) {
                    if (action == 'move' || action == 'copy' || action == 'delete') {
                        loadingText = '<br>' + Lang[action+'waiting'] + '<br><br>';
                    }
                    $.common.oLoading.show(loadingText);
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                // show error msg
                var msg = Lang.networkerror + "<br><br>";
                msg += '<button class="btn btn-default" onclick="$.common.checkNetwork();">'+Lang.checknetwork+'</button>'
                    + '&nbsp;&nbsp;&nbsp;<a href="javascript:$.common.oInfoModal.hide();GO.loginModal.show();">'+Lang.connectagain+'</a>';
                $.common.oInfoModal.show(msg, true, false);
            },
            complete: function (jqXHR, textStatus) {
                Modal.isRefresh = true;
                // hide loading
                if (!hideLoading) {
                    $.common.oLoading.hide();
                }
                
                if (jqXHR.responseText) {
                    if (callback) {
                        var oData = $.parseJSON(jqXHR.responseText);
                        if (oData.code && oData.code != 0) {
                            // J_ErrorText
                            $.common.oInfoModal.show(oData.msg);
                        } else {
                            $.common.oInfoModal.hide();
                        }
                        callback($.parseJSON(jqXHR.responseText));
                    }
                }
            }
        });
    },

    checkNetwork: function() {
        var oParam = {};
        oParam.data = {action: 'info'};
        oParam.callback = function(oData) {
            if (oData.storages) {
                GO.oInfo = oData;
                $.common.oInfoModal.show(Lang.networkok, false);
            }
        }
        $.common.ajax(oParam, true);
    },

    setTemplate: function(sTmpl, oVar){
        var ary = [];
        var more = "", str = "";
        if ((Array.isArray && Array.isArray(oVar)) || oVar instanceof Array) {
            ary = oVar;
        } else if ('object' == typeof(oVar)) {
            ary[0] = oVar;
        }
        for (var i in ary) {
            str = sTmpl;
            for (var prop in ary[i]) {
                var r = new RegExp('{'+prop+'}', 'g');
                var v = ary[i][prop];
                str = str.replace(r, v);
            }
            more += str;
        }
        return more;
    },
    // filter folder or file name
    // illegal file name <,>,|,*,?,",/,\
    filterName: function(name) {
        if (!name.trim()) {
            $.common.oInfoModal.show(Lang.nameempty);
            return;
        }
        var reg = /[<>|\*\?\"\/\\,]/;
        if (!reg.test(name)) {
            return true;
        }
        $.common.oInfoModal.show(Lang.nameerror);
    }
};
