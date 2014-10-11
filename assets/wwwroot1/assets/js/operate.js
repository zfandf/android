/* all operate logic*/
Operate = {
    newDirTmpl: $('#J_NewDirTmpl').html(),// create new dir template
    
    copyModal: null, // modal for copy
    moveModal: null, // modal for copy
    uploadModal: null,// modal for upload
    deleteModal: null,// 文件删除确认 modal

    // initialize operate event
    fnInit: function(curMenuOperate) {
        // default all buttons not be work
        eOperateBtns.removeClass('novisiable');

        Operate.fnInitEvent(curMenuOperate);
    },

    fnInitEvent: function(curMenuOperate) {
        var refreshBtn = $('.J_Refresh'),
            downloadBtn = $('.J_Download'),
            deleteBtn = $('.J_Delete'),
            createBtn = $('.J_CreateNew'),
            copymoveBtn = $('.J_CopyMove'),
            createForm = $('#J_NewForm'),
            uploadBtn = $('#J_UploadBox');

        $('.J_OperateBtn').addClass('hide');
        if (curMenuOperate['refresh']) {
            // click event: refresh event
            refreshBtn.off('click').on('click', Operate.refreshClick);
            refreshBtn.removeClass('hide');
        }
        if (curMenuOperate['download']) {
            // click event: download
            downloadBtn.off('click').on('click', Operate.downloadClick);
            downloadBtn.removeClass('hide');
        }
        if (curMenuOperate['delete']) {
            // click event: open dialog for make sure delete
            deleteBtn.off('click').on('click', Operate.deleteClick);
            deleteBtn.removeClass('hide');
        }
        if (curMenuOperate['create']) {
            // click event: new dir template render
            createForm.off('click').on('submit', Operate.newsubmitClick);
            createBtn.off('click').on('click', Operate.createnewClick);
            createBtn.removeClass('hide');
        }
        if (curMenuOperate['copy']) {
            // click event: open dir tree dialog for copy or move files
            copymoveBtn.off('click').on('click', Operate.copymoveClick);
            copymoveBtn.removeClass('hide');
        }
        if (curMenuOperate['upload'] && $('#SWFUpload_0').length == 0) {
            Operate.fnRenderUpload();
            uploadBtn.removeClass('hide');
        }
        if (!curMenuOperate['upload'] && !curMenuOperate['refresh'] && !curMenuOperate['create']) {
            $('#J_FixedBtnsBox').addClass('hide');
        } else {
            $('#J_FixedBtnsBox').removeClass('hide');
        }
    },

    /*
     * click event
     */
    refreshClick: function(e) {
        var path = $('.J_CrumbPath:last').attr('path');
        if (!path) {
            path = $('#J_HomePath').attr('path');
        }
        List.fetchList(path);
    },
    downloadClick: function(e) {
        Operate.fnDownload();
    },
    deleteClick: function(e) {
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
    },
    newsubmitClick: function(e) {
        e.preventDefault();
        Operate.fnCreateDir();
    },
    createnewClick: function(e) {
        if ($('#J_NewDirName').length > 0) {
            $('#J_CreateCancel').click();
        }
        $('#J_FilesList').prepend($.common.setTemplate(List.sFileTmpl, Operate.initDir()));
        $('#J_NewDirName').focus(function() {
            $(this).select();
        });
        $('#J_NewDirName').val(Lang.newDirName).select();
    },
    copymoveClick: function() {
        var action = $(this).attr('action');
        if (!Operate[action+'Modal']) {
            Operate[action+'Modal'] = Operate.getTreeModal(action);
        }
        Operate[action+'Modal'].show();
        DirTree.createNew(GO.oInfo.storages, 'J_DirTree').show();
    },

    // 渲染上传插件
    fnRenderUpload: function() {
        $.getScript('assets/js/upload.js')
            .done(function() {
                /* 执行成功后的处理 */
                $('#J_UploadBox').removeClass('novisiable');
            })
            .fail(function() {
                /* 执行失败后的处理 */
            });
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