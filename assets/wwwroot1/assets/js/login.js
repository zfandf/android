Login = {
    fnInit: function() {
        $.common.ajaxLoad(eLoginBox, "template/temp_login.html", function() {
            changeLangeValue(Lang);
            // 判断手机文件系统是否可用
            if (!isEnabled) {
                $('#J_LoginSubmit, #verificationcode').attr("disabled", true);
                alert(Lang.fileerror);
                return false;
            }

            $('#J_LoginSubmit').click(function(e) {
                e.preventDefault();
                var code = $('#verificationcode').val();
                if (!/^\d{4}$/.test(code)) {
                    $('#J_LoginInfo').removeClass('hide').text(Lang.verificationerror);
                    return;
                }
                Login.fnLogin(code);
            });
            // 如果已经验证过, 则直接通过...
            if ($.common.storage && $.common.storage.verificationcode) {
                $('#verificationcode').val($.common.storage.verificationcode);
                $('#J_LoginSubmit').click();
            }
        });
    },
    fnReload: function() {
        eAllBox.addClass('hide');
        eLoginBox.removeClass('hide');
        if (eLoginBox.children().length > 0) {
            $('#verificationcode').val("");
            $('#J_LoginInfo').addClass('hide');
            $('#J_LoginSubmit, #verificationcode').attr("disabled", false);
        } else {
            Login.fnInit();
        }
    },
    fnLogin: function(code) {
        var oParam = {};
        oParam.data = {
            action: 'verify',
            code: code
        };
        oParam.callback = function(oData) {
            $.common.storage.verificationcode = code;
            if (oData.code == 0) {
                renderMainBox();
            } else {
                $.common.oInfoModal.hide();
                $('#J_LoginInfo').removeClass('hide').text(oData.msg);
            }
        }
        $.common.ajax(oParam);
    }
};