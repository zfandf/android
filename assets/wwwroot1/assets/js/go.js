$(document).ready(function() {
});



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

var GO = {
    is_enable: true,
    host: '',
    eMain: $('#main'),

    oInfo: {},

    fnPhotoCallback: function(oData) {

    },

    fnInfoCallback: function(oData, refresh) {
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
                HOST = oData.ip;
            } else {
                GO.renderFooter();    
            }
        }
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