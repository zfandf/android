var eMenuBox, eList, eOperateBtns, eCurmbBox;

Main = {
    fnInit: function() {
        eMenuBox = $('#J_MenuBox');
        eList = $('#J_FilesList');
        eOperateBtns = $('.J_OperateBtns');
        eCurmbBox = $('#J_CrumbsBox')

        eAllBox.addClass('hide');
        eMainBox.removeClass('hide');


        Main.setWindowSize();
        $(window).resize(function() {
            Main.setWindowSize();
        });

        Menu.fnInit();
    },

    setWindowSize: function() {
        var winH = $(window).height();
        var headerH = $('#J_MainHeader').outerHeight();
        var footerH = $('#J_MainFooter').outerHeight();
        $('#J_MainBody').height(winH - headerH - footerH - 2);

        $('.main-l').height($(window).height());
    }
};


/* Left Menu init*/
Menu = {
    menuData: {
        'info': {
            name: Lang.menus.file,
            img: 'file',
            filter: '',
            is_online: 1,
            operate: {'refresh': 0, 'download': 1, 'upload': 1, 'copy': 1, 'create': 1, 'delete': 1}
        },
        'photo': {
            name: Lang.menus.photo,
            img: 'photo',
            filter: '',
            is_online: 1,
            operate: {'refresh': 0, 'download': 1, 'upload': 0, 'copy': 0, 'create': 0, 'delete': 1}
        }
    },

    fnInit: function() {
        var data = Menu.menuData;

        for (var action in data) {
            var name = data[action].name;
            var is_online = data[action].is_online;
            if (!is_online) {
                eCurMenu = eMenuBox.find('[action='+action+']');
                eCurMenu.addClass('cover').attr({
                    'data-toggle': 'tooltip',
                    'data-placement': 'right',
                    'title': Lang.comming
                });
            }                
        }
        Menu.fnInitEvent();
        List.fnInit();
        Modal.init();
    },

    fnInitEvent: function() {
        $('.main-lmenu.cover').tooltip();
        var eTag = $('.main-lmenu:not(.cover)');
        eTag.unbind();
        eTag.on('click', function(e) {
            $(this).removeClass('main-lmenu-default').addClass('main-lmenu-click');
            if ($(this).siblings().hasClass('main-lmenu-click')) {
                $(this).siblings().removeClass('main-lmenu-click');
            }
            ActionType = $(this).attr('action');
            
            $('.J_Curmb').addClass('hide');
            $('.J_Curmb[action='+ActionType+']').removeClass('hide');
            
            eList.html("");
            Operate.fnInit(Menu.menuData[ActionType].operate);
            Main.setWindowSize();

            if (ActionType == 'info') {
                Menu.getInfo(ActionType);
            } else if (ActionType == 'photo') {
                Menu.renderPhotoList();
            }
        });

        $('.main-lmenu:not(.cover):first').click();

        // init logo event
        $('.main-l-logo').on('click', function() {
            $('.main-lmenu-click').click();
        });
    },

    renderPhotoList: function() {
        $.getScript('assets/js/photo.js')
            .done(function() {
                /* 执行成功后的处理 */
                Photo.fnInit();
            })
            .fail(function() {
                /* 执行失败后的处理 */
        });
    },

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
                    HOST = oData.ip;
                } else {
                    GO.renderFooter();    
                }
            }
        }
        $.common.ajax(oParam, refresh);
    }
};
