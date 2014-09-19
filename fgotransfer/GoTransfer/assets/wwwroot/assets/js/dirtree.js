var DirTree = {
    nodeTmpl: $('#J_TreeNodeTmpl').html(),
    loadingDir: $('#loading-dir'),
    createNew: function(data, id) {
        var tree = {};
        tree.data = data;
        tree.id = id; 
        $('#'+id).addClass('dirtree');
        tree.show = DirTree.show;
        return tree;
    },
    show: function(tree) {
        tree = tree || this;
        var eBox = $('#'+tree.id);
        var str = DirTree.getTreeList(tree.data);
        eBox.html(str);
        DirTree.initEvent(eBox);
    },
    initEvent: function(eBox) {
        eBox.off('click').on('click', function(e) {
            var eTag = $(e.target);
            if (eTag.is('#J_CreateCancel, #J_CreateCancel *')) {
                var parent = eTag.parents('ul:eq(0)');
                // when click cancle and there is no sub dir, make sure to hide the plus icon
                if (parent.children('li').length === 1) {
                    parent.prev().addClass('treenode-empty').find('.plus').hide();
                }
                eTag.parents('li:eq(0)').remove();
            } else if (eTag.is('#J_CreateOk')) { // click [create new dir] button
                DirTree.createDir(eTag.parents('ul:eq(0)').prev());
            } else if (eTag.is('.treeview-node, .treeview-node *')) { // click parent node
                eTag = eTag.is('.treeview-node') ? eTag : eTag.parents('.treeview-node:eq(0)');

                // new folder can not be selected
                if (eTag.attr('path') === '') {
                    return;
                }

                // empty dir can not be open
                if (!eTag.hasClass("selected")) {
                    $('.treeview-node.selected').removeClass('selected');
                    eTag.addClass("selected");
                }
                if (eTag.hasClass("treenode-empty")) {
                    return;
                }

                if (eTag.hasClass("_minus")) { // has children and has open
                    eTag.removeClass('_minus').find('.plus:eq(0)').addClass('fa-plus-square-o').removeClass('fa-minus-square-o');
                } else { // list children
                    eTag.addClass("_minus").find('.plus:eq(0)').addClass('fa-minus-square-o').removeClass('fa-plus-square-o');
                    if (eTag.parents('li:eq(0)').find('ul:eq(0)>li').length == 0) {
                        DirTree.addNode(eTag);
                    }
                }
            }
        });

        $('#J_NewForm').off().on('submit', function(e) {
            e.preventDefault();
            $('#J_CreateOk').click();
        });

        $('.J_CreateNewTree').click(function() {
            if ($('#J_NewDirName').length > 0) {
                $('#J_CreateCancel').click();
            }
            var selectNode = $('.treeview-node.selected');
            var parentNode = selectNode.parent();

            if (parentNode.find('ul:eq(0)').length == 0) {
                parentNode.append('<ul></ul>');
                selectNode.addClass('_minus').removeClass('treenode-empty');
            }
            var file = {
                idx: 1*selectNode.attr('idx') + 1,
                path: ''
            };
            // if parent is close, open it
            if (!selectNode.hasClass('treenode-empty') && !selectNode.hasClass('_minus')) {
                selectNode.addClass('_minus');
            }
            // when click cancle, and then click create new, the plus will be hide, so make sure to show plus icon
            selectNode.find('.plus').show();
            parentNode.find('ul:eq(0)').prepend($.common.setTemplate(DirTree.nodeTmpl, DirTree.initDir(file)));
            $('#J_NewDirName').val(Lang.newDirName).select();
        });
        
        // open default
        var currentSelectedStoroge = $('.J_Storage').val();
        $('.treeview-node[path="'+currentSelectedStoroge+'"]').click();

    },
    createDir: function(eTag) {
        // eTag.find('.plus').show();
        var newName = $('#J_NewDirName').val();
        if (!$.common.filterName(newName)) {
            return;
        }
        var path = eTag.attr('path');
        var oParam = {};
        oParam.data = {
            action: 'create_dir',
            path: path,
            dir_name: newName
        };
        oParam.callback = function(oData) {
            if (oData.code == 0) {
                $('#J_Creating').parents('.treeview-node:eq(0)').attr('path', path+'/'+newName);
                $('#J_Creating').replaceWith(newName);
                $('#J_Creating').parents('.treeview-node:eq(0)').click();
                // refersh backup list
                // List.fetchList();
            }
        }
        $.common.ajax(oParam);
    },
    addNode: function(eTag) {
        // hide plus and show loading
        eTag.find('.plus').hide();
        DirTree.loadingDir.appendTo(eTag.find('.loading-dir'));
        DirTree.loadingDir.removeClass('hide');
        var path = eTag.attr('path');
        var oParam = {};
        oParam.data = {
            action: 'file',
            path: path,
            filter: 'dir'
        };
        oParam.callback = function(oData) {
            if (oData.code != 0) {
                return;
            }
            var idx = 1*eTag.attr('idx') + 1;
            var nodes = '<ul>'+DirTree.getTreeList(oData.files, idx)+'</ul>';
            eTag.parent().append(nodes);
            // hide loading
            DirTree.loadingDir.addClass('hide');
            eTag.find('.plus').show();
        }
        // get data and hide common loading
        $.common.ajax(oParam, true);
    },
    getTreeList: function(data, idx) {
        var str = '';
        idx = idx || 1;
        data.idx = idx;
        for (var i = 0; i < data.length; i++) {
            if (idx == 1) { // top dir must has sub dir
                data[i].has_sub_dir = true;    
            }
            data[i].idx = idx;
            data[i].cls = (data[i].has_sub_dir) ? '' : 'treenode-empty';
            str += $.common.setTemplate(DirTree.nodeTmpl, DirTree.initDir(data[i]));
        }
        return str;
    },

    initDir: function(file) {
        file = file || {};
        return {
            name: file.name || Operate.newDirTmpl,
            subTree: file.subTree || '',
            cls: (file.cls == null) ? 'treenode-empty' : file.cls,
            idx: file.idx || 1,
            paddingLeft: 15*file.idx,
            path: file.path
        }
    }
};