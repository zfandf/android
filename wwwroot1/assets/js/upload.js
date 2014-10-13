Upload = {
    fnInit: function() {

        $.getScript('assets/js/jquery.uploadify.min.js')
            .done(function() {
                /* 执行成功后的处理 */
                Upload.fnInitUploadify();
            })
            .fail(function() {
                /* 执行失败后的处理 */
        });
        
    },

    fnInitUploadify: function() {
        // click event: open upload dialog for select files
        $('#J_Upload').uploadify({
            'auto': false,
            'buttonText': '<span class="glyphicon glyphicon-open"></span><span id="J_UploadBtnText">'+ (Lang.uploadbtn || 'Upload Files') + '</span>',
            'buttonClass': 'btn btn-success',
            'removeCompleted' : false,
            'swf'      : 'assets/uploadify.swf',
            'uploader' : HOST,
            'onSelect': function(file) {
                
                var path = $('.J_CrumbPath:last').attr('path');
                path = HOST + '?action=upload&path='+path+'&radum='+(new Date().getTime());
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
                Upload.fnUploadStatus(file);
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
    }
};