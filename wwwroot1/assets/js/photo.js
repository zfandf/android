Photo = {
    bucketTmpl: $('#J_PhotoGroupTmpl').html(),
    photoTmpl: $('#J_PhotoTmpl').html(),
    sortType: 0, // 0 相册， 1 时间

    ePicPreview: $('#J_PicPreview'),
    ePicImage: $('#J_ShowPicImage'),
    ePicPath: $('#J_ShowPicPath'),
    eClosePicPreview: $('#J_ClosePicPreview'),
    ePicLeft: $('#J_ShowLeftPic'),
    ePicRight: $('#J_ShowRightPic'),
    ePicDelete: $('#J_PicDelete'),
    ePicDownload: $('#J_PicDownload'),
    ePicName: $('#J_PicName'),
    
    fnInit: function() {
        Photo.initEvent();
    },
    initEvent: function() {
        $('.J_PhotoCurmb').off('click').on('click', function() {
            var sort_type = $(this).attr('sort_type');
            Photo.sortType = sort_type;
            Photo.getPhotos();
        });
        $('.J_PhotoCurmb:eq(0)').click();

        Photo.eClosePicPreview.off('click').on('click', function() {
            Photo.ePicPreview.addClass('hide');
        });
        Photo.ePicDelete.off('click').on('click', function() {
            var path = Photo.ePicPath.val();
            Operate.fnDelete(path);
        });
        Photo.ePicDownload.off('click').on('click', function() {
            var path = Photo.ePicPath.val();
            Operate.fnDownFile(path);
        });
        Photo.ePicLeft.off('click').on('click', function() {
            var path = Photo.ePicPath.val();
            Photo.initPicPreview($('.J_Photo[path="'+path+'"]').prev().attr('path'));
        });
        Photo.ePicRight.off('click').on('click', function() {
            var path = Photo.ePicPath.val();
            Photo.initPicPreview($('.J_Photo[path="'+path+'"]').next().attr('path'));
        });

    },
    getPhotos: function(bucket_id) {
        var oParam = {};
        bucket_id = bucket_id || 0;
        oParam.data = {
            action: 'photos',
            bucket_id: bucket_id,
            sort_type: Photo.sortType
        };
        oParam.callback = function(oData) {
            if (oData.code != 0) {
                return;
            }
            if (bucket_id) {
                Photo.photoCallback(bucket_id, oData);
            } else {
                Photo.bucketCallback(oData);
            }
        }
        $.common.ajax(oParam);
    },

    photoCallback: function(bucket_id, oData) {
        if (oData.photos) {
            for (var i = 0; i < oData.photos.length; i++) {
                oData.photos[i].imgSrc = HOST + '?action=image&is_thumb=1&path=' + oData.photos[i].path;
            }
            var str = $.common.setTemplate(Photo.photoTmpl, oData.photos);
            ePhotoBox = $('.J_Photos[bucket_id="'+bucket_id+'"]');
            ePhotoBox.html(str);
            Photo.initPhoto(bucket_id);
        }
    },
    initPhoto: function(bucket_id) {
        var ePhotos = $('.J_Photos[bucket_id="'+bucket_id+'"]');
        var ePhotoShows = ePhotos.find('.J_ShowPic');
        var ePhotoSelect = ePhotos.find('.J_SelectPhoto');

        ePhotoSelect.off('click').on('click', function() {
            var path = $(this).attr('path');
            var ePhoto = $('.J_Photo[path="'+path+'"]');
            if (ePhoto.hasClass('selected')) {
                ePhoto.removeClass('selected');
            } else {
                ePhoto.addClass('selected');
            }
        });

        ePhotoShows.off('click').on('click', function() {
            var path = $(this).attr('path');
            Photo.initPicPreview(path);
        });
    },
    initPicPreview: function(path) {
        Photo.ePicPreview.removeClass('hide');
        Photo.ePicPath.val(path);
        Photo.ePicImage.attr('src', HOST + '?action=image&is_thumb=0&path=' + path);
        Photo.ePicName.text($('.J_PhotoName[path="'+path+'"]').val());
    },

    bucketCallback: function(oData) {
        if (oData.photos) {
            var str = $.common.setTemplate(Photo.bucketTmpl, oData.photos);
            eList.html(str);
            Photo.initBucket();
        }
    },
    initBucket: function() {
        $('.J_BucketSelect').off('click').on('click', function() {
            var bucket_id = $(this).attr('bucket_id');
            if ($(this).hasClass('selected')) {
                $(this).removeClass('selected');
                $('.J_Photos[bucket_id="'+bucket_id+'"] .J_Photo').removeClass('selected');
                return;
            }
            $(this).addClass('selected');
            $('.J_Photos[bucket_id="'+bucket_id+'"] .J_Photo').addClass('selected');
        });
        
        $('.J_Bucket').click(function(e) {
            var eBucket = $(this);
            
            var eSign = eBucket.find('.J_BucketSign');
            var ePhotoBox = eBucket.next('.J_Photos');
            var bucket_id = $(this).attr('bucket_id');
            if (eBucket.hasClass('minus')) {
                eBucket.removeClass('minus');
                eSign.removeClass('glyphicon-minus');
                ePhotoBox.addClass('hide');
            } else {
                eBucket.addClass('minus');
                eSign.addClass('glyphicon-minus');
                ePhotoBox.removeClass('hide');
                if (ePhotoBox.children().length == 0) {
                    Photo.getPhotos(bucket_id);
                }
            }
        });
        $('.J_Bucket:eq(0)').click();
    }
};