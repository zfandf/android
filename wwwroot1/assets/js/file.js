var WIN_CTRL_KEYCODE = 17,
    MAC_CTRL_KEYCODE = 92;
var isMultiple = false; // 是否处于多选状态

/* bread crumb*/
var CrumbNav = {
    init: function() {
        CrumbNav.initData();
        CrumbNav.initEvent();
    },
    initEvent: function() {
        $('.breadcrumb').click(function(e) {
            var eTag = $(e.target);
            if (eTag.is('.J_CrumbPath, .J_CrumbPath *')) {
                eTag = eTag.hasClass('J_CrumbPath') ? eTag : eTag.parents('.J_CrumbPath');
                var path = eTag.attr('path');
                List.fetchList(path);
                eTag.nextAll().remove();
            } else if (eTag.is('.J_HomeType_First, .J_HomeType_First *')) {
                eTag = eTag.is('.J_HomeType_First') ? eTag : eTag.parents('.J_HomeType_First');
                $('.J_CrumbPath').remove();
                $('.breadcrumb').append('<li class="J_CrumbPath" path="'+path+'"><a href="#">'+name+'</a></li>');
            }
            
        });
        // init switch view type button
        $('.J_ViewType').click(function() {
            $('.J_ViewType').removeClass('disabled');
            $(this).addClass('disabled');
            var type = $(this).attr('view-type');
            $('#J_FilesList').removeClass('list grid');
            $('#J_FilesList').addClass(type);
        });
        // init change storage
        $('.J_Storage').on('change', function() {
            CrumbNav.init();
        })
    },
    initData: function() {
        var path = $('.J_Storage').val(),
            name = $('.J_Storage option:selected').text();
        $('.J_CrumbPath').remove();
        $('.breadcrumb').append('<li class="J_CrumbPath" path="'+path+'"><a href="#">'+name+'</a></li>');
        List.fetchList(path);
    },

    renderFooter: function() {
        var dirCount = $('.defaultstyle.dir.selected').length;
        var fileCount = $('.defaultstyle.file.selected').length;
        if (dirCount > 0 || fileCount > 0) {
            $('#J_StorageStatus').text(Lang.selected + dirCount + Lang.unit + Lang.folder + ', ' + fileCount+ Lang.unit + Lang.file);
            return;
        }
        var crumbPath = $('.J_CrumbPath:last').attr('path');
        var storagePath = $('.J_Storage').val();
        if (crumbPath == storagePath || !crumbPath) {
            var storages = List.oInfo.storages;
            for (var i = 0; i < storages.length; i++) {
                var storage = storages[i];
                if (storagePath == storage.path) {
                    $('#J_StorageStatus').text(storage.name + '(' + Lang.total + storage.total_space + ', ' + Lang.free + storage.free_space+')');
                    break;
                }
            }
        } else {
            var dirCount = $('.defaultstyle.dir').length;
            var fileCount = $('.defaultstyle.file').length;
            $('#J_StorageStatus').text(Lang.total + dirCount + Lang.unit + Lang.folder + ', ' + fileCount + Lang.unit + Lang.file);
        }
    }
};

List = {
    oInfo: {},
    sFileTmpl: null,

    fnInit: function() {
        List.sFileTmpl = $('#J_FileTmpl').html();
        List.fnInitEvent();
        List.getInfo(ActionType);
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
            CrumbNav.renderFooter();
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
            CrumbNav.renderFooter();
        });
    },

    getInfo: function(action, refresh) {
        action = action || 'info';
        var oParam = {};
        oParam.data = {action: action};
        oParam.callback = function(oData) {
            if (oData.storages) {
                List.oInfo = oData;
                if (!refresh) {
                    var content = '';
                    for (var i = oData.storages.length -1 ; i >=0 ; i --) {
                        content += '<option value="' + oData.storages[i].path +'">' + oData.storages[i].name + '</option>';
                    }
                    $('.J_Storage').html(content);
                    CrumbNav.init();
                    $('#J_MyDevice').text(oData.phone_name);
                    $('#J_Version').text(oData.v);
                    HOST = oData.ip;
                } else {
                    CrumbNav.renderFooter();    
                }
            }
        }
        $.common.ajax(oParam, refresh);
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

            // 当在根目录下时， 重新获取用户手机存储信息， 并显示在footer下， 否则将文件夹信息显示在footer
            if ($('.J_CrumbPath').length == 1) {
                List.getInfo('info', true);
            } else {
                CrumbNav.renderFooter();
            }
        }
        $.common.ajax(oParam, hideLoading);
    }
};