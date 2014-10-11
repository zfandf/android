$(document).ready(function() {
    Modal.init();
    GO.loginModal.show();
});
var Lang = language.en;

/* login */
var Login = {
    sign: 'login',
    body: $('#J_LoginTmpl').html(),
    autoClose: false,
    fnInit: function() {
        GO.initLanguage();
        $('#verificationcode').focus(function() {
            $('#J_LoginInfo').addClass('hide');
        });
        if (!GO.is_enable) {
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
        if (sessionStorage && sessionStorage.getItem(verificationcode)) {
            $('#verificationcode').val(sessionStorage.getItem(verificationcode));
            $('#J_LoginSubmit').click();
        }
    },
    fnLogin: function(code) {
        var oParam = {};
        oParam.data = {
            action: 'verify',
            code: code
        };
        oParam.callback = function(oData) {
            if (sessionStorage) {
                sessionStorage.setItem(verificationcode, code);
            }
            $.common.storage.verificationcode = code;
            if (oData.code == 0) {
                GO.loginModal.hide();
                if (!GO.oInfo || !GO.oInfo.storages) {
                    GO.init();
                }
            } else {
                $.common.oInfoModal.hide();
                $('#J_LoginInfo').removeClass('hide').text(oData.msg);
            }
        }
        $.common.ajax(oParam);
    }
};

/* all operate logic*/
var Operate = {
    newDirTmpl: $('#J_NewDirTmpl').html(),// create new dir template
    eOperateBtns: $('.J_OperateBtns'),// all operate buttons

    copyModal: null, // modal for copy
    moveModal: null, // modal for copy
    uploadModal: null,// modal for upload

    deleteModal: null,// 文件删除确认 modal

    // initialize operate event
    init: function() {
        // default all buttons not be work
        Operate.eOperateBtns.find('.btn').addClass('disabled');
        
        $('#J_NewForm').submit(function(e) {
        	e.preventDefault();
        	Operate.fnCreateDir();
        });

        // click event: open upload dialog for select files
        $('#J_Upload').uploadify({
            'auto': false,
            'buttonText': '<span class="glyphicon glyphicon-open"></span><span id="J_UploadBtnText">'+ (Lang.uploadbtn || 'Upload Files') + '</span>',
            'buttonClass': 'btn btn-success',
            'removeCompleted' : false,
            'swf'      : 'assets/uploadify.swf',
            'uploader' : GO.host,
            'onSelect': function(file) {
                
                var path = $('.J_CrumbPath:last').attr('path');
                path = GO.host + '?action=upload&path='+path+'&radum='+(new Date().getTime());
                $('#J_UploadCancelAll').removeClass('hide');
                $('#J_UploadComplete').addClass('hide');

                $('#J_Upload').uploadify("settings", "uploader", path);

                $('#'+file.id).find('.cancel a').remove();
                $('#'+file.id).find('.cancel').append('<span style="float:right;" class="J_Cancel glyphicon glyphicon-remove"></span>');
                $('#'+file.id).find('.cancel .J_Cancel').click(function() {
                    $('#J_Upload').uploadify('cancel', file.id);
                    $('#'+file.id).find('.cancel a, .cancel span').remove();
                    $('#'+file.id).find('.cancel').append('<span style="float:right;">'+Lang.uploadcancel+'</span>');  
                    $('#'+file.id).removeAttr('id');
                });
            },
            onDialogClose: function(queueData) {
                if (queueData.filesSelected == 0) {
                    return;
                }
                Modal.isRefresh = true;
                if (!Operate.uploadModal) {
                    Operate.uploadModal = Modal.createNew({
                        sign: 'upload',
                        title: Lang.uploadbtn,
                        body: (function() {
                            $('#J_Upload-queue').show();
                            $('#J_UploadBody').prepend($('#J_Upload-queue'));
                            return $('#J_UploadBody');
                        })(),
                        fnInit: function() {
                            $('#J_UploadBody').removeClass('hide');
                            $('#J_UploadCancelAll').removeClass('hide');
                            $('#J_UploadComplete').addClass('hide');

                            // cancel all
                            $('#J_UploadCancelAll').off('click').on('click', function() {
                                $('.uploadify-queue-item:not(.complete) .J_Cancel').click();
                            });
                            // upload complete and close window
                            $('#J_UploadComplete').off('click').on('click', function() {
                                Modal.eClose.click();
                            });
                        }
                    });
                }
                Operate.uploadModal.show();
                
                $('#J_Upload').uploadify('upload', '*');
                
                var left = Modal.eBody.width() + Modal.eBody.offset().left - 110;
                var top = Modal.eBody.height() + Modal.eBody.offset().top - 16;
                $('#J_UploadBox').css({left: left+'px', top: top + 'px'});
                $('#J_UploadBtnText').text(Lang.uploadcontinue);
                $('#J_UploadBox').addClass('upload-box');
                $('#J_Upload-queue').scrollTop(10000);
            },
            onQueueComplete: function(queueData) {
                $('#J_UploadCancelAll').addClass('hide');
                $('#J_UploadComplete').removeClass('hide');
            },
            onUploadProgress: function(file, bytesUploaded, bytesTotal, totalBytesUploaded, totalBytesTotal) {
                if (bytesUploaded >= bytesTotal) {
                    $('#'+file.id).find('.cancel a, .cancel span').remove();
                    $('#'+file.id).find('.cancel').append('<span style="float:right;">'+Lang.uploadwaiting+'</span>');
                }
            },
            onUploadComplete: function(file) {
                Operate.fnUploadStatus(file);
                $('#'+file.id).addClass('complete');
                if (file.filestatus == -5) {
                    $('#'+file.id).find('.cancel a, .cancel span').remove();
                    $('#'+file.id).find('.cancel').append('<span style="float:right;">'+Lang.uploadcancel+'</span>');  
                    $('#'+file.id).removeAttr('id');
                } else if (file.filestatus == -4) {
                    $('#'+file.id).find('.cancel a, .cancel span').remove();
                    $('#'+file.id).find('.cancel').append('<span style="float:right;" class="glyphicon glyphicon-ok"></span>');
                    $('#'+file.id).removeAttr('id');
                    List.fetchList($('.J_CrumbPath:last').attr('path'), true);
                }
            },
            onUploadError: function(file, errorCode, errorMsg, errorString) {
                if (errorString == 'IO Error') {
                    $.common.oInfoModal.show('', true);
                }
            }
        });

        // click event: open dialog for make sure delete
        $('.J_Delete').click(function() {
            var aPath = [];
            $('#J_FilesList .defaultstyle.selected').each(function() {
                var path = $(this).attr('path');
                aPath.push(path);
            });

            if (!Operate.deleteModal) {
                Operate.deleteModal =  Modal.createNew({
                    sign: 'delete',
                    title: Lang.deletebtn,
                    body: Lang.deleteinfo + aPath.length + Lang.filetext + '?',
                    fnSubmit: function() {
                        Operate.fnDelete();
                    }
                });
            }
            Operate.deleteModal.show();
            Modal.eBody.html(Lang.deleteinfo + aPath.length + Lang.filetext + '?');
        });

        // click event: new dir template render
        $('.J_CreateNew').click(function() {
            if ($('#J_NewDirName').length > 0) {
                $('#J_CreateCancel').click();
            }
            $('#J_FilesList').prepend($.common.setTemplate(List.sFileTmpl, Operate.initDir()));
            $('#J_NewDirName').focus(function() {
                $(this).select();
            });
            $('#J_NewDirName').val(Lang.newDirName).select();
        });

        // click event: refresh event
        $('.J_Refresh').click(function() {
            var path = $('.J_CrumbPath:last').attr('path');
            if (!path) {
                path = $('#J_HomePath').attr('path');
            }
            List.fetchList(path);
        })

        // click event: open dir tree dialog for copy or move files
        $('.J_CopyMove').click(function() {
            var action = $(this).attr('action');
            if (!Operate[action+'Modal']) {
                Operate[action+'Modal'] = Operate.getTreeModal(action);
            }
            Operate[action+'Modal'].show();
            DirTree.createNew(GO.oInfo.storages, 'J_DirTree').show();
        });

        // click event: download
        $('.J_Download').click(function() {
            Operate.fnDownload();
        });
    },
    fnUploadStatus: function(file) {
        var statusText = ' - ';
        if (file.filestatus == -3) {
            statusText += Lang.uploaderror;
            $('#'+file.id).find('.cancel a, .cancel span').remove();
        } else if (file.filestatus == -5) {
            statusText += Lang.uploadcancel;
        } else if (file.filestatus == -4) {
            statusText += Lang.uploadcomplete;
        } else {
            statusText = $('#'+file.id).find('.data').text();
        }
        $('#'+file.id).find('.data').text(statusText);
    },
    // get download url
    fnDownload: function() {
        var aPath = [];
        $('#J_FilesList .defaultstyle.selected').each(function() {
            aPath.push($(this).attr('path'));
        });
        if (aPath.length == 1 && $('#J_FilesList .defaultstyle.selected').hasClass('file')) {
            Operate.fnDownFile(aPath[0]);
            return;
        }
        var oParam = {};
        oParam.data = {
            action: 'get_download',
            path: aPath.join('|')
        };
        oParam.callback = function(oData) {
            if (oData.code == 0) {
                Operate.fnDownFile(oData.path);
            }
        }
        $.common.ajax(oParam);
    },
    // down file
    fnDownFile: function(path) {
        var url = GO.host+'?action=download&path='+path;
        $('body').append('<a id="J_DownloadZip" href="'+url+'" download="'+url+'">');
        document.getElementById('J_DownloadZip').click();
        $('#J_DownloadZip').remove();
    },

    // create modal where click copy or move for view dir tree
    getTreeModal: function(action) {
        var title = (action == 'copy') ? Lang.copybtn : Lang.movebtn;
        return Modal.createNew({
            'sign': action,
            'title': title,
            'body': $('#J_TreeTmpl').html(),
            'fnSubmit': function() {
                var toPath = $('.treeview-node.selected').attr('path');
                var aPath = [];
                var flag = true;
                $('#J_FilesList .defaultstyle.selected').each(function() {
                    var path = $(this).attr('path');
                    var name = $(this).find('.file-name').text();
                    if (toPath.indexOf(path) != -1) {
                        $.common.oInfoModal.show(Lang.containerror + path + title + toPath);
                        flag = false;
                    } else if ((toPath + '/' + name) == path) {
                        $.common.oInfoModal.show(Lang.containerror + title + Lang.cantmove);
                    } else {
                        aPath.push(path);
                    }
                });
                if (!flag || aPath.length == 0) {
                    return;
                }
                Operate.fnCopyMove(action, aPath, toPath);
            },
            'fnInit': function() {
                if (Modal.eFooter.find('.J_CreateNewTree').length === 0) {
                    Modal.eFooter.prepend('<a class="J_CreateNewTree pull-left btn btn-success" style="font-size:14px; margin-right:20px;">'+Lang.newfolder+'</a>');
                }
            }
        });
    },
    // ajax request for copy and move
    fnCopyMove: function(action, from, to, force) {
        var oParam = {};
        force = force || 0;
        oParam.data = {
            action: action,
            from: from.join('|'),
            to: to,
            force: force
        };
        oParam.callback = function(data) {
            if (data.code == 0) {
            	var msg = (action == "move") ? Lang.move : Lang.copy;
                Operate[action+'Modal'].hide();
                var oInfoModal = $.common.oInfoModal;
                var showBtn = false;
                oInfoModal.show(msg + Lang.complete, false, showBtn);
                if (!showBtn) {
                    window.setTimeout(function() {
                        oInfoModal.hide();
                    }, 2000);
                }
                List.fetchList();
            } else if (data.code == 2) {
                $.common.oInfoModal.hide();
                var msg = '<br>';
                if (data.count > 0) {
                    msg += Lang.success + ':' + data.count + '<br>';
                }
                msg += Lang.moveerrorinfo;
                $.common.oInfoModal.show(msg , false, true, function() {
                    Operate.fnCopyMove(action, data.paths, to, 1);
                });
            }
        }
        $.common.ajax(oParam);
    },
    /* ajax request for delete files */
    fnDelete: function() {
        var aPath = [];
        $('#J_FilesList .defaultstyle.selected').each(function() {
            var path = $(this).attr('path');
            aPath.push(path);
        })
        var path = aPath.join('|');
        var oParam = {};
        oParam.data = {
            action: 'delete',
            path: path
        };
        oParam.callback = function(oData) {
            Operate.deleteModal.hide();
            if (oData.code == 0) {
                $.common.oInfoModal.show(Lang.success, false);
                window.setTimeout(function() {
                    $.common.oInfoModal.hide();
                }, 2000);
                List.fetchList($('.J_CrumbPath:last').attr('path'), true);
            }
        }
        $.common.ajax(oParam);
    },

    // ajax request for create new dir 
    fnCreateDir: function() {
        var newName = $('#J_NewDirName').val();
        if (!$.common.filterName(newName)) {
            return;
        }
        var path = $('.J_CrumbPath:last').attr('path');
        if (!path) {
            path = $('#J_HomePath').attr('path');
        }
        
        var oParam = {};
        oParam.data = {
            action: 'create_dir',
            path: path,
            dir_name: newName
        };
        oParam.callback = function(oData) {
            if (oData.code == 0) {
                List.fetchList(path);
            }
        };
        $.common.ajax(oParam);
    },

    initDir: function(file) {
        file = file || {};
        return {
            time: file.time || 0, 
            cls: file.type || "dir", 
            type: file.type || 'dir',
            name: file.name || Operate.newDirTmpl,
            imgname: file.name || Lang.newfolder,
            size: file.size || '0kb'
        };
    }
};

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
    }
};

