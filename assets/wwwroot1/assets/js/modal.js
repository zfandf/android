
var Modal = {

    isRefresh: false, // whether or not refresh list

    init: function() {
        Modal.eClose.click(function() {
            if (Modal.eBody.find('#J_UploadCancelAll').length > 0) {
                $('#J_UploadCancelAll').click();
            }
            Modal.closeDialog();
            if (Modal.isRefresh) {
                List.fetchList();
            }
            Modal.isRefresh = false;
        });
    },

    /*
     * create modal
     * input: modalData <object> {
        sign: modal唯一标记，如果没有sign， 则每次都重新渲染modal
        title: modal 标题，默认为空，modal-header不显示
        body: modal-body 部分， 必须
        fnSubmit: <function> modal-footer提交按钮事件，默认无，modal-footer不显示
        fnInit: <function> modal渲染完毕之后布局初始化，默认无
     }
     * output: <object> {
        show: <function> 打开modal
        hide: <function> 关闭modal
     }
     */
    createNew: function(modalData) {
        console.log(modalData);
        return {
            show: function() {

                Modal.openDialog(modalData);
            },
            hide: function() {
                Modal.closeDialog(modalData);
            }
        }
    },

    eDialog: $('#J_ModalDialog'),
    eTitle: $('#J_ModalTitle'),
    eBody: $('#J_ModalBody'),
    eSubmit: $('#J_ModalSubmit'),
    eClose: $('#J_ModalDialog .J_ModalClose'),
    eHeader: $('#J_ModalDialog .modal-header'),
    eFooter: $('#J_ModalDialog .modal-footer'),

    /*
     * notice view
     * msg <string> notice message
     * type <string> notice type: danger/primary/success/info/warning
     */
    fnNotie: function(msg, type) {
        Modal.eState.text(msg);
        type = type || 'danger';
        var cls = "bg-"+type;
        Modal.eState.removeClass('hide bg-primary bg-success bg-info bg-warning bg-danger');
        Modal.eState.addClass(cls);
    },
    /*
     * notice hide
     */
    fnNotieHide: function() {
        Modal.eState.addClass('hide');
    },
    /*
     * excute after modal success
     */
    fnShown: function() {
        Modal.eState = $('.J_ModalState');
    },

    openDialog: function(modalData) {
        if (modalData.sign != 'upload' && Modal.eDialog.find('#J_UploadBody').length == 1) {
            $('body').append($('#J_UploadBody'));
            $('#J_UploadBody').addClass('hide');
        } else if (modalData.sign != 'copy' && modalData.sign != 'move' && Modal.eFooter.find('.J_CreateNewTree').length == 1) {
            Modal.eFooter.find('.J_CreateNewTree').remove();
        }
        if (modalData.sign) {
            if (Modal.eDialog.attr('sign') == modalData.sign) {
                Modal.eDialog.modal('show');
                return;
            }
        } else {
            modalData.sign = null;
        }
        Modal.eDialog.attr('sign', modalData.sign);
        if (modalData.title) {
            Modal.eHeader.removeClass('hide');
            Modal.eTitle.html(modalData.title);
        } else {
            Modal.eHeader.addClass('hide');
        }

        Modal.eBody.html(modalData.body);
        Modal.eSubmit.off('click');
        if (modalData.fnSubmit) {
            Modal.eFooter.removeClass('hide');
            Modal.eSubmit.on('click', function() {
                Modal.eSubmit.attr('disabled', true);
                modalData.fnSubmit();
                Modal.eSubmit.attr('disabled', false);
            });
        } else {
            Modal.eFooter.addClass('hide');
        }

        Modal.eDialog.modal({
            backdrop: 'static',
            keyboard: false,
            show: true,
            'aria-hidden': true
        });
        if (modalData.fnInit) {
            modalData.fnInit();
        }
        Modal.fnShown();
    },

    closeDialog: function() {
        if ($('#J_UploadBox').hasClass('upload-box')) {
            $('#J_UploadBtnText').text(Lang.uploadbtn);
            $('#J_UploadBox').removeClass('upload-box');
        }
        Modal.eDialog.modal('hide');
    }
};