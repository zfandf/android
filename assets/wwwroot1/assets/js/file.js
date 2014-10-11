var WIN_CTRL_KEYCODE = 17,
    MAC_CTRL_KEYCODE = 92;
var isMultiple = false; // 是否处于多选状态

List = {
    sFileTmpl: null,

    fnInit: function() {
        List.sFileTmpl = $('#J_FileTmpl').html();

        List.fnInitEvent();
    },
    fnInitEvent: function() {
        $(window).off("keydown", "keyup").on({
            keydown: function(e) {
                isMultiple = (e.keyCode == WIN_CTRL_KEYCODE || e.keyCode == MAC_CTRL_KEYCODE) ? true : false;
            },
            keyup: function(e) {
                isMultiple = false;
            }
        });

        eList.off('click').on('click', function(e) {
            e.preventDefault();
            var eTag = $(e.target);

            // cancel create new folder
            if (eTag.is('#J_CreateCancel, #J_CreateCancel *')) {
                eTag.parents('.defaultstyle').remove();
                return;
            }

            // submit create new folder
            if (eTag.is('#J_CreateOk')) {
                Operate.fnCreateDir();
                return;
            }

            // click folder name link or folder icon open the folder
            if (eTag.is('a.dir-link, .grid .dir-icon')) {
                eTag = eTag.parents('.defaultstyle');
                var path = eTag.attr('path');
                var name = eTag.find('.file-name').text();
                $('.breadcrumb').append('<li class="J_CrumbPath" path="'+path+'"><a href="#">'+name+'</a></li>');
                List.fetchList(path);
                return;
            }

            // 点击选择框
            if (eTag.is('.chk-box, .chk-box *')) {
                eTag = eTag.parents('.defaultstyle');
                if (eTag.hasClass('selected')) {
                    eTag.removeClass('selected');
                } else {
                    eTag.addClass('selected');
                }
            } else if (eTag.not('#J_NewDirName')) {
                eTag = eTag.is('.defaultstyle') ? eTag : eTag.parents('.defaultstyle');
                if (eTag.find('#J_NewDirName').length > 0) {
                    return;
                }

                if (isMultiple) {
                    if (eTag.hasClass('selected')) {
                        eTag.removeClass('selected');
                    } else {
                        eTag.addClass('selected');
                    }
                } else {
                    var flag = false;
                    if (!eTag.hasClass('selected') || eList.find('.selected').length > 1) {
                        flag = true;
                    }
                    eList.find('.selected').removeClass('selected');
                    if (flag) {
                        eTag.addClass('selected');
                    }
                }
            }
            /* if more 0 file be selected, operate button can be use , else button cann't use */
            if (eList.find('.defaultstyle.selected').length > 0) {
                eOperateBtns.removeClass('novisiable');
            } else {
                eOperateBtns.addClass('novisiable');
            }
            /* if all file be selected, the select all button be checked, else the button is not checked */
            if (eList.find('.defaultstyle.selected').length == eList.find('.defaultstyle').length) {
                $('#J_SelectAll').addClass('selected');
            } else {
                $('#J_SelectAll').removeClass('selected');
            }
            GO.renderFooter();
        });

        $('#J_SelectAll').off('click').on('click', function() {
            if ($(this).hasClass('selected')) {
                $(this).removeClass('selected');
                eList.find('.defaultstyle.selected').removeClass('selected');
                eOperateBtns.addClass('novisiable');
            } else {
                $(this).addClass('selected');
                eList.find('.defaultstyle').addClass('selected');
                eOperateBtns.removeClass('novisiable');
            }
            GO.renderFooter();
        });
    },

    oFileIcon: {
        zip: 'zip',
        mp3: 'midi',
        doc: 'doc',
        pdf: 'pdf',
        xls: 'xls',
        ppt: 'ppt',
        txt: 'text',
        png: 'jpg',
        jpg: 'jpg',
        apk: 'apk',
        php: 'code',
        js: 'code',
        css: 'code',
        html: 'code',
        file: 'file'
    },

    fetchList: function(path, hideLoading) {
        if (!path) {
            path = $('.J_CrumbPath:last').attr('path');
        }
        var oParam = {};
        oParam.data = {
            action: 'file',
            path: path
        };
        oParam.callback = function(oData) {
            Modal.isRefresh = false;
            if (oData.code != 0) {
                return;
            }
            var str = '';
            for (var i = 0; i < oData.files.length; i++) {
                var file = oData.files[i];
                file.cls = file.type;
                if (file.type == 'file') {
                    file.suffix = file.suffix.toLowerCase();
                    file.type = List.oFileIcon[file.suffix] || List.oFileIcon['file'];
                }
                str += $.common.setTemplate(List.sFileTmpl, file);
            }
            eList.html(str);
            eOperateBtns.addClass('novisiable');
            $('#J_SelectAll').removeClass('selected');

            // if in gen dir, refresh phone info, else get folder info
            if ($('.J_CrumbPath').length == 1) {
                GO.getInfo('info', true);
            } else {
                GO.renderFooter();
            }
        }
        $.common.ajax(oParam, hideLoading);
    }
};