var List = {
    sFileTmpl: $('#J_FileTmpl').html(),
    
    eFilesList: $('#J_FilesList'),

    init: function() {
        var isCtrl = false;
        $(window).on({
            keydown: function(e) {
                if (e.keyCode == 17 || e.keyCode == 91) {
                    isCtrl = true;
                } else {
                    isCtrl = false;
                }
            },
            keyup: function(e) {
                isCtrl = false;
            }
        });
        List.eFilesList.dblclick(function(e) {
            e.preventDefault();
        });
        List.eFilesList.click(function(e) {
            e.preventDefault();
            var eTag = $(e.target);
            if (eTag.is('#J_CreateCancel, #J_CreateCancel *')) {// cancel create new folder
                eTag.parents('.defaultstyle').remove();
                return;
            } else if (eTag.is('#J_CreateOk')) {// submit create new folder
                Operate.fnCreateDir();
		        return;
            } else if (eTag.is('a.dir-link, .grid .dir-icon')) {// click folder name link or folder icon open the folder
                eTag = eTag.parents('.defaultstyle');
                var path = eTag.attr('path');
                var name = eTag.find('.file-name').text();
                $('.breadcrumb').append('<li class="J_CrumbPath" path="'+path+'"><a href="#">'+name+'</a></li>');
                List.fetchList(path);
                return;
            } else if (eTag.is('.chk-box, .chk-box *')) {
            	eTag = eTag.parents('.defaultstyle');
            	if (eTag.hasClass('selected')) {
                    eTag.removeClass('selected');
                } else {
                    eTag.addClass('selected');
                }
            } else if (eTag.is('.defaultstyle, .defaultstyle *') && eTag.not('#J_NewDirName')) {
                eTag = eTag.is('.defaultstyle') ? eTag : eTag.parents('.defaultstyle');
                if (eTag.find('#J_NewDirName').length > 0) {
                    return;
                }

                if (isCtrl) {
                    if (eTag.hasClass('selected')) {
                        eTag.removeClass('selected');
                    } else {
                        eTag.addClass('selected');
                    }
                } else {
                    var flag = false;
                    if (!eTag.hasClass('selected') || List.eFilesList.find('.selected').length > 1) {
                        flag = true;
                    }
                    List.eFilesList.find('.selected').removeClass('selected');
                    if (flag) {
                        eTag.addClass('selected');
                    }
                }
            }
            /* if more 0 file be selected, operate button can be use , else button cann't use */
            if (List.eFilesList.find('.defaultstyle.selected').length > 0) {
                $('.J_OperateBtns .btn').removeClass('disabled');
            } else {
                $('.J_OperateBtns .btn').addClass('disabled');
            }
            /* if all file be selected, the select all button be checked, else the button is not checked */
            if (List.eFilesList.find('.defaultstyle.selected').length == List.eFilesList.find('.defaultstyle').length) {
                $('#J_SelectAll').addClass('selected');
            } else {
                $('#J_SelectAll').removeClass('selected');
            }
            GO.renderFooter();
        });

        $('#J_SelectAll').click(function() {
            if ($(this).hasClass('selected')) {
                $(this).removeClass('selected');
                List.eFilesList.find('.defaultstyle.selected').removeClass('selected');
                $('.J_OperateBtns .btn').addClass('disabled');
            } else {
                $(this).addClass('selected');
                List.eFilesList.find('.defaultstyle').addClass('selected');
                $('.J_OperateBtns .btn').removeClass('disabled');
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
            List.eFilesList.html(str);
            Operate.eOperateBtns.find('.btn').addClass('disabled');
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

/* Left Menu init*/
var Menu = {
	menuData: [],
    init: function() {
        $('.main-l').height($(window).height());
        Menu.menuData = [{
            name: Lang.menus.file,
            action: 'info',
            img: 'file',
            filter: '',
            is_online: 1
        },{
            name: Lang.menus.photo,
            action: 'photo',
            img: 'photo',
            filter: '',
            is_online: 0
        }];
        Menu.initData();
    },
    initData: function() {
        var data = Menu.menuData;
        var oTmpl = $('#J_TmplMenu').html();
        for (var i = 0; i < data.length; i ++) {
            data[i].cover = data[i].is_online ? '' : 'cover';
            data[i].tooltip = data[i].is_online ? '' : 'data-toggle="tooltip" data-placement="right" title="'+ Lang.comming +'"';
        }
        $('#J_MenuBox').html($.common.setTemplate(oTmpl, data));
        Menu.initEvent();
        $('.main-lmenu:not(.cover):first').click();
    },

    initEvent: function() {
        $('.main-lmenu.cover').tooltip();
        var target = $('.main-lmenu:not(.cover)');
        target.unbind();
        target.on('hover', function(e) {
            $(this).addClass('main-lmenu-hover')
        })
        target.on('click', function(e) {
            $(this).removeClass('main-lmenu-default').addClass('main-lmenu-click');
            if ($(this).siblings().hasClass('main-lmenu-click')) {
                $(this).siblings().removeClass('main-lmenu-click');
            }
            GO.getInfo($(this).attr('action'));
        });
        // init logo event
        $('.main-l-logo').on('click', function() {
            $('.main-lmenu-click').click();
        });
    }
};


var GO = {
    is_enable: true,
    host: '',
    eMain: $('#main'),
    loginModal: Modal.createNew(Login),

    init: function() {
        GO.eMain.removeClass('hide');

        GO.setWindowSize();
        $(window).resize(function() {
            GO.setWindowSize();
        });
        Menu.init();
        List.init();
        Operate.init();
    },

    setWindowSize: function() {
        var winH = $(window).height();
        var panelHH = $('.panel-heading').outerHeight();
        var topBtnH = $('#J_TopBtns').outerHeight();
        var footerH = $('#J_Footer').outerHeight();
        $('#main').height(winH);
        $('#J_FilesList').height(winH - panelHH - topBtnH - footerH -1);
    },

    initLanguage: function() {
        var oParam = {};
        oParam.type = "GET";
        oParam.data = {action: 'get_country'};
        oParam.callback = function(oData) {
            if (oData.code == 0) {
                //oData.country = 'en';
                Lang = language[oData.country];
                GO.renderLanguage();
                if (oData.enable == 1) {
                    GO.is_enable = true;
                } else {
                    GO.is_enable = false;
                }
            }
        };
        $.common.ajax(oParam);
    },
    renderLanguage: function() {
        for (var x in Lang) {
            $('[langkey='+x+']').text(Lang[x]);
        }
    },

    oInfo: {},
    getInfo: function(action, refresh) {
        action = action || 'info';
        var oParam = {};
        oParam.data = {action: action};
        oParam.callback = function(oData) {
            if (oData.storages) {
                GO.oInfo = oData;
                if (!refresh) {
                    var content = '';
                    for (var i = oData.storages.length -1 ; i >=0 ; i --) {
                        content += '<option value="' + oData.storages[i].path +'">' + oData.storages[i].name + '</option>';
                    }
                    $('.J_Storage').html(content);
                    CrumbNav.init();
                    $('#J_MyDevice').text(oData.phone_name);
                    $('#J_Version').text(oData.v);
                    GO.host = oData.ip;
                } else {
                    GO.renderFooter();    
                }
            }
        }
        $.common.ajax(oParam, refresh);
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
            var storages = GO.oInfo.storages;
